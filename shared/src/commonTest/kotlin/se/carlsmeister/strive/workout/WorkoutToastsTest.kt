package se.carlsmeister.strive.workout

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.carlsmeister.strive.data.SqlSessions
import se.carlsmeister.strive.data.memoryDriver
import se.carlsmeister.strive.db.StriveDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class) // runCurrent and advanceTimeBy drive the hold
class WorkoutToastsTest {
    private class Setup(val sessions: SqlSessions, val toasts: WorkoutToasts, val benchSe: Long, val pullSe: Long)

    /** A workout holding Bench (two logged sets) and Pull Up, on the test's one scheduler. */
    private suspend fun TestScope.setup(hold: (WorkoutToast) -> Long = { 4_000 }): Setup {
        val db = StriveDatabase(memoryDriver())
        val sessions = SqlSessions(db, StandardTestDispatcher(testScheduler))
        fun exercise(name: String, type: String): Long {
            db.exerciseQueries.insertSeed(name, "", "chest", "", "", "", type, false, 2.5, 20.0, "strength", "", "", "", "")
            return db.exerciseQueries.all().executeAsList().first { it.name == name }.id
        }
        val id = sessions.start("Pass", 0)
        sessions.add(id, listOf(exercise("Bench Press BB", "weight_reps"), exercise("Pull Up", "bodyweight_reps")))
        val (bench, pull) = sessions.open().first()!!.exercises
        sessions.saveSet(bench.sets.single().copy(weight = 80.0, reps = 8, done = true))
        sessions.addSet(bench.id)
        sessions.saveSet(sessions.open().first()!!.exercises.first().sets[1].copy(done = true))
        return Setup(sessions, WorkoutToasts(backgroundScope, sessions::removeExercise, hold), bench.id, pull.id)
    }

    private suspend fun TestScope.names(s: Setup): List<String> {
        runCurrent()
        return s.sessions.open().first()!!.exercises.map { it.name }
    }

    @Test
    fun undoBeforeTheToastEndsKeepsTheExerciseAndItsSets() = runTest {
        val s = setup()
        s.toasts.show(WorkoutToast.Removed("Bench Press BB", s.benchSe))
        runCurrent()
        assertEquals(setOf(s.benchSe), s.toasts.hidden.value)

        advanceTimeBy(3_000)
        s.toasts.undo()
        advanceTimeBy(10_000)
        runCurrent()

        val bench = s.sessions.open().first()!!.exercises.first()
        assertEquals("Bench Press BB", bench.name)
        assertEquals(listOf(true, true), bench.sets.map { it.done })
        assertEquals(emptySet(), s.toasts.hidden.value)
        assertNull(s.toasts.shown.value)
    }

    @Test
    fun whenTheToastEndsTheExerciseIsRemoved() = runTest {
        val s = setup()
        s.toasts.show(WorkoutToast.Removed("Bench Press BB", s.benchSe))
        advanceTimeBy(4_001)

        assertEquals(listOf("Pull Up"), names(s))
        assertNull(s.toasts.shown.value)
        assertEquals(emptySet(), s.toasts.hidden.value)
    }

    @Test
    fun aSecondToastRemovesTheFirstAtOnce() = runTest {
        val s = setup()
        s.toasts.show(WorkoutToast.Removed("Bench Press BB", s.benchSe))
        advanceTimeBy(1_000)
        s.toasts.show(WorkoutToast.Removed("Pull Up", s.pullSe))

        assertEquals(listOf("Pull Up"), names(s))
        assertEquals(WorkoutToast.Removed("Pull Up", s.pullSe), s.toasts.shown.value)
        assertEquals(setOf(s.pullSe), s.toasts.hidden.value)
    }

    @Test
    fun settleRemovesAPendingRemovalAtOnce() = runTest {
        val s = setup()
        s.toasts.show(WorkoutToast.Removed("Bench Press BB", s.benchSe))
        s.toasts.settle()

        assertEquals(listOf("Pull Up"), names(s))
        assertNull(s.toasts.shown.value)
        assertEquals(emptySet(), s.toasts.hidden.value)
    }

    @Test
    fun aToastEndsByItselfAfterItsHold() = runTest {
        val s = setup()
        s.toasts.show(WorkoutToast.Restored)
        runCurrent()
        assertEquals(WorkoutToast.Restored, s.toasts.shown.value)

        advanceTimeBy(4_001)
        runCurrent()

        assertNull(s.toasts.shown.value)
        assertEquals(listOf("Bench Press BB", "Pull Up"), names(s))
    }

    /** A remove that waits until its gate opens, so a test can hold a removal in flight. */
    private class Gated {
        val gates = mutableMapOf<Long, CompletableDeferred<Unit>>()
        val removed = mutableListOf<Long>()
        suspend fun remove(id: Long) {
            gates.getOrPut(id) { CompletableDeferred() }.await()
            removed += id
        }
        fun open(id: Long) = gates.getOrPut(id) { CompletableDeferred() }.complete(Unit)
    }

    @Test
    fun settleWaitsForARemovalAnEarlierToastStarted() = runTest {
        val gated = Gated()
        val toasts = WorkoutToasts(backgroundScope, gated::remove) { 4_000 }
        toasts.show(WorkoutToast.Removed("A", 1))
        toasts.show(WorkoutToast.Removed("B", 2)) // starts A's removal, which waits on its gate
        val settling = launch { toasts.settle() }
        runCurrent()

        gated.open(2)
        runCurrent()
        assertFalse(settling.isCompleted, "settle returned before A's removal finished")

        gated.open(1)
        runCurrent()
        assertTrue(settling.isCompleted)
        assertEquals(setOf(1L, 2L), gated.removed.toSet())
        assertEquals(emptySet(), toasts.hidden.value)
    }

    @Test
    fun settleWaitsForARemovalTheHoldStarted() = runTest {
        val gated = Gated()
        val toasts = WorkoutToasts(backgroundScope, gated::remove) { 4_000 }
        toasts.show(WorkoutToast.Removed("A", 1))
        advanceTimeBy(4_001) // the hold ends and starts A's removal
        val settling = launch { toasts.settle() }
        runCurrent()
        assertFalse(settling.isCompleted, "settle returned before the hold's removal finished")

        gated.open(1)
        runCurrent()
        assertTrue(settling.isCompleted)
        assertEquals(listOf(1L), gated.removed)
    }

    /** With VoiceOver on, a Removed toast holds until acted on, past any hold, while Restored and Swapped keep theirs (#42 review). */
    @Test
    fun withAScreenReaderARemovedToastHoldsUntilUndo() = runTest {
        val s = setup { holdFor(it, 4_000, screenReader = true) }
        val removed = WorkoutToast.Removed("Bench Press BB", s.benchSe)
        s.toasts.show(removed)
        advanceTimeBy(10_001)
        assertEquals(removed, s.toasts.shown.value)
        assertEquals(setOf(s.benchSe), s.toasts.hidden.value)
        assertEquals(listOf("Bench Press BB", "Pull Up"), names(s))

        s.toasts.undo()
        runCurrent()
        assertNull(s.toasts.shown.value)
        assertTrue(s.toasts.hidden.value.isEmpty())

        s.toasts.show(WorkoutToast.Swapped("Chest Press Machine"))
        advanceTimeBy(4_001)
        assertNull(s.toasts.shown.value)
        assertEquals(4_000, holdFor(removed, 4_000, screenReader = false))
    }

    @Test
    fun eachToastHoldsForTheHoldReadWhenItIsShown() = runTest {
        var hold = 1_000L
        val toasts = WorkoutToasts(backgroundScope, {}) { hold }
        toasts.show(WorkoutToast.Restored)
        advanceTimeBy(1_001)
        assertNull(toasts.shown.value)

        hold = 5_000
        toasts.show(WorkoutToast.Swapped("Chest Press Machine"))
        advanceTimeBy(1_001)
        assertEquals(WorkoutToast.Swapped("Chest Press Machine"), toasts.shown.value)
        advanceTimeBy(4_000)
        assertNull(toasts.shown.value)
    }
}
