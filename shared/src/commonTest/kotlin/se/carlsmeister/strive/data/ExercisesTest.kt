package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExercisesTest {
    private val database = StriveDatabase(memoryDriver())
    private val settings = SqlSettings(database)
    private val exercises = SqlExercises(database)

    private fun seed(name: String = "Bench Press BB", steps: String = "1. Press.") = """
        [{"name": "$name", "aliases": "bench | Bänkpress", "primary_muscle": "chest", "secondary_muscles": ["triceps", "shoulders"],
          "pattern": "Horisontell press", "equipment": "Skivstång", "type": "weight_reps", "per_side": false, "step": 2.5, "bar": 20,
          "category": "strength", "steps_en": "$steps", "tips_en": "Tip.", "steps_sv": "1. Pressa.", "tips_sv": "Tips.",
          "prompt": {"view": "side"}, "verified": false}]
    """.trimIndent()

    @Test
    fun aFreshDatabaseGetsTheSeed() = runTest {
        seedExercises(seed(), version = 1, exercises, settings)
        val bench = exercises.all().first().single()
        assertEquals("Bench Press BB", bench.name)
        assertEquals(listOf("bench", "Bänkpress"), bench.aliases)
        assertEquals(Muscle.Chest, bench.primary)
        assertEquals(listOf(Muscle.Triceps, Muscle.Shoulders), bench.secondary)
        assertEquals(ExerciseType.WeightReps, bench.type)
        assertEquals(20.0, bench.bar)
        assertNull(bench.mark)
    }

    @Test
    fun aNewerSeedUpdatesAnExerciseButKeepsItsMark() = runTest {
        seedExercises(seed(), version = 1, exercises, settings)
        val id = exercises.all().first().single().id
        exercises.setMark(id, Mark.Yes)
        seedExercises(seed(steps = "1. Press harder."), version = 2, exercises, settings)
        val bench = exercises.all().first().single()
        assertEquals("1. Press harder.", bench.stepsEn)
        assertEquals(Mark.Yes, bench.mark)
    }

    @Test
    fun theSameSeedVersionDoesNotRunAgain() = runTest {
        seedExercises(seed(), version = 1, exercises, settings)
        seedExercises(seed(steps = "1. Changed."), version = 1, exercises, settings)
        assertEquals("1. Press.", exercises.all().first().single().stepsEn)
    }

    @Test
    fun anExerciseReadsBackById() = runTest {
        seedExercises(seed(), version = 1, exercises, settings)
        val id = exercises.all().first().single().id
        assertEquals("Bench Press BB", exercises.byId(id).first()?.name)
    }
}
