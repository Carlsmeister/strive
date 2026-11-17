package se.carlsmeister.strive.fixture

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.memoryDriver
import se.carlsmeister.strive.workout.clock
import se.carlsmeister.strive.workout.now
import se.carlsmeister.strive.workout.previous
import se.carlsmeister.strive.workout.wallClock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Instant

class SampleDataTest {
    private val graph = AppGraph(memoryDriver())

    /** A test that left the clock pinned fails the next one here, in whatever order the runner takes them (Review Focus 2). */
    @BeforeTest
    fun theClockStartsReleased() = check(wallClock === Clock.System) { "a test left the clock pinned" }

    @AfterTest
    fun releaseTheClock() { wallClock = Clock.System }

    private fun Long.at() = Instant.fromEpochMilliseconds(this).toLocalDateTime(SampleData.zone)

    @Test
    fun todaysPushAIsInProgressAt2413WithOneOf14Sets() = runTest {
        val id = SampleData.load(graph)
        val w = graph.sessions.open().first()!!
        assertEquals(id to "Push A", w.id to w.name)
        assertEquals(LocalDateTime(2026, 11, 9, 17, 2), w.startedAt.at())
        assertEquals("24:13", clock((now() - w.startedAt) / 1_000))
        assertEquals(1 to 14, w.countedLogged to w.countedSets)
        assertEquals("Arch, feet planted.", w.exercises.first().pinned)
    }

    @Test
    fun theFinishedPushAOfMonday2NovemberIsLastTimesSets() = runTest {
        SampleData.load(graph)
        val last = graph.sessions.history().first().single()
        assertEquals(LocalDateTime(2026, 11, 2, 17, 5) to LocalDateTime(2026, 11, 2, 18, 3), last.startedAt.at() to last.endedAt!!.at())
        assertEquals(listOf("Bench Press BB", "Incline Press DB", "Lateral Raise DB", "Tricep Dips", "Tricep Pushdown"), last.exercises.map { it.name })
        assertEquals(last.exercises[1].superset, last.exercises[2].superset)
        assertNotNull(last.exercises[1].superset)
        val bench = graph.exercises.all().first().first { it.name == "Bench Press BB" }
        assertEquals(
            listOf(Triple(40.0, 10, null), Triple(77.5, 8, 3), Triple(80.0, 8, 3), Triple(80.0, 7, 2)),
            previous(listOf(last), bench.id).map { Triple(it.weight, it.reps, it.rir) },
        )
        assertEquals(SetKind.Warmup, previous(listOf(last), bench.id).first().kind)
        assertEquals(listOf(SetKind.Normal, SetKind.Failure), last.exercises[3].sets.map { it.kind })
        assertEquals(SetKind.Drop, last.exercises[4].sets.last().kind)
        assertEquals(15 to 15, last.logged to last.sets) // every set logged, the warm-up included (R8)
        assertEquals("Kändes stark idag." to 7, last.note to last.rpe)
    }

    @Test
    fun lastMondaysBenchCarriesItsNote() = runTest {
        val today = SampleData.load(graph)
        val bench = graph.exercises.all().first().first { it.name == "Bench Press BB" }
        assertEquals("Kändes stark idag.", graph.sessions.previousNote(bench.id, today).first())
    }

    @Test
    fun carlsExerciseMarksReadBack() = runTest {
        SampleData.load(graph)
        val marks = graph.exercises.all().first().filter { it.mark != null }.associate { it.name to it.mark }
        assertEquals(
            mapOf(
                "Bench Press BB" to Mark.Yes, "Squat BB" to Mark.Yes, "Deadlift BB" to Mark.Yes, "Overhead Press BB" to Mark.Yes,
                "Pull Up" to Mark.Yes, "Incline Press DB" to Mark.Yes, "Lateral Raise DB" to Mark.Yes, "Romanian Deadlift DB" to Mark.Yes,
                "Seated Row Cable" to Mark.Sub, "Bulgarian Split Squat DB" to Mark.Test, "Leg Extension Machine" to Mark.No,
            ),
            marks,
        )
    }
}
