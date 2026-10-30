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
}