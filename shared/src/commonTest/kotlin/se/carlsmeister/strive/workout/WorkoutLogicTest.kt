package se.carlsmeister.strive.workout

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.data.warmupOrder
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

class PreviousTest {
    private fun ex(exerciseId: Long, vararg sets: WorkoutSet) = SessionExercise(exerciseId * 10, exerciseId, "E$exerciseId", ExerciseType.WeightReps, 2.5, "", sets.toList())
    private fun w(id: Long, vararg exercises: SessionExercise) = Workout(id, "Pass", id, id + 1, "", null, exercises.toList())

    // History is newest first.
    private val history = listOf(
        w(3, ex(2, WorkoutSet(31, weight = 26.0, reps = 10, rir = 2, done = true))),
        w(2, ex(1, WorkoutSet(21, weight = 80.0, reps = 8, rir = 3, done = true), WorkoutSet(22, weight = 80.0, reps = 7, rir = 2, done = true))),
        w(1, ex(1, WorkoutSet(11, weight = 75.0, reps = 8, done = true))),
    )

    @Test
    fun theSetsComeFromTheNewestFinishedWorkoutWithThatExercise() =
        assertEquals(listOf(21L, 22L), previous(history, exerciseId = 1).map { it.id })

    @Test
    fun anExerciseNeverDoneHasNone() = assertEquals(emptyList(), previous(history, exerciseId = 9))

    @Test
    fun theChangeIsThePlannedWeightAgainstLastTime() {
        assertEquals(2.5, change(82.5, 80.0))
        assertEquals(-2.5, change(77.5, 80.0))
        assertNull(change(80.0, 80.0))
        assertNull(change(null, 80.0))
        assertNull(change(80.0, null))
    }
}

class SetKindTest {
    private fun s(id: Long, kind: SetKind = SetKind.Normal, done: Boolean = false) = WorkoutSet(id, kind = kind, done = done)

    @Test
    fun workingSetsAreNumberedAmongThemselvesAndTheOthersShowTheirLetter() =
        assertEquals(listOf("W", "1", "2", "D", "3", "F"),
            badges(listOf(s(1, SetKind.Warmup), s(2), s(3), s(4, SetKind.Drop), s(5), s(6, SetKind.Failure))))

    @Test
    fun anUnloggedSetMadeAWarmUpMovesUpToBeTheNextSetToDo() {
        // Before starting: above the working sets.
        assertEquals(listOf(3L, 1L, 2L), warmupOrder(listOf(s(1), s(2), s(3)), id = 3))
        // Mid-exercise: next in line, after what is logged.
        assertEquals(listOf(1L, 4L, 2L, 3L), warmupOrder(listOf(s(1, done = true), s(2), s(3), s(4)), id = 4))
    }

    @Test
    fun aLoggedSetMadeAWarmUpStaysWhereItIs() =
        assertEquals(listOf(1L, 2L, 3L), warmupOrder(listOf(s(1, done = true), s(2, done = true), s(3)), id = 2))

    @Test
    fun warmUpsCountForNeitherSnittRirNorVolume() {
        val w = Workout(1, "Pass", 0, 1, "", null, listOf(SessionExercise(1, 1, "E", ExerciseType.WeightReps, 2.5, "", listOf(
            WorkoutSet(1, weight = 40.0, reps = 10, rir = 5, kind = SetKind.Warmup, done = true),
            WorkoutSet(2, weight = 80.0, reps = 8, rir = 2, done = true),
        ))))
        val s = summary(w, now = 0)
        assertEquals(2.0, s.rir)
        assertEquals(1, s.rirSets)
        assertEquals(640.0, s.volume)
    }
}

class RestTest {
    @Test
    fun aRestNeverShowsMoreThanItsLengthOrLessThanNothing() {
        assertEquals(135, Rest(startedAt = 10_000, seconds = 135).left(now = 8_500))
        assertEquals(75, Rest(startedAt = 0, seconds = 135).left(now = 60_000))
        assertEquals(0, Rest(startedAt = 0, seconds = 60).left(now = 61_000))
    }

    @Test
    fun aSetsOwnRestWinsOverTheDefault() {
        assertEquals(90, restOf(WorkoutSet(1, rest = 90), default = 120))
        assertEquals(120, restOf(WorkoutSet(1), default = 120))
    }

    @Test
    fun theDefaultRestIsTwoMinutesUntilSettingsSaysOtherwise() {
        assertEquals(120, defaultRest(null))
        assertEquals(150, defaultRest("150"))
        assertEquals(120, defaultRest("nonsense"))
    }

    @Test
    fun restDigitsReadAsMinutesAndSeconds() {
        assertEquals(120, restSeconds("200"))
        assertEquals(90, restSeconds("130"))
        assertEquals(120, restSeconds("2:00"))
        assertEquals(45, restSeconds("45"))
        assertNull(restSeconds(""))
    }

    @Test
    fun restKeysTypeDigitsAndShowAColonBeforeTheLastTwo() {
        val f = "130".fold(Field("2:00")) { f, k -> restKey(f, k) }
        assertEquals("1:30", f.text)
        assertEquals("13", restKey(f, '⌫').text)
        assertEquals("1:30", restKey(f, ',').text)
    }

    @Test
    fun aRestStepIsFifteenSecondsAndNeverBelowZero() {
        assertEquals("2:15", restStep(Field("2:00"), 15).text)
        assertEquals("0:00", restStep(Field("0:10"), -15).text)
    }

    @Test
    fun theRestLeftCountsDownFromWhenItStarted() {
        assertEquals(120, Rest(startedAt = 0, seconds = 120).left(now = 0))
        assertEquals(30, Rest(startedAt = 0, seconds = 120).left(now = 90_500))
        assertEquals(0, Rest(startedAt = 0, seconds = 120).left(now = 200_000))
    }
}

class StartTimeTest {
    private val zone = TimeZone.of("Europe/Stockholm")
    private fun at(h: Int, m: Int) = LocalDateTime(2026, 11, 2, h, m).toInstant(zone).toEpochMilliseconds()

    @Test
    fun aNewStartTimeKeepsTheDay() = assertEquals(at(17, 5), withTime(at(17, 40), 17, 5, zone, now = at(18, 30)))

    @Test
    fun aStartTimeIsNeverInTheFuture() = assertEquals(at(18, 30), withTime(at(17, 40), 19, 0, zone, now = at(18, 30)))
}
