package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

/** The twelve library sections, in the order the library lists them (#17 Q34). */
internal enum class Muscle(val key: String) {
    Chest("chest"), Back("back"), Shoulders("shoulders"), Biceps("biceps"), Triceps("triceps"), Forearms("forearms"),
    Quads("quads"), Hamstrings("hamstrings"), Glutes("glutes"), Calves("calves"), Core("core"), FullBody("full_body");

    companion object {
        fun of(key: String) = entries.first { it.key == key }
    }
}

/** What a set of the exercise records (#17 Q2). */
internal enum class ExerciseType(val key: String) {
    WeightReps("weight_reps"), BodyweightReps("bodyweight_reps"), Reps("reps"), Distance("distance"), Time("time");

    companion object {
        fun of(key: String) = entries.first { it.key == key }
    }
}

/** Mobility and conditioning stay out of the muscle sections, under their own chips (decision 36). */
internal enum class Category(val key: String) {
    Strength("strength"), Mobility("mobility"), Conditioning("conditioning");

    companion object {
        fun of(key: String) = entries.first { it.key == key }
    }
}

/** "I mina program": the coach's YES / SUB / TEST / NO from Carl's exercise library. */
internal enum class Mark { Yes, Sub, Test, No }

internal data class Exercise(
    val id: Long,
    val name: String,
    val aliases: List<String>,
    val primary: Muscle,
    val secondary: List<Muscle>,
    val pattern: String,
    val equipment: String,
    val type: ExerciseType,
    val perSide: Boolean,
    val step: Double?,
    val bar: Double?,
    val category: Category,
    val stepsEn: String,
    val tipsEn: String,
    val stepsSv: String,
    val tipsSv: String,
    val mark: Mark?,
    val note: String?,
)

internal interface Exercises {
    /** Every exercise not archived, sent again whenever one changes. */
    fun all(): Flow<List<Exercise>>

    fun byId(id: Long): Flow<Exercise?>

    suspend fun setMark(id: Long, mark: Mark?)
}

/** One row of `files/exercises.json`; keys the app does not use (the image prompt, review state) are ignored. */
@Serializable
internal data class SeedExercise(
    val name: String,
    val aliases: String = "",
    @SerialName("primary_muscle") val primaryMuscle: String,
    @SerialName("secondary_muscles") val secondaryMuscles: List<String>,
    val pattern: String,
    val equipment: String,
    val type: String,
    @SerialName("per_side") val perSide: Boolean,
    val step: Double? = null,
    val bar: Double? = null,
    val category: String,
    @SerialName("steps_en") val stepsEn: String,
    @SerialName("tips_en") val tipsEn: String,
    @SerialName("steps_sv") val stepsSv: String,
    @SerialName("tips_sv") val tipsSv: String,
)

internal class SqlExercises(private val database: StriveDatabase, private val io: CoroutineContext = Dispatchers.IO) : Exercises {
    private val queries = database.exerciseQueries

    override fun all() = queries.all(::exercise).asFlow().mapToList(io)

    override fun byId(id: Long) = queries.byId(id, ::exercise).asFlow().mapToOneOrNull(io)

    override suspend fun setMark(id: Long, mark: Mark?) {
        withContext(io) { queries.setMark(mark?.name, id) }
    }

    suspend fun seed(rows: List<SeedExercise>) = withContext(io) {
        database.transaction {
            for (r in rows) {
                val secondary = r.secondaryMuscles.joinToString(",")
                queries.insertSeed(r.name, r.aliases, r.primaryMuscle, secondary, r.pattern, r.equipment, r.type, r.perSide, r.step, r.bar,
                    r.category, r.stepsEn, r.tipsEn, r.stepsSv, r.tipsSv)
                queries.updateSeed(r.aliases, r.primaryMuscle, secondary, r.pattern, r.equipment, r.type, r.perSide, r.step, r.bar,
                    r.category, r.stepsEn, r.tipsEn, r.stepsSv, r.tipsSv, r.name)
            }
        }
    }

    @Suppress("LongParameterList")
    private fun exercise(
        id: Long, name: String, aliases: String, primary: String, secondary: String, pattern: String, equipment: String, type: String,
        perSide: Boolean, step: Double?, bar: Double?, category: String, stepsEn: String, tipsEn: String, stepsSv: String,
        tipsSv: String, mark: String?, note: String?, @Suppress("UNUSED_PARAMETER") edited: Boolean,
        @Suppress("UNUSED_PARAMETER") archived: Boolean,
    ) = Exercise(
        id, name, aliases.split("|").map { it.trim() }.filter { it.isNotEmpty() }, Muscle.of(primary),
        secondary.split(",").filter { it.isNotEmpty() }.map(Muscle::of), pattern, equipment, ExerciseType.of(type), perSide, step, bar,
        Category.of(category), stepsEn, tipsEn, stepsSv, tipsSv, mark?.let(Mark::valueOf), note,
    )
}

private val json = Json { ignoreUnknownKeys = true }

/** Seeds or updates the library when [version] is newer than the one this database last took (#41). */
internal suspend fun seedExercises(seedJson: String, version: Int, exercises: SqlExercises, settings: Settings) {
    val taken = settings.get(SEED_VERSION).first()?.toIntOrNull() ?: 0
    if (taken >= version) return
    exercises.seed(json.decodeFromString<List<SeedExercise>>(seedJson))
    settings.put(SEED_VERSION, version.toString())
}

private const val SEED_VERSION = "exercise_seed_version"