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
)

internal data class SessionExercise(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val type: ExerciseType,
    val step: Double?,
    val note: String,
    val sets: List<WorkoutSet>,
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
    val sets get() = exercises.sumOf { it.sets.size }
    val logged get() = exercises.sumOf { e -> e.sets.count { it.done } }
}

/** A set can be logged once the values its exercise's type records are there. */
internal fun WorkoutSet.loggable(type: ExerciseType) = when (type) {
    ExerciseType.WeightReps -> weight != null && reps != null
    ExerciseType.BodyweightReps, ExerciseType.Reps -> reps != null
    ExerciseType.Time -> seconds != null
    ExerciseType.Distance -> metres != null
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

    /** A copy of the exercise's last set, not logged and without its RIR (Q8). */
    suspend fun addSet(sessionExerciseId: Long)

    suspend fun saveSet(set: WorkoutSet)

    suspend fun deleteSet(id: Long)

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
                queries.insertSet(queries.lastId().executeAsOne(), null, null, null, null)
            }
        }
    }

    override suspend fun addSet(sessionExerciseId: Long) = write {
        queries.transaction {
            val last = queries.lastSet(sessionExerciseId).executeAsOneOrNull()
            queries.insertSet(sessionExerciseId, last?.weight, last?.reps, last?.seconds, last?.metres)
        }
    }

    override suspend fun saveSet(set: WorkoutSet) = write {
        queries.updateSet(set.weight, set.reps?.toLong(), set.seconds?.toLong(), set.metres, set.rir?.toLong(), set.done, set.id)
    }

    override suspend fun deleteSet(id: Long) = write { queries.deleteSet(id) }

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
                    if (how == Finish.LogAsTheyStand && set.loggable(exercise.type)) {
                        queries.updateSet(set.weight, set.reps?.toLong(), set.seconds?.toLong(), set.metres, set.rir?.toLong(), true, set.id)
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
    val seId: Long?, val exerciseId: Long?, val seNote: String?, val exerciseName: String?, val type: String?, val step: Double?,
    val setId: Long?, val weight: Double?, val reps: Long?, val seconds: Long?, val metres: Double?, val rir: Long?, val done: Boolean?,
)

/** Groups the rows, already in workout, exercise and set order, back into workouts. */
private fun workouts(rows: List<Row>): List<Workout> = rows.groupBy { it.id }.values.map { workout ->
    val w = workout.first()
    val exercises = workout.filter { it.seId != null }.groupBy { it.seId!! }.values.map { exercise ->
        val e = exercise.first()
        SessionExercise(
            e.seId!!, e.exerciseId!!, e.exerciseName!!, ExerciseType.of(e.type!!), e.step, e.seNote!!,
            exercise.filter { it.setId != null }.map {
                WorkoutSet(it.setId!!, it.weight, it.reps?.toInt(), it.seconds?.toInt(), it.metres, it.rir?.toInt(), it.done!!)
            },
        )
    }
    Workout(w.id, w.name, w.startedAt, w.endedAt, w.note, w.rpe?.toInt(), exercises)
}
