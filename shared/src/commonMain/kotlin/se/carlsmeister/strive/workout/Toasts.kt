package se.carlsmeister.strive.workout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * The workout screen's one toast and its hold (P4, P5). A removed exercise is only hidden while its
 * toast shows; its rows are deleted when the toast ends, is replaced, or is settled, so Undo and a
 * killed app both bring it back.
 */
internal class WorkoutToasts(
    private val scope: CoroutineScope,
    private val remove: suspend (Long) -> Unit,
    private val hold: () -> Long,
) {
    private val toast = MutableStateFlow<WorkoutToast?>(null)
    val shown: StateFlow<WorkoutToast?> = toast.asStateFlow()

    private val hiddenIds = MutableStateFlow(emptySet<Long>())
    /** The session exercises waiting on Undo. */
    val hidden: StateFlow<Set<Long>> = hiddenIds.asStateFlow()

    // The hold ends on a worker thread while the UI calls show and undo, so whoever clears the pending
    // id atomically owns it: the hold removes it, Undo un-hides it, never both.
    private val pending = MutableStateFlow<Long?>(null)
    private var job: Job? = null

    /** Ends the current toast first, removing a pending exercise, then shows [next] for the hold. */
    fun show(next: WorkoutToast) {
        job?.cancel()
        pending.getAndUpdate { null }?.let { id -> scope.launch { drop(id) } }
        if (next is WorkoutToast.Removed) {
            hiddenIds.update { it + next.sessionExerciseId }
            pending.value = next.sessionExerciseId
        }
        toast.value = next
        val millis = hold()
        job = scope.launch {
            delay(millis)
            toast.compareAndSet(next, null)
            // Launched apart from the hold, so a show that cancels the hold cannot cancel a removal already claimed.
            if (next is WorkoutToast.Removed && pending.compareAndSet(next.sessionExerciseId, null)) scope.launch { drop(next.sessionExerciseId) }
        }
    }

    /** Clears the toast and shows the exercise again, without removing anything. */
    fun undo() {
        job?.cancel()
        toast.value = null
        pending.getAndUpdate { null }?.let { id -> hiddenIds.update { it - id } }
    }

    /** Clears a toast that has no pending removal; one that has waits for its hold, Undo or [settle]. */
    fun dismiss() {
        if (pending.value != null) return
        job?.cancel()
        toast.value = null
    }

    /** Removes a pending exercise now and clears the toast, before the workout is finished or discarded. */
    suspend fun settle() {
        job?.cancel()
        toast.value = null
        pending.getAndUpdate { null }?.let { drop(it) }
    }

    private suspend fun drop(id: Long) {
        remove(id)
        hiddenIds.update { it - id }
    }
}
