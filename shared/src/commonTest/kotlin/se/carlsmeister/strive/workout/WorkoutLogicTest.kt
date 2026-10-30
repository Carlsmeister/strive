package se.carlsmeister.strive.workout

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeypadTest {
    private fun Field.type(keys: String, decimals: Boolean = true, separator: Char = ',') =
        keys.fold(this) { f, k -> f.key(k, decimals, separator) }

    @Test
    fun theFirstKeyReplacesWhatWasThere() = assertEquals("85", Field("82,5").type("85").text)

    @Test
    fun backspaceOnAFreshFieldClearsIt() = assertEquals("", Field("82,5").type("⌫").text)

    @Test
    fun aDecimalTakesOneSeparatorAndTwoDecimals() = assertEquals("2,25", Field("").type("2,,2567").text)

    @Test
    fun aSeparatorFirstReadsAsZero() = assertEquals("0,5", Field("8").type(",5").text)

    @Test
    fun repsTakeNoSeparator() = assertEquals("12", Field("8").type("1,2", decimals = false).text)

    @Test
    fun theFieldStopsAtSixCharacters() = assertEquals("123456", Field("").type("1234567", decimals = false).text)

    @Test
    fun aStepAddsToTheValueAndKeepsTheLocaleFormat() {
        assertEquals("82,5", Field("80").step(2.5, ',').text)
        assertEquals("82.5", Field("80").step(2.5, '.').text)
    }

    @Test
    fun aStepNeverGoesBelowZeroAndAnEmptyFieldStartsAtZero() {
        assertEquals("0", Field("1").step(-2.5, ',').text)
        assertEquals("1", Field("").step(1.0, ',').text)
    }

    @Test
    fun aStepThenAKeyAppends() = assertEquals("825", Field("80").step(2.5, ',').type("⌫5").text.replace(",", ""))

    @Test
    fun theValueReadsEitherSeparator() {
        assertEquals(82.5, Field("82,5").value)
        assertNull(Field("").value)
    }
}

class SummaryTest {
    private fun set(weight: Double? = 80.0, reps: Int? = 8, rir: Int? = null, done: Boolean = true) =
        WorkoutSet(0, weight = weight, reps = reps, rir = rir, done = done)

    private fun exercise(type: ExerciseType, vararg sets: WorkoutSet) = SessionExercise(0, 0, "E", type, 2.5, "", sets.toList())

    private val workout = Workout(
        1, "Pass", startedAt = 0, endedAt = 68 * 60_000L + 30_000, note = "", rpe = null,
        exercises = listOf(
            exercise(ExerciseType.WeightReps, set(rir = 2), set(82.5, 6, rir = 1), set(85.0, 3, done = false)),
            exercise(ExerciseType.BodyweightReps, set(null, 10, rir = 3), set(null, 12)),
        ),
    )

    @Test
    fun theTilesCountLoggedSetsAndAverageTheRirOfThoseThatHaveOne() {
        val s = summary(workout, now = 0)
        assertEquals(68, s.minutes)
        assertEquals(4, s.sets)
        assertEquals(2.0, s.rir)
        assertEquals(3, s.rirSets)
        assertEquals(80.0 * 8 + 82.5 * 6, s.volume)
    }

    @Test
    fun withoutAnRirTheAverageIsMissing() = assertNull(summary(workout.copy(exercises = listOf(exercise(ExerciseType.Reps, set(null, 5)))), 0).rir)

    @Test
    fun anOpenWorkoutCountsToNow() = assertEquals(5, summary(workout.copy(endedAt = null), now = 5 * 60_000).minutes)

    @Test
    fun theBestSetIsTheHeaviestThenTheMostReps() {
        assertEquals(set(85.0, 3), best(exercise(ExerciseType.WeightReps, set(80.0, 8), set(85.0, 3), set(85.0, 2))))
        assertEquals(set(null, 12), best(exercise(ExerciseType.Reps, set(null, 10), set(null, 12))))
        assertNull(best(exercise(ExerciseType.WeightReps, set(done = false))))
    }
}

class WeeksTest {
    private val zone = TimeZone.of("Europe/Stockholm")
    private fun at(y: Int, m: Int, d: Int) = Workout(
        y * 10_000L + m * 100 + d, "Pass", LocalDateTime(y, m, d, 17, 0).toInstant(zone).toEpochMilliseconds(), null, "", null, emptyList(),
    )

    @Test
    fun workoutsGroupByIsoWeekNewestFirst() {
        val history = listOf(at(2026, 10, 25), at(2026, 10, 19), at(2026, 10, 18))
        assertEquals(listOf(43 to 2, 42 to 1), weeks(history, zone).map { it.week to it.workouts.size })
    }

    @Test
    fun theFirstDaysOfJanuaryCanBelongToTheYearBefore() {
        // 1 January 2027 is a Friday, so it is in week 53 of 2026.
        val week = weeks(listOf(at(2027, 1, 1)), zone).single()
        assertEquals(2026 to 53, week.year to week.week)
    }
}
class FormatTest {
    @Test
    fun aSetReadsAsWeightTimesRepsAtRir() {
        assertEquals("82,5 × 8 @2", setText(WorkoutSet(0, weight = 82.5, reps = 8, rir = 2), ExerciseType.WeightReps, swedish = true))
        assertEquals("82.5 × 8", setText(WorkoutSet(0, weight = 82.5, reps = 8), ExerciseType.WeightReps, swedish = false))
        assertEquals("12", setText(WorkoutSet(0, reps = 12), ExerciseType.BodyweightReps, swedish = true))
        assertEquals("1:30", setText(WorkoutSet(0, seconds = 90), ExerciseType.Time, swedish = true))
        assertEquals("–", setText(WorkoutSet(0), ExerciseType.WeightReps, swedish = true))
    }

    @Test
    fun aClockShowsHoursOnlyWhenThereAreAny() {
        assertEquals("0:07", clock(7))
        assertEquals("12:34", clock(754))
        assertEquals("1:02:03", clock(3723))
    }

    @Test
    fun thousandsAreGroupedTheLocaleWay() {
        assertEquals("12\u00A0450", grouped(12_450, swedish = true))
        assertEquals("12,450", grouped(12_450, swedish = false))
        assertEquals("950", grouped(950, swedish = true))
    }
}

class SwipeTest {
    private val width = 1000f
    private val open = -264f
    private val flick = 900f
    private fun target(offset: Float, velocity: Float = 0f) = swipeTarget(offset, velocity, width, open, flick)

    @Test
    fun pastHalfTheRowDeletes() = assertEquals(-width, target(-510f))

    @Test
    fun aQuickShortFlickLeftShowsDeleteInsteadOfDeleting() = assertEquals(open, target(-390f, velocity = -3000f))

    @Test
    fun aFlickRightHidesDeleteAgain() = assertEquals(0f, target(-264f, velocity = 2000f))

    @Test
    fun withoutAFlickTheRowGoesToTheNearerOfShutAndOpen() {
        assertEquals(open, target(-200f))
        assertEquals(0f, target(-100f))
    }
}
