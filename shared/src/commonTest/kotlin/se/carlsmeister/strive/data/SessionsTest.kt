package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionsTest {
    private val file = "sessions-test.db"

    @BeforeTest
    @AfterTest
    fun clean() = deleteFileDatabase(file)

    private fun StriveDatabase.exercise(name: String, type: String = "weight_reps"): Long {
        exerciseQueries.insertSeed(name, "", "chest", "", "Horisontell press", "Skivstång", type, false, 2.5, 20.0, "strength", "", "", "", "")
        return exerciseQueries.all().executeAsList().first { it.name == name }.id
    }

    private fun sessions(driver: SqlDriver = memoryDriver()) = StriveDatabase(driver).let { it to SqlSessions(it) }

    @Test
    fun aStartedWorkoutIsOpenAndEmpty() = runTest {
        val (_, sessions) = sessions()
        val id = sessions.start("Pass", now = 1_000)
        val open = sessions.open().first()!!
        assertEquals(id, open.id)
        assertEquals("Pass", open.name)
        assertEquals(1_000, open.startedAt)
        assertEquals(emptyList(), open.exercises)
    }

    @Test
    fun anAddedExerciseHasOneEmptySetAndANewSetCopiesTheLastWithoutItsRir() = runTest {
        val (db, sessions) = sessions()
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB"), db.exercise("Pull Up", "bodyweight_reps")))
        val bench = sessions.open().first()!!.exercises.first()
        assertEquals(listOf("Bench Press BB", "Pull Up"), sessions.open().first()!!.exercises.map { it.name })
        assertEquals(ExerciseType.WeightReps, bench.type)
        assertEquals(listOf(WorkoutSet(bench.sets.single().id)), bench.sets)

        sessions.saveSet(bench.sets.single().copy(weight = 80.0, reps = 8, rir = 2, done = true))
        sessions.addSet(bench.id)
        val sets = sessions.open().first()!!.exercises.first().sets
        assertEquals(2, sets.size)
        assertEquals(WorkoutSet(sets[1].id, weight = 80.0, reps = 8), sets[1])
    }

    @Test
    fun loggingAsTheyStandKeepsWhatCanBeLoggedAndDropsTheRest() = runTest {
        val (db, sessions) = sessions()
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB"), db.exercise("Squat BB")))
        val (bench, squat) = sessions.open().first()!!.exercises
        sessions.saveSet(bench.sets.single().copy(weight = 80.0, reps = 8, done = true))
        sessions.addSet(bench.id) // 80 × 8, not logged
        sessions.addSet(bench.id)
        sessions.deleteSet(squat.sets.single().id)
        sessions.addSet(squat.id) // empty, cannot be logged

        sessions.finish(id, Finish.LogAsTheyStand, now = 60_000)

        assertNull(sessions.open().first())
        val done = sessions.history().first().single()
        assertEquals(60_000, done.endedAt)
        assertEquals(listOf("Bench Press BB"), done.exercises.map { it.name })
        assertEquals(3, done.sets)
        assertTrue(done.exercises.single().sets.all { it.done })
    }

    @Test
    fun savingWithoutThemDropsEverySetNotLogged() = runTest {
        val (db, sessions) = sessions()
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        val bench = sessions.open().first()!!.exercises.single()
        sessions.saveSet(bench.sets.single().copy(weight = 80.0, reps = 8, done = true))
        sessions.addSet(bench.id)

        sessions.finish(id, Finish.WithoutThem, now = 1)

        assertEquals(1, sessions.history().first().single().sets)
    }

    @Test
    fun aDiscardedWorkoutLeavesNothingBehind() = runTest {
        val driver = memoryDriver()
        val (db, sessions) = sessions(driver)
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        sessions.discard(id)
        assertNull(sessions.open().first())
        for (table in listOf("session", "session_exercise", "workout_set")) {
            assertEquals(0L, driver.executeQuery(null, "SELECT COUNT(*) FROM $table", { it.next(); QueryResult.Value(it.getLong(0)) }, 0).value, table)
        }
    }

    @Test
    fun aWorkoutKilledMidSessionReopensWithItsLoggedSets() = runTest {
        val first = fileDriver(file, StriveDatabase.Schema)
        val (db, sessions) = sessions(first)
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        sessions.saveSet(sessions.open().first()!!.exercises.single().sets.single().copy(weight = 80.0, reps = 8, done = true))
        first.close() // the app is killed

        val again = fileDriver(file, StriveDatabase.Schema)
        try {
            val open = SqlSessions(StriveDatabase(again)).open().first()!!
            assertEquals(id, open.id)
            assertEquals(1, open.logged)
        } finally {
            again.close()
        }
    }

    @Test
    fun aFinishedWorkoutIsInHistoryAfterARestart() = runTest {
        val first = fileDriver(file, StriveDatabase.Schema)
        val (db, sessions) = sessions(first)
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        sessions.saveSet(sessions.open().first()!!.exercises.single().sets.single().copy(weight = 80.0, reps = 8, done = true))
        sessions.finish(id, Finish.WithoutThem, now = 1)
        first.close()

        val again = fileDriver(file, StriveDatabase.Schema)
        try {
            val done = SqlSessions(StriveDatabase(again)).history().first().single()
            assertEquals(listOf(WorkoutSet(done.exercises.single().sets.single().id, weight = 80.0, reps = 8, done = true)), done.exercises.single().sets)
        } finally {
            again.close()
        }
    }

    /** A workout finished at [at] that does [exerciseId] once per note in [notes], each time with [sets] logged. */
    private suspend fun SqlSessions.finished(
        at: Long, exerciseId: Long, notes: List<String> = listOf(""), sets: List<WorkoutSet> = listOf(WorkoutSet(0, 80.0, 8)),
    ): Long {
        val id = start("Pass", at)
        add(id, List(notes.size) { exerciseId })
        for ((e, note) in workout(id).first()!!.exercises.zip(notes)) {
            repeat(sets.size - 1) { addSet(e.id) }
            val ids = workout(id).first()!!.exercises.single { it.id == e.id }.sets.map { it.id }
            sets.zip(ids) { set, setId ->
                saveSet(set.copy(id = setId, done = true))
                setKind(setId, set.kind)
            }
            setExerciseNote(e.id, note)
        }
        finish(id, Finish.WithoutThem, now = at)
        return id
    }

    @Test
    fun thePreviousNoteIsTheLastFinishedSessionsNoteForThatExercise() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(1, bench, listOf("old"))
        sessions.finished(2, bench, listOf("Kändes stark idag."))
        val open = sessions.start("Pass", 3)
        assertEquals("Kändes stark idag.", sessions.previousNote(bench, open).first())
    }

    @Test
    fun anEmptyNoteLastTimeGivesNoPreviousNote() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(1, bench, listOf("old"))
        sessions.finished(2, bench, listOf(""))
        val open = sessions.start("Pass", 3)
        assertNull(sessions.previousNote(bench, open).first())
    }

    @Test
    fun theOpenSessionsOwnNoteIsNotItsPrevious() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        val open = sessions.start("Pass", 3)
        sessions.add(open, listOf(bench))
        sessions.setExerciseNote(sessions.open().first()!!.exercises.single().id, "today")
        assertNull(sessions.previousNote(bench, open).first())
    }

    @Test
    fun anExerciseTwiceLastTimeGivesItsFirstNote() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(2, bench, listOf("", "second"))
        val open = sessions.start("Pass", 3)
        assertEquals("second", sessions.previousNote(bench, open).first())
    }

    @Test
    fun anExerciseAddedFromLastTimeTakesItsSetsUnlogged() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(1, bench, sets = List(2) { WorkoutSet(0, 70.0, 5) })
        sessions.finished(
            2, bench,
            sets = listOf(
                WorkoutSet(0, 40.0, 10, kind = SetKind.Warmup, rest = 90),
                WorkoutSet(0, 80.0, 8, rir = 2, rest = 120),
                WorkoutSet(0, 60.0, 10, rir = 1, kind = SetKind.Drop, rest = 120),
            ),
        )
        val open = sessions.start("Pass", 3)
        sessions.add(open, listOf(bench), lastTime = true)
        val ids = sessions.open().first()!!.exercises.single().sets.map { it.id }
        assertEquals(
            listOf(
                WorkoutSet(ids[0], 40.0, 10, kind = SetKind.Warmup, rest = 90),
                WorkoutSet(ids[1], 80.0, 8, rest = 120),
                WorkoutSet(ids[2], 60.0, 10, kind = SetKind.Drop, rest = 120),
            ),
            sessions.open().first()!!.exercises.single().sets,
        )
    }

    @Test
    fun anExerciseNeverDoneIsAddedWithOneEmptySet() = runTest {
        val (db, sessions) = sessions()
        sessions.finished(1, db.exercise("Bench Press BB"))
        val open = sessions.start("Pass", 3)
        sessions.add(open, listOf(db.exercise("Squat BB")), lastTime = true)
        val set = sessions.open().first()!!.exercises.single().sets.single()
        assertEquals(WorkoutSet(set.id), set)
    }

    @Test
    fun withoutLastTimeAnExerciseIsAddedWithOneEmptySet() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(1, bench)
        val open = sessions.start("Pass", 3)
        sessions.add(open, listOf(bench))
        val set = sessions.open().first()!!.exercises.single().sets.single()
        assertEquals(WorkoutSet(set.id), set)
    }

    @Test
    fun aWhitespaceNoteLastTimeGivesNoPreviousNote() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(1, bench, listOf("old"))
        sessions.finished(2, bench, listOf(" \n\t\r"))
        val open = sessions.start("Pass", 3)
        assertNull(sessions.previousNote(bench, open).first())
    }

    @Test
    fun anExerciseSkippedLastTimeTakesTheSetsOfTheTimeBefore() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        sessions.finished(1, bench, sets = List(2) { WorkoutSet(0, 70.0, 5) })
        val skipped = sessions.start("Pass", 2)
        sessions.add(skipped, listOf(bench))
        sessions.skip(sessions.open().first()!!.exercises.single().id, true)
        sessions.finish(skipped, Finish.WithoutThem, now = 2)
        val open = sessions.start("Pass", 3)
        sessions.add(open, listOf(bench), lastTime = true)
        assertEquals(List(2) { 70.0 to 5 }, sessions.open().first()!!.exercises.single().sets.map { it.weight to it.reps })
    }

    @Test
    fun anExerciseTwiceLastTimeGivesTheSetsOfItsFirst() = runTest {
        val (db, sessions) = sessions()
        val bench = db.exercise("Bench Press BB")
        val last = sessions.start("Pass", 2)
        sessions.add(last, listOf(bench, bench))
        val (first, second) = sessions.open().first()!!.exercises
        sessions.saveSet(first.sets.single().copy(weight = 70.0, reps = 5, done = true))
        sessions.saveSet(second.sets.single().copy(weight = 90.0, reps = 3, done = true))
        sessions.finish(last, Finish.WithoutThem, now = 2)
        val open = sessions.start("Pass", 3)
        sessions.add(open, listOf(bench), lastTime = true)
        assertEquals(listOf(70.0 to 5), sessions.open().first()!!.exercises.single().sets.map { it.weight to it.reps })
    }

    @Test
    fun anotherWorkoutCarriesTheExercisesUnit() = runTest {
        val (db, sessions) = sessions()
        val curl = db.exercise("Curl DB")
        SqlExercises(db).setSettings(curl, step = 2.0, bar = null, unit = WeightUnit.Lb)
        val first = sessions.start("Pass", 0)
        sessions.add(first, listOf(curl))
        assertEquals(WeightUnit.Lb, sessions.open().first()!!.exercises.single().unit)

        sessions.finish(first, Finish.WithoutThem, now = 1)
        sessions.add(sessions.start("Pass", 2), listOf(curl))
        assertEquals(WeightUnit.Lb, sessions.open().first()!!.exercises.single().unit)
    }
}
class SessionChangesTest {
    private fun StriveDatabase.exercise(name: String): Long {
        exerciseQueries.insertSeed(name, "", "chest", "", "Horisontell press", "Skivstång", "weight_reps", false, 2.5, 20.0, "strength", "", "", "", "")
        return exerciseQueries.all().executeAsList().first { it.name == name }.id
    }

    private val db = StriveDatabase(memoryDriver())
    private val sessions = SqlSessions(db)

    @Test
    fun aSetKeepsItsRestAndANewSetCopiesItButIsAWorkingSet() = runTest {
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        val bench = sessions.open().first()!!.exercises.single()
        sessions.saveSet(bench.sets.single().copy(weight = 40.0, reps = 10, rest = 90, done = true))
        sessions.setKind(bench.sets.single().id, SetKind.Warmup)
        sessions.addSet(bench.id)
        val sets = sessions.open().first()!!.exercises.single().sets
        assertEquals(listOf(SetKind.Warmup, SetKind.Normal), sets.map { it.kind })
        assertEquals(listOf(90, 90), sets.map { it.rest })
    }

    @Test
    fun anUnloggedSetMadeAWarmUpIsStoredFirstInLine() = runTest {
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        val e = sessions.open().first()!!.exercises.single()
        sessions.saveSet(e.sets.single().copy(weight = 80.0, reps = 8, done = true))
        sessions.addSet(e.id)
        sessions.addSet(e.id)
        val third = sessions.open().first()!!.exercises.single().sets[2].id
        sessions.setKind(third, SetKind.Warmup)
        val sets = sessions.open().first()!!.exercises.single().sets
        assertEquals(third, sets[1].id)
        assertEquals(SetKind.Warmup, sets[1].kind)
    }

    @Test
    fun restForAllSetsStartTimeSwapOrderAndStickyNoteAreStored() = runTest {
        val id = sessions.start("Pass", 0)
        val incline = db.exercise("Incline Press DB")
        sessions.add(id, listOf(db.exercise("Bench Press BB"), db.exercise("Squat BB")))
        val (bench, squat) = sessions.open().first()!!.exercises
        sessions.addSet(bench.id)
        sessions.setRestAll(bench.id, 150)
        sessions.setStart(id, 5_000)
        sessions.reorder(listOf(squat.id, bench.id))
        sessions.swap(bench.id, incline)
        SqlExercises(db).setNote(incline, "Bench at 30°")
        val w = sessions.open().first()!!
        assertEquals(5_000, w.startedAt)
        assertEquals(listOf("Squat BB", "Incline Press DB"), w.exercises.map { it.name })
        assertEquals(listOf(150, 150), w.exercises[1].sets.map { it.rest })
        assertEquals("Bench at 30°", w.exercises[1].pinned)
    }

    @Test
    fun aSkippedExerciseLeavesTheCountAndStaysRecordedWithoutItsSets() = runTest {
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB"), db.exercise("Squat BB")))
        val (bench, squat) = sessions.open().first()!!.exercises
        sessions.saveSet(bench.sets.single().copy(weight = 80.0, reps = 8, done = true))
        sessions.skip(squat.id, true)
        assertEquals(1, sessions.open().first()!!.sets)
        sessions.finish(id, Finish.WithoutThem, now = 1)
        val done = sessions.history().first().single()
        assertEquals(listOf("Bench Press BB", "Squat BB"), done.exercises.map { it.name })
        assertTrue(done.exercises[1].skipped)
        assertEquals(emptyList(), done.exercises[1].sets)
    }

    private suspend fun exercises() = sessions.open().first()!!.exercises

    @Test
    fun supersetGroupsThePickedExercisesAndRemovingOneLeavesNoSupersetOfOne() = runTest {
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Squat BB")))
        sessions.add(id, listOf(db.exercise("Incline Press DB"), db.exercise("Lateral Raise DB")), superset = true)
        val (squat, incline, lateral) = exercises()
        assertEquals(null, squat.superset)
        assertTrue(incline.superset != null && incline.superset == lateral.superset)
        sessions.removeExercise(incline.id)
        assertEquals(listOf(null, null), exercises().map { it.superset })
    }

    @Test
    fun theMenuJoinsAnExerciseToASupersetAndLeavingDissolvesIt() = runTest {
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB"), db.exercise("Squat BB"), db.exercise("Incline Press DB")))
        val (bench, squat, incline) = exercises()
        sessions.superset(bench.id, incline.id)
        // Incline Press moves up to follow Bench Press, the superset's last exercise.
        assertEquals(listOf(bench.id, incline.id, squat.id), exercises().map { it.id })
        assertTrue(exercises()[0].superset != null && exercises()[0].superset == exercises()[1].superset)
        sessions.leaveSuperset(incline.id)
        assertEquals(listOf(null, null, null), exercises().map { it.superset })
    }

    @Test
    fun movingASupersetApartOrFinishingWithoutOneOfItsExercisesDissolvesIt() = runTest {
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(db.exercise("Bench Press BB")))
        sessions.add(id, listOf(db.exercise("Incline Press DB"), db.exercise("Lateral Raise DB")), superset = true)
        val (bench, incline, lateral) = exercises()
        sessions.reorder(listOf(incline.id, bench.id, lateral.id))
        assertEquals(listOf(null, null, null), exercises().map { it.superset })

        sessions.reorder(listOf(bench.id, incline.id, lateral.id))
        sessions.superset(incline.id, lateral.id)
        sessions.saveSet(exercises()[1].sets.single().copy(weight = 26.0, reps = 10, done = true))
        // Lateral Raise has nothing logged, so finishing drops it; Incline Press is left alone, not a superset of one.
        sessions.finish(id, Finish.WithoutThem, now = 1)
        assertEquals(listOf(null), sessions.history().first().single().exercises.filter { it.name == "Incline Press DB" }.map { it.superset })
    }

    @Test
    fun theExercisesStepAndBarAreCarlsOnceChanged() = runTest {
        val bench = db.exercise("Bench Press BB")
        SqlExercises(db).setSettings(bench, step = 1.25, bar = 15.0, unit = null)
        val e = SqlExercises(db).byId(bench).first()!!
        assertEquals(1.25 to 15.0, e.step to e.bar)
    }
}
