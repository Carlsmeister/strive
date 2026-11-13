package se.carlsmeister.strive.fixture

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.EXERCISE_SEED
import se.carlsmeister.strive.data.Finish
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.SqlExercises
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.seedExercises
import se.carlsmeister.strive.workout.wallClock
import kotlin.time.Clock
import kotlin.time.Instant

/** `files/exercises.json`, read the way this platform's tests can reach it. */
internal expect suspend fun exerciseSeed(): String

/**
 * Carl's sample data (`docs/design/18-approved/inventory.md` section 5), the state every board shot loads: Monday 9 November 2026 at
 * 17:26:13, 24:13 into today's Push A, with last Monday's Push A finished behind it. Written through the app's own calls only.
 */
internal object SampleData {
    val zone = TimeZone.of("Europe/Stockholm")
    val now: Instant = LocalDateTime(2026, 11, 9, 17, 26, 13).toInstant(zone)

    /** Pins [wallClock] at [now] and writes the data; returns today's open workout's id. The caller resets [wallClock] after. */
    suspend fun load(graph: AppGraph): Long {
        wallClock = object : Clock {
            override fun now() = SampleData.now
        }
        seed(graph)
        marks(graph)
        lastMonday(graph)
        return today(graph)
    }

    private suspend fun seed(graph: AppGraph) {
        seedExercises(exerciseSeed(), EXERCISE_SEED, graph.exercises as SqlExercises, graph.settings)
        graph.exercises.setNote(graph.id("Bench Press BB"), "Arch, feet planted.")
    }

    private suspend fun marks(graph: AppGraph) {
        val marks = listOf("Bench Press BB", "Squat BB", "Deadlift BB", "Overhead Press BB", "Pull Up", "Incline Press DB", "Lateral Raise DB",
            "Romanian Deadlift DB").associateWith { Mark.Yes } +
            mapOf("Seated Row Cable" to Mark.Sub, "Bulgarian Split Squat DB" to Mark.Test, "Leg Extension Machine" to Mark.No)
        marks.forEach { (name, mark) -> graph.exercises.setMark(graph.id(name), mark) }
    }

    private suspend fun lastMonday(graph: AppGraph) {
        val id = pushA(
            graph, at(2, 17, 5),
            listOf(set(40.0, 10, kind = SetKind.Warmup), set(77.5, 8, 3), set(80.0, 8, 3), set(80.0, 7, 2)),
            listOf(set(26.0, 9, 2), set(26.0, 9, 2), set(26.0, 8, 1)),
            listOf(set(10.0, 13, 2), set(10.0, 13, 2), set(10.0, 12, 1)),
            listOf(set(10.0, 9, 2), set(10.0, 8, 0, SetKind.Failure)),
            listOf(set(30.0, 11, 2), set(30.0, 10, 1), set(20.0, 12, kind = SetKind.Drop)),
            done = true,
        )
        graph.sessions.finish(id, Finish.WithoutThem, at(2, 18, 3))
        graph.sessions.setNote(id, "Kändes stark idag.")
        graph.sessions.setRpe(id, 7)
    }

    private suspend fun today(graph: AppGraph): Long {
        val id = pushA(
            graph, at(9, 17, 2),
            listOf(set(40.0, 10, kind = SetKind.Warmup, done = true), set(80.0, 8, 2, done = true), set(82.5, 8), set(80.0, 7)),
            List(3) { set(26.0, 9) },
            List(3) { set(10.0, 13) },
            listOf(set(10.0, 9), set(10.0, 9, kind = SetKind.Failure)),
            listOf(set(30.0, 11), set(30.0, 11), set(20.0, 12, kind = SetKind.Drop)),
        )
        return id
    }

    /**
     * Push A started at [startedAt]: Bench Press BB, then Incline Press DB and Lateral Raise DB as one superset, then the triceps, each
     * with its [sets] in order; every set logged if [done].
     */
    private suspend fun pushA(graph: AppGraph, startedAt: Long, vararg sets: List<WorkoutSet>, done: Boolean = false): Long {
        val s = graph.sessions
        val id = s.start("Push A", startedAt)
        s.add(id, listOf(graph.id("Bench Press BB")))
        s.add(id, listOf(graph.id("Incline Press DB"), graph.id("Lateral Raise DB")), superset = true)
        s.add(id, listOf(graph.id("Tricep Dips"), graph.id("Tricep Pushdown")))
        val rests = listOf(120, 90, 90, 90, 90)
        s.workout(id).first()!!.exercises.forEachIndexed { i, e ->
            repeat(sets[i].size - 1) { s.addSet(e.id) }
            val ids = s.workout(id).first()!!.exercises[i].sets.map { it.id }
            sets[i].zip(ids) { set, setId ->
                s.saveSet(set.copy(id = setId, done = done || set.done))
                if (set.kind != SetKind.Normal) s.setKind(setId, set.kind)
            }
            s.setRestAll(e.id, rests[i])
        }
        return id
    }

    private fun set(weight: Double, reps: Int, rir: Int? = null, kind: SetKind = SetKind.Normal, done: Boolean = false) =
        WorkoutSet(0, weight, reps, rir = rir, done = done, kind = kind)

    /** November [day] 2026 at [hour]:[minute] in [zone], in epoch milliseconds. */
    private fun at(day: Int, hour: Int, minute: Int) = LocalDateTime(2026, 11, day, hour, minute).toInstant(zone).toEpochMilliseconds()

    private suspend fun AppGraph.id(name: String) = exercises.all().first().first { it.name == name }.id
}
