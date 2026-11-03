package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal data class WorkoutSet(
    val id: Long,
    val weight: Double? = null,
    val reps: Int? = null,
    val seconds: Int? = null,
    val metres: Double? = null,
    val rir: Int? = null,
    val done: Boolean = false,
    val kind: SetKind = SetKind.Normal,
    /** Seconds of rest after the set; null for the default in Settings (Q10). */
    val rest: Int? = null,
)

/** A set's type (Q21): a working set, or a warm-up, drop or failure set, which show W, D or F in the badge. */
internal enum class SetKind(val key: String, val letter: String?) {
    Normal("normal", null), Warmup("warmup", "W"), Drop("drop", "D"), Failure("failure", "F");

    companion object {
        fun of(key: String) = entries.firstOrNull { it.key == key } ?: Normal
    }
}

/** A rest counting down, started when a set is logged or from the title bar's timer (Q17). Kept in memory only. */
internal data class Rest(val startedAt: Long, val seconds: Int) {
    // The screen's clock ticks on the second, so it can read a moment before the rest began: never show more than the rest.
    fun left(now: Long) = (seconds - ((now - startedAt) / 1_000).toInt()).coerceIn(0, seconds)
}

internal data class SessionExercise(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val type: ExerciseType,
    val step: Double?,
    val note: String,
    val sets: List<WorkoutSet>,
    /** The exercise's sticky note, shown every time it is done (Q15). */
    val pinned: String = "",
    val bar: Double? = null,
    /** Skipped in this workout: it stays recorded, but its sets are not counted (Q8). */
    val skipped: Boolean = false,
)

/** A workout with its exercises and sets, open while [endedAt] is null. Times are epoch milliseconds. */
internal data class Workout(
    val id: Long,
    val name: String,
    val startedAt: Long,
    val endedAt: Long?,
    val note: String,
    val rpe: Int?,
    val exercises: List<SessionExercise>,
) {
    val sets get() = exercises.filterNot { it.skipped }.sumOf { it.sets.size }
    val logged get() = exercises.filterNot { it.skipped }.sumOf { e -> e.sets.count { it.done } }
}

/** A set can be logged once the values its exercise's type records are there. */
internal fun WorkoutSet.loggable(type: ExerciseType) = when (type) {
    ExerciseType.WeightReps -> weight != null && reps != null
    ExerciseType.BodyweightReps, ExerciseType.Reps -> reps != null
    ExerciseType.Time -> seconds != null
    ExerciseType.Distance -> metres != null
}

/**
 * The order after a set is made a warm-up (#17 choice 4): unlogged, it moves up to be the next set to do, just before the first
 * set not logged; logged, it stays where it is.
 */
internal fun warmupOrder(sets: List<WorkoutSet>, id: Long): List<Long> {
    val set = sets.first { it.id == id }
    if (set.done) return sets.map { it.id }
    val rest = sets.filter { it.id != id }
    val at = rest.indexOfFirst { !it.done }.takeIf { it >= 0 } ?: rest.size
    return (rest.take(at) + set + rest.drop(at)).map { it.id }
}

/** Avsluta with sets not logged (Q20): log the ones that can be as they stand, or save without them. */
internal enum class Finish { LogAsTheyStand, WithoutThem }

internal interface Sessions {
    /** The workout in progress, if there is one; two cannot run at once (Q46). */
    fun open(): Flow<Workout?>

    fun workout(id: Long): Flow<Workout?>

    /** Finished workouts, newest first. */
    fun history(): Flow<List<Workout>>

    suspend fun start(name: String, now: Long): Long

    /** Each exercise joins the end of the workout with one empty set. */
    suspend fun add(sessionId: Long, exerciseIds: List<Long>)

    /** A copy of the exercise's last set and its rest, a working set, not logged and without its RIR (Q8). */
    suspend fun addSet(sessionExerciseId: Long)

    suspend fun saveSet(set: WorkoutSet)

    suspend fun deleteSet(id: Long)

    /** Changes a set's type; an unlogged set made a warm-up moves up to be the next set to do (#17 choice 4). */
    suspend fun setKind(id: Long, kind: SetKind)

    suspend fun setRestAll(sessionExerciseId: Long, seconds: Int)

    /** Puts the workout's exercises in this order. */
    suspend fun reorder(sessionExerciseIds: List<Long>)

    /** Swaps the exercise for another, keeping its sets (Q8). */
    suspend fun swap(sessionExerciseId: Long, exerciseId: Long)

    suspend fun skip(sessionExerciseId: Long, skipped: Boolean)

    suspend fun setStart(id: Long, startedAt: Long)

    suspend fun removeExercise(sessionExerciseId: Long)

    suspend fun setExerciseNote(sessionExerciseId: Long, note: String)

    suspend fun rename(id: Long, name: String)

    suspend fun setNote(id: Long, note: String)

    suspend fun setRpe(id: Long, rpe: Int?)

    /** Ends the workout. Sets not logged are dropped unless [how] logs them, and so are exercises left without a set (Q12). */
    suspend fun finish(id: Long, how: Finish, now: Long)

    /** Deletes the workout and everything in it. */
    suspend fun discard(id: Long)
}

internal class SqlSessions(private val database: StriveDatabase, private val io: CoroutineContext = Dispatchers.IO) : Sessions {
    private val queries = database.sessionQueries

    /** A write off the main thread; Unit, whatever the query returns. */
    private suspend fun write(block: () -> Unit) = withContext(io) { block() }

    override fun open() = queries.openRows(::Row).asFlow().mapToList(io).map { workouts(it).firstOrNull() }

    override fun workout(id: Long) = queries.rowsOf(id, ::Row).asFlow().mapToList(io).map { workouts(it).firstOrNull() }

    override fun history() = queries.finishedRows(::Row).asFlow().mapToList(io).map(::workouts)

    override suspend fun start(name: String, now: Long) = withContext(io) {
        queries.transactionWithResult {
            queries.insertSession(name, now)
            queries.lastId().executeAsOne()
        }
    }

    override suspend fun add(sessionId: Long, exerciseIds: List<Long>) = write {
        queries.transaction {
            for (exerciseId in exerciseIds) {
                queries.insertExercise(sessionId, exerciseId)
                queries.insertSet(queries.lastId().executeAsOne(), null, null, null, null, null)
            }
        }
    }

    override suspend fun addSet(sessionExerciseId: Long) = write {
        queries.transaction {
            val last = queries.lastSet(sessionExerciseId).executeAsOneOrNull()
            queries.insertSet(sessionExerciseId, last?.weight, last?.reps, last?.seconds, last?.metres, last?.rest_seconds)
        }
    }

    override suspend fun saveSet(set: WorkoutSet) = write {
        queries.updateSet(set.weight, set.reps?.toLong(), set.seconds?.toLong(), set.metres, set.rir?.toLong(), set.done, set.rest?.toLong(), set.id)
    }

    override suspend fun deleteSet(id: Long) = write { queries.deleteSet(id) }

    override suspend fun setKind(id: Long, kind: SetKind) = write {
        queries.transaction {
            queries.setKind(kind.key, id)
            if (kind == SetKind.Warmup) {
                val sets = queries.setsOf(id).executeAsList().map { WorkoutSet(it.id, done = it.done) }
                warmupOrder(sets, id).forEachIndexed { i, set -> queries.setPosition(i.toLong(), set) }
            }
        }
    }

    override suspend fun setRestAll(sessionExerciseId: Long, seconds: Int) = write { queries.setRestOf(seconds.toLong(), sessionExerciseId) }

    override suspend fun reorder(sessionExerciseIds: List<Long>) = write {
        queries.transaction { sessionExerciseIds.forEachIndexed { i, id -> queries.setExercisePosition(i.toLong(), id) } }
    }

    override suspend fun swap(sessionExerciseId: Long, exerciseId: Long) = write { queries.swapExercise(exerciseId, sessionExerciseId) }

    override suspend fun skip(sessionExerciseId: Long, skipped: Boolean) = write { queries.setSkipped(skipped, sessionExerciseId) }

    override suspend fun setStart(id: Long, startedAt: Long) = write { queries.setStartedAt(startedAt, id) }

    override suspend fun removeExercise(sessionExerciseId: Long) = write {
        queries.transaction {
            queries.deleteSetsOf(sessionExerciseId)
            queries.deleteExercise(sessionExerciseId)
        }
    }

    override suspend fun setExerciseNote(sessionExerciseId: Long, note: String) = write { queries.setExerciseNote(note, sessionExerciseId) }

    override suspend fun rename(id: Long, name: String) = write { queries.rename(name, id) }

    override suspend fun setNote(id: Long, note: String) = write { queries.setNote(note, id) }

    override suspend fun setRpe(id: Long, rpe: Int?) = write { queries.setRpe(rpe?.toLong(), id) }

    override suspend fun finish(id: Long, how: Finish, now: Long) = write {
        queries.transaction {
            val workout = workouts(queries.rowsOf(id, ::Row).executeAsList()).single()
            for (exercise in workout.exercises) {
                for (set in exercise.sets.filterNot { it.done }) {
                    if (how == Finish.LogAsTheyStand && !exercise.skipped && set.loggable(exercise.type)) {
                        queries.updateSet(set.weight, set.reps?.toLong(), set.seconds?.toLong(), set.metres, set.rir?.toLong(), true, set.rest?.toLong(), set.id)
                    } else {
                        queries.deleteSet(set.id)
                    }
                }
            }
            queries.deleteEmptyExercises(id)
            queries.finish(now, id)
        }
    }

    override suspend fun discard(id: Long) = write {
        queries.transaction {
            queries.deleteSetsOfSession(id)
            queries.deleteExercisesOfSession(id)
            queries.deleteSession(id)
        }
    }
}

/** One flat row of the workout queries: a set with its exercise and workout, or a workout or exercise without one. */
@Suppress("LongParameterList")
private class Row(
    val id: Long, val name: String, val startedAt: Long, val endedAt: Long?, val note: String, val rpe: Long?,
    val seId: Long?, val exerciseId: Long?, val seNote: String?, val skipped: Boolean?, val exerciseName: String?, val type: String?,
    val step: Double?, val bar: Double?, val pinned: String?,
    val setId: Long?, val weight: Double?, val reps: Long?, val seconds: Long?, val metres: Double?, val rir: Long?, val done: Boolean?,
    val kind: String?, val rest: Long?,
)

/** Groups the rows, already in workout, exercise and set order, back into workouts. */
private fun workouts(rows: List<Row>): List<Workout> = rows.groupBy { it.id }.values.map { workout ->
    val w = workout.first()
    val exercises = workout.filter { it.seId != null }.groupBy { it.seId!! }.values.map { exercise ->
        val e = exercise.first()
        SessionExercise(
            e.seId!!, e.exerciseId!!, e.exerciseName!!, ExerciseType.of(e.type!!), e.step, e.seNote!!,
            exercise.filter { it.setId != null }.map {
                WorkoutSet(it.setId!!, it.weight, it.reps?.toInt(), it.seconds?.toInt(), it.metres, it.rir?.toInt(), it.done!!, SetKind.of(it.kind!!),
                    it.rest?.toInt())
            },
            pinned = e.pinned.orEmpty(), bar = e.bar, skipped = e.skipped!!,
        )
    }
    Workout(w.id, w.name, w.startedAt, w.endedAt, w.note, w.rpe?.toInt(), exercises)
}
