package se.carlsmeister.strive.workout

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.library.kilos
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.time.Instant

/** A keypad field's text (#17 K3). A field just opened is fresh: the first key replaces what it shows. */
internal data class Field(val text: String, val fresh: Boolean = true) {
    val value: Double? get() = text.replace(',', '.').toDoubleOrNull()

    fun key(k: Char, decimals: Boolean, separator: Char): Field {
        val base = if (fresh) "" else text
        val next = when {
            k == '⌫' -> base.dropLast(1)
            k == separator -> when {
                !decimals || separator in base -> base
                base.isEmpty() -> "0$separator"
                else -> base + separator
            }
            !k.isDigit() || base.length >= MAX_LENGTH || base.substringAfter(separator, "").length >= 2 -> base
            base == "0" -> k.toString()
            else -> base + k
        }
        return Field(next, fresh = false)
    }

    fun step(by: Double, separator: Char): Field {
        val v = (max(0.0, (value ?: 0.0) + by) * 100).roundToLong() / 100.0
        return Field(kilos(v, swedish = separator == ','), fresh = false)
    }
}

private const val MAX_LENGTH = 6

/** The summary's numbers (#17 choice 7): time, logged sets, Snitt-RIR over the sets that have one, and volume for History. */
internal data class Summary(val minutes: Long, val sets: Int, val rir: Double?, val rirSets: Int, val volume: Double)

internal fun summary(w: Workout, now: Long): Summary {
    val logged = w.exercises.flatMap { e -> e.sets.filter { it.done }.map { e.type to it } }
    // Warm-ups say nothing about how hard the workout was, nor about the work done (#17 choice 4).
    val work = logged.filter { it.second.kind != SetKind.Warmup }
    val rirs = work.mapNotNull { it.second.rir }
    return Summary(
        minutes = ((w.endedAt ?: now) - w.startedAt) / 60_000,
        sets = logged.size,
        rir = if (rirs.isEmpty()) null else rirs.average(),
        rirSets = rirs.size,
        volume = work.filter { it.first == ExerciseType.WeightReps }.sumOf { (_, s) -> (s.weight ?: 0.0) * (s.reps ?: 0) },
    )
}

/** The exercise's best logged set: the heaviest, then the most reps; the longest for time and distance. */
internal fun best(e: SessionExercise): WorkoutSet? = e.sets.filter { it.done }.maxWithOrNull(
    compareBy<WorkoutSet>({ it.weight ?: 0.0 }, { it.reps ?: 0 }, { it.seconds ?: 0 }, { it.metres ?: 0.0 }),
)

/** Last time's sets of an exercise (Q21's Förra): the newest finished workout in [history], newest first, that has it. */
// shortcut: reads every finished workout; give it its own query once history is long enough to feel.
internal fun previous(history: List<Workout>, exerciseId: Long): List<WorkoutSet> =
    history.firstNotNullOfOrNull { w -> w.exercises.firstOrNull { it.exerciseId == exerciseId && it.sets.isNotEmpty() }?.sets }.orEmpty()

/**
 * How the planned weight differs from last time's ("upp 2,5 kg"), null when they are equal or either is missing. Until the progression
 * rule (#45) has template rep ranges to judge by, this is only the difference.
 */
internal fun change(planned: Double?, before: Double?): Double? =
    if (planned == null || before == null || planned == before) null else ((planned - before) * 100).roundToLong() / 100.0

/** Each set's badge (Q21): W or D, else its number among the working sets, failure sets among them (Q31). */
internal fun badges(sets: List<WorkoutSet>): List<String> {
    var n = 0
    return sets.map { if (it.kind.numbered) n++; it.kind.letter ?: "$n" }
}

/** How many working sets, failure sets among them: the "of 4" in "set 2 of 4". */
internal fun working(sets: List<WorkoutSet>) = sets.count { it.kind.numbered }

/** The exercises in blocks, in order: a superset's exercises together (#17 choice 6), every other exercise alone. */
internal fun blocks(exercises: List<SessionExercise>): List<List<SessionExercise>> {
    val out = mutableListOf<MutableList<SessionExercise>>()
    for (e in exercises) if (e.superset != null && out.lastOrNull()?.last()?.superset == e.superset) out.last() += e else out += mutableListOf(e)
    return out
}

/** A block's rounds in the order you lift (G2): each set alone for one exercise; set n of each exercise in turn for a superset. */
internal fun rounds(block: List<SessionExercise>): List<List<Pair<SessionExercise, Int>>> =
    if (block.size == 1) block[0].sets.indices.map { listOf(block[0] to it) }
    else (0 until block.maxOf { it.sets.size }).map { r -> block.filter { r < it.sets.size }.map { it to r } }

/** A superset's letters, A, B, … by exercise id (G2); an exercise alone has none. */
internal fun tags(exercises: List<SessionExercise>): Map<Long, String> =
    blocks(exercises).filter { it.size > 1 }.flatMap { b -> b.mapIndexed { n, e -> e.id to "${'A' + n}" } }.toMap()

/**
 * The set whose rest starts once set [id] is logged: the last of its round, once every set in the round is logged, in either order;
 * else none (choice 6). Alone, a set is its own round.
 */
internal fun restAfterLogging(exercises: List<SessionExercise>, id: Long): WorkoutSet? {
    val round = blocks(exercises).flatMap(::rounds).firstOrNull { r -> r.any { (e, i) -> e.sets[i].id == id } } ?: return null
    return if (round.all { (e, i) -> e.sets[i].done || e.sets[i].id == id }) round.last().let { (e, i) -> e.sets[i] } else null
}

/** The Settings key for the default rest in seconds (Q10). */
internal const val REST_KEY = "rest_default"

internal fun defaultRest(setting: String?) = setting?.toIntOrNull() ?: 120

internal fun restOf(set: WorkoutSet, default: Int) = set.rest ?: default

/** The rest field's text in seconds: "2:00", or digits typed as minutes and seconds, "200" for 2:00 (#17 choice 2). */
internal fun restSeconds(text: String): Int? {
    val (m, sec) = if (':' in text) text.substringBefore(':') to text.substringAfter(':') else text.dropLast(2) to text.takeLast(2)
    if (m.isEmpty() && sec.isEmpty()) return null
    return (m.toIntOrNull() ?: 0) * 60 + (sec.toIntOrNull() ?: 0)
}

/** A key on the rest field: digits only, four at most, shown with a colon before the last two. */
internal fun restKey(f: Field, k: Char): Field {
    val base = if (f.fresh) "" else f.text.filter { it.isDigit() }
    val digits = when {
        k == '⌫' -> base.dropLast(1)
        !k.isDigit() || base.length >= 4 -> base
        else -> base + k
    }
    return Field(if (digits.length > 2) "${digits.dropLast(2)}:${digits.takeLast(2)}" else digits, fresh = false)
}

/** − and + on the rest field: 15 s a step (Q17), never below zero. */
internal fun restStep(f: Field, by: Int) = Field(clock(max(0, (restSeconds(f.text) ?: 0) + by).toLong()), fresh = false)

internal data class Week(val year: Int, val week: Int, val workouts: List<Workout>)

/** History's groups (#17 choice 14): ISO weeks, in the order the workouts come, newest first. */
internal fun weeks(history: List<Workout>, zone: TimeZone): List<Week> =
    history.groupBy { isoWeek(Instant.fromEpochMilliseconds(it.startedAt).toLocalDateTime(zone).date) }
        .map { (key, workouts) -> Week(key.first, key.second, workouts) }

/** A week belongs to the year its Thursday is in, so 1 January can be week 53. */
internal fun isoWeek(date: LocalDate): Pair<Int, Int> {
    val thursday = date.plus(4 - date.dayOfWeek.isoDayNumber, DateTimeUnit.DAY)
    return thursday.year to (thursday.dayOfYear - 1) / 7 + 1
}

/** A set as a line reads it: "82,5 × 8 @2", "12", "1:30" or "400 m"; "–" before anything is entered. */
internal fun setText(s: WorkoutSet, type: ExerciseType, swedish: Boolean): String {
    val main = when (type) {
        ExerciseType.WeightReps -> if (s.weight == null && s.reps == null) null
            else "${s.weight?.let { kilos(it, swedish) } ?: "–"} × ${s.reps ?: "–"}"
        ExerciseType.BodyweightReps, ExerciseType.Reps -> s.reps?.toString()
        ExerciseType.Time -> s.seconds?.let { clock(it.toLong()) }
        ExerciseType.Distance -> s.metres?.let { "${kilos(it, swedish)} m" }
    } ?: return "–"
    return main + (s.rir?.let { " @$it" } ?: "")
}

internal fun clock(seconds: Long): String {
    val h = seconds / 3600
    val m = seconds % 3600 / 60
    val s = (seconds % 60).toString().padStart(2, '0')
    return if (h > 0) "$h:${m.toString().padStart(2, '0')}:$s" else "$m:$s"
}

/** Swedish groups thousands with a no-break space, English with a comma. */
internal fun grouped(n: Long, swedish: Boolean): String =
    n.toString().reversed().chunked(3).joinToString(if (swedish) " " else ",").reversed()

internal fun now() = kotlin.time.Clock.System.now().toEpochMilliseconds()

/** Ändra starttid: the same day at [hour]:[minute], never later than [now]. */
internal fun withTime(at: Long, hour: Int, minute: Int, zone: TimeZone, now: Long): Long {
    val day = Instant.fromEpochMilliseconds(at).toLocalDateTime(zone).date
    return minOf(kotlinx.datetime.LocalDateTime(day, kotlinx.datetime.LocalTime(hour, minute)).toInstant(zone).toEpochMilliseconds(), now)
}
