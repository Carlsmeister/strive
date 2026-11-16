package se.carlsmeister.strive.workout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the workout screen's toast says. */
internal sealed interface WorkoutToast {
    data object Restored : WorkoutToast
    data class Removed(val name: String, val sessionExerciseId: Long) : WorkoutToast
    data class Swapped(val name: String) : WorkoutToast
}

/**
 * How long [toast] holds: [millis], except a Removed toast while a screen reader runs, which holds until Undo, another toast or
 * `settle`. VoiceOver cannot reach Undo within a few seconds, and the removal deletes logged sets for good.
 */
internal fun holdFor(toast: WorkoutToast, millis: Long, screenReader: Boolean) =
    if (screenReader && toast is WorkoutToast.Removed) Long.MAX_VALUE else millis

/** Whether VoiceOver (iOS) or TalkBack (Android) is running, read as a toast is shown. */
internal expect fun screenReaderRunning(): Boolean

/**
 * The workout screen's one toast and its hold (P4, P5). A removed exercise is only hidden while its
 * toast shows; its rows are deleted when the toast ends, is replaced, or is settled, so Undo and a
 * killed app both bring it back.
 */
internal class WorkoutToasts(
    private val scope: CoroutineScope,
    private val remove: suspend (Long) -> Unit,
    private val hold: (WorkoutToast) -> Long,
) {
    private val toast = MutableStateFlow<WorkoutToast?>(null)
    val shown: StateFlow<WorkoutToast?> = toast.asStateFlow()

    private val hiddenIds = MutableStateFlow(emptySet<Long>())
    /** The session exercises waiting on Undo. */
    val hidden: StateFlow<Set<Long>> = hiddenIds.asStateFlow()

    // The hold ends on a worker thread while the UI calls show and undo, so whoever clears the pending
    // slot atomically owns it: the hold removes it, Undo un-hides it, never both. The slot carries its
    // show's token, so a stale hold never matches a newer show of the same exercise.
    private data class Pending(val token: Long, val id: Long)
    private val pending = MutableStateFlow<Pending?>(null)
    private val current = MutableStateFlow(0L)
    private var job: Job? = null
    private val removals = MutableStateFlow(emptyList<Job>())

    /** Ends the current toast first, removing a pending exercise, then shows [next] for the hold. */
    fun show(next: WorkoutToast) {
        job?.cancel()
        val token = current.value + 1
        current.value = token
        pending.getAndUpdate { null }?.let { launchDrop(it.id) }
        if (next is WorkoutToast.Removed) {
            hiddenIds.update { it + next.sessionExerciseId }
            pending.value = Pending(token, next.sessionExerciseId)
        }
        toast.value = next
        val millis = hold(next)
        job = scope.launch {
            delay(millis)
            // shortcut: check-then-set, so a show in the instants between can still lose an equal toast; harmless, as only the toast's timing is at stake.
            if (current.value == token) toast.compareAndSet(next, null)
            if (next is WorkoutToast.Removed && pending.compareAndSet(Pending(token, next.sessionExerciseId), null)) launchDrop(next.sessionExerciseId)
        }
    }

    /** Clears the toast and shows the exercise again, without removing anything. */
    fun undo() {
        job?.cancel()
        toast.value = null
        pending.getAndUpdate { null }?.let { p -> hiddenIds.update { it - p.id } }
    }

    /** Clears a toast that has no pending removal; one that has waits for its hold, Undo or [settle]. */
    fun dismiss() {
        if (pending.value != null) return
        job?.cancel()
        toast.value = null
    }

    /**
     * Removes a pending exercise now, clears the toast, and returns only once every removal already
     * started has finished too, so a finish or discard that follows never sees a removed exercise.
     */
    suspend fun settle() {
        job?.cancelAndJoin() // a hold past its delay finishes, so a removal it claimed is in [removals]
        toast.value = null
        pending.getAndUpdate { null }?.let { drop(it.id) }
        removals.getAndUpdate { emptyList() }.joinAll()
    }

    // Launched apart from the hold, so a show that cancels the hold cannot cancel a removal already claimed.
    private fun launchDrop(id: Long) {
        val removal = scope.launch { drop(id) }
        removals.update { it + removal }
    }

    private suspend fun drop(id: Long) {
        remove(id)
        hiddenIds.update { it - id }
    }
}
