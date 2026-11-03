package se.carlsmeister.strive.flows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.writeToFile
import androidx.compose.runtime.CompositionLocalProvider
import se.carlsmeister.strive.App
import se.carlsmeister.strive.theme.LocalDarkTheme
import se.carlsmeister.strive.theme.LocalHighContrast
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.deleteFileDatabase
import se.carlsmeister.strive.data.fileDriver
import se.carlsmeister.strive.data.memoryDriver
import se.carlsmeister.strive.db.StriveDatabase
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_count
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.add_set
import strive.shared.generated.resources.all_activities
import strive.shared.generated.resources.back
import strive.shared.generated.resources.back_to_workout
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.close
import strive.shared.generated.resources.delete
import strive.shared.generated.resources.discard_question
import strive.shared.generated.resources.discard_workout
import strive.shared.generated.resources.done
import strive.shared.generated.resources.empty_workout
import strive.shared.generated.resources.exercise_options
import strive.shared.generated.resources.field_reps
import strive.shared.generated.resources.field_seconds
import strive.shared.generated.resources.field_weight
import strive.shared.generated.resources.finish
import strive.shared.generated.resources.hide
import strive.shared.generated.resources.keep_going
import strive.shared.generated.resources.library
import strive.shared.generated.resources.log
import strive.shared.generated.resources.log_and_save
import strive.shared.generated.resources.log_next
import strive.shared.generated.resources.log_set
import strive.shared.generated.resources.logged
import strive.shared.generated.resources.minimise
import strive.shared.generated.resources.next
import strive.shared.generated.resources.no_workouts
import strive.shared.generated.resources.note
import strive.shared.generated.resources.remove_exercise
import strive.shared.generated.resources.rename
import strive.shared.generated.resources.rir
import strive.shared.generated.resources.save
import strive.shared.generated.resources.save_change
import strive.shared.generated.resources.save_without
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.start_a_workout
import strive.shared.generated.resources.start_workout
import strive.shared.generated.resources.tab_plan
import strive.shared.generated.resources.tab_start
import strive.shared.generated.resources.tab_you
import strive.shared.generated.resources.unlogged
import strive.shared.generated.resources.workout
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToKey
import se.carlsmeister.strive.data.Mark
import strive.shared.generated.resources.add_named
import strive.shared.generated.resources.collapse
import strive.shared.generated.resources.edit
import strive.shared.generated.resources.expand
import strive.shared.generated.resources.now_set
import strive.shared.generated.resources.quick_picks
import strive.shared.generated.resources.sets_progress
import strive.shared.generated.resources.what_today
import strive.shared.generated.resources.back_to_focus
import strive.shared.generated.resources.change_start
import strive.shared.generated.resources.free_rest
import strive.shared.generated.resources.kind_warmup
import strive.shared.generated.resources.order_skip
import strive.shared.generated.resources.rest
import strive.shared.generated.resources.rest_all
import strive.shared.generated.resources.rest_row
import strive.shared.generated.resources.set_type
import strive.shared.generated.resources.skip
import strive.shared.generated.resources.skipped
import strive.shared.generated.resources.sticky_note
import strive.shared.generated.resources.swap
import strive.shared.generated.resources.up_by
import strive.shared.generated.resources.rest_after
import strive.shared.generated.resources.kind_failure
import strive.shared.generated.resources.now_kind
import strive.shared.generated.resources.rest_change
import strive.shared.generated.resources.superset_count
import strive.shared.generated.resources.superset_create
import strive.shared.generated.resources.superset_leave
import strive.shared.generated.resources.superset_rest
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Every flow of logging a workout (#42), driven through the real screens on the iOS simulator at iPhone 13 Pro Max size.
 * Texts are looked up in the app's own strings, so the tests pass in whatever language the simulator runs.
 */
@OptIn(ExperimentalTestApi::class)
class FlowTest {
    private fun flow(graph: AppGraph = AppGraph(memoryDriver()), dark: Boolean? = null, highContrast: Boolean? = null, block: suspend Flow.() -> Unit) =
        runSkikoComposeUiTest(Size(1284f, 2778f), Density(3f)) {
            setContent { CompositionLocalProvider(LocalDarkTheme provides dark, LocalHighContrast provides highContrast) { App(graph) } }
            Flow(this, graph).block()
        }

    /** The whole workout in dark mode, for a person to look at: text that named no colour was black on black there (#42). */
    @Test
    fun everyWorkoutScreenInDarkMode() = flow(dark = true) {
        startWorkout()
        shot("dark-01-empty")
        tap(s(Res.string.add_exercises))
        search("Bench Press")
        shot("dark-02-picker")
        tap("Bench Press BB")
        tap(s(Res.string.add_count, 1))
        see("Bench Press BB")
        openSet(0)
        tapSegment("2")
        shot("dark-03-dock")
        tapDesc(s(Res.string.field_weight))
        shot("dark-04-keypad")
        keys("80")
        tap(s(Res.string.next))
        keys("8")
        tap(s(Res.string.next))
        tap(s(Res.string.log_next))
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        shot("dark-05-sets")
        tap(s(Res.string.finish))
        see(s(Res.string.keep_going))
        shot("dark-06-sheet")
        tap(s(Res.string.log_and_save))
        see(s(Res.string.done))
        tapSegment("7")
        shot("dark-07-summary")
    }

    @Test
    fun aNewWorkoutStartsEmpty() = flow {
        startWorkout()
        see(s(Res.string.what_today))
        see("0/0", substring = true)
        shot("01-empty-workout")
    }

    @Test
    fun thePickerAddsExercisesInTheOrderTicked() = flow {
        startWorkout()
        addExercises("Chest Dips", "Bench Press BB")
        assertEquals(listOf("Chest Dips", "Bench Press BB"), open().exercises.map { it.name })
        // Only the exercise in focus stands open; the second waits folded below it.
        test.waitUntil(timeoutMillis = TIMEOUT) { setRows().fetchSemanticsNodes().size == 1 }
        seeDesc(s(Res.string.expand, "Bench Press BB"))
        assertEquals(listOf(1, 1), open().exercises.map { it.sets.size })
    }

    @Test
    fun cancelInThePickerAddsNothing() = flow {
        startWorkout()
        tap(s(Res.string.add_exercises))
        search("Bench Press BB")
        tap("Bench Press BB")
        tap(s(Res.string.cancel))
        see(s(Res.string.what_today))
        assertEquals(0, open().exercises.size)
    }

    @Test
    fun theKeypadTypesWeightAndRepsAndLogsOnTheLastField() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        shot("02-dock")
        tapDesc(s(Res.string.field_weight))
        shot("03-keypad")
        keys("80")
        tap(s(Res.string.next))
        keys("8")
        tap(s(Res.string.next))
        shot("03b-rest-field")
        tap(s(Res.string.log_next))
        see(s(Res.string.sets_progress, 1, 1))
        seeDesc(s(Res.string.logged, 1))
        val set = open().exercises.single().sets.single()
        assertEquals(80.0 to 8, set.weight to set.reps)
        assertTrue(set.done)
        shot("04-logged")
    }

    @Test
    fun backspaceAndHideWork() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapDesc(s(Res.string.field_weight))
        keys("805⌫")
        tap(s(Res.string.hide))
        // The keypad is gone, the steppers are back, and the field kept what was typed.
        seeDesc("${s(Res.string.field_weight)} +")
        see("80")
    }

    @Test
    fun stepsAndRirLogFromTheDock() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        repeat(2) { tapDesc("${s(Res.string.field_weight)} +") } // 2,5 kg a step
        repeat(3) { tapDesc("${s(Res.string.field_reps)} +") }
        tapDesc("${s(Res.string.field_reps)} −")
        tapSegment("2")
        tap(s(Res.string.log))
        waitUntilTrue { open().exercises.single().sets.single().let { it.done && it.weight == 5.0 && it.reps == 2 && it.rir == 2 } }
    }

    @Test
    fun rirFourPlusOpensFourToTenAndShowsThePick() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapSegment("4+")
        tapSegment("7")
        tapDesc("${s(Res.string.field_weight)} +")
        tapDesc("${s(Res.string.field_reps)} +")
        tap(s(Res.string.log))
        waitUntilTrue { open().exercises.single().sets.single().rir == 7 }
    }

    @Test
    fun anEmptySetsCheckOpensTheDockInsteadOfLogging() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.log_set, 1))
        seeDesc(s(Res.string.field_weight))
        assertEquals(false, open().exercises.single().sets.single().done)
    }

    @Test
    fun logWithAValueMissingGoesToTheEmptyField() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapDesc("${s(Res.string.field_weight)} +")
        tap(s(Res.string.log))
        // Reps is empty, so Logga opens the keypad on it rather than logging half a set; rest comes after it.
        see(s(Res.string.next))
        test.onNodeWithTag("key-1").assertExists()
        assertEquals(false, open().exercises.single().sets.single().done)
    }

    @Test
    fun aNewSetCopiesTheLastAndItsCheckLogsItInOneTap() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().exercises.single().sets.size == 2 }
        tapDesc(s(Res.string.log_set, 2))
        waitUntilTrue { open().logged == 2 }
        val second = open().exercises.single().sets[1]
        assertEquals(80.0 to 8, second.weight to second.reps)
        // A tap on a logged set's check takes the log back.
        tapDesc(s(Res.string.logged, 2))
        waitUntilTrue { open().logged == 1 }
    }

    @Test
    fun aLoggedSetOpensWithSaveChange() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        openSet(0)
        tapDesc("${s(Res.string.field_reps)} +")
        tap(s(Res.string.save_change))
        waitUntilTrue { open().exercises.single().sets.single().reps == 9 }
    }

    @Test
    fun theDockClosesOnATapOutside() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        test.onRoot().performTouchInput { click(Offset(centerX, 300f)) }
        gone(hasContentDescription(s(Res.string.field_weight)))
    }

    @Test
    fun theDockClosesWhenItsGrabberIsDraggedDown() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        test.onNodeWithContentDescription(s(Res.string.close)).performTouchInput { swipeDown(endY = centerY + 900f) }
        gone(hasContentDescription(s(Res.string.field_weight)))
    }

    @Test
    fun theGrabbersCloseActionClosesTheDockForVoiceOver() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        // VoiceOver does not tap the grabber, it performs its action.
        test.onNodeWithContentDescription(s(Res.string.close)).performSemanticsAction(SemanticsActions.OnClick)
        gone(hasContentDescription(s(Res.string.field_weight)))
    }

    @Test
    fun aFullSwipeLeftDeletesASet() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().exercises.single().sets.size == 2 }
        setRows()[1].performTouchInput { swipeLeft(startX = right - 10f, endX = left + 10f) }
        waitUntilTrue { open().exercises.single().sets.size == 1 }
    }

    @Test
    fun aShortSwipeShowsDeleteAndATapOnItDeletes() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        setRows()[0].performTouchInput { swipeLeft(startX = right - 10f, endX = right - 400f) }
        see(s(Res.string.delete))
        // Only the part the row uncovers is red; the row itself stays opaque (point 4).
        shot("13-swipe-open")
        // The swipe began on the set's ✓ and ended over the row: neither may count as a tap.
        gone(hasContentDescription(s(Res.string.field_weight)))
        tap(s(Res.string.delete))
        waitUntilTrue { open().exercises.single().sets.isEmpty() }
    }

    @Test
    fun aTapOnARowSwipedOpenShutsItInsteadOfOpeningTheDock() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        setRows()[0].performTouchInput { swipeLeft(startX = centerX, endX = centerX - 400f) }
        see(s(Res.string.delete))
        setRows()[0].performClick()
        gone(hasText(s(Res.string.delete)))
        gone(hasContentDescription(s(Res.string.field_weight)))
        assertEquals(1, open().exercises.single().sets.size)
    }

    @Test
    fun theExerciseMenuAddsANoteAndRemovesTheExercise() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.note))
        type("Elbows in")
        waitUntilTrue { open().exercises.single().note == "Elbows in" }
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.remove_exercise))
        waitUntilTrue { open().exercises.isEmpty() }
    }

    @Test
    fun theWorkoutMenuRenamesAndKeepsANote() = flow {
        startWorkout()
        tap(s(Res.string.workout))
        tap(s(Res.string.rename))
        type("Push A")
        tap(s(Res.string.save))
        see("Push A")
        tap("Push A")
        tap(s(Res.string.session_note))
        type("Slept badly")
        tap(s(Res.string.save))
        waitUntilTrue { open().name == "Push A" && open().note == "Slept badly" }
    }

    @Test
    fun finishingWithEverySetLoggedGoesStraightToTheSummary() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.finish))
        see(s(Res.string.done))
        shot("05-summary")
        tapSegment("7") // RPE
        waitUntilTrue { graph.sessions.history().first().single().rpe == 7 }
        tap("Bench Press BB") // the folded exercise opens to its sets
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasText("80 × 8")).fetchSemanticsNodes().size >= 2 }
        shot("05b-summary-open")
        tap(s(Res.string.done))
        see(s(Res.string.tab_start))
        assertNull(graph.sessions.open().first())
    }

    @Test
    fun finishingWithSetsLeftAsksAndLogsThemAsTheyStand() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        tap(s(Res.string.finish))
        see(getPluralString(Res.plurals.unlogged, 1, 1))
        shot("06-finish-sheet")
        tap(s(Res.string.log_and_save))
        see(s(Res.string.done))
        assertEquals(2, graph.sessions.history().first().single().logged)
    }

    @Test
    fun finishingWithSetsLeftCanSaveWithoutThem() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        tap(s(Res.string.finish))
        tap(s(Res.string.save_without))
        see(s(Res.string.done))
        assertEquals(1, graph.sessions.history().first().single().sets)
    }

    @Test
    fun finishingWithSetsLeftCanDiscardTheWorkout() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.finish))
        see(s(Res.string.keep_going))
        tap(s(Res.string.discard_workout)) // the sheet's row is drawn last, over the list's own button
        see(s(Res.string.tab_start))
        assertNull(graph.sessions.open().first())
        assertEquals(0, graph.sessions.history().first().size)
    }

    @Test
    fun continueInTheSheetKeepsTheWorkoutGoing() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.finish))
        tap(s(Res.string.keep_going))
        gone(hasText(s(Res.string.keep_going)))
        assertEquals(1, open().sets)
    }

    @Test
    fun finishingAnEmptyWorkoutOffersToDiscardIt() = flow {
        startWorkout()
        tap(s(Res.string.finish))
        see(s(Res.string.discard_question))
        tap(s(Res.string.cancel))
        gone(hasText(s(Res.string.discard_question)))
        open()
    }

    @Test
    fun discardFromTheListAsksFirst() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.discard_workout))
        see(s(Res.string.discard_question))
        tap(s(Res.string.cancel))
        open()
        tap(s(Res.string.discard_workout))
        see(s(Res.string.discard_question))
        tap(s(Res.string.discard_workout)) // the alert's button is drawn last
        see(s(Res.string.tab_start))
        assertNull(graph.sessions.open().first())
    }

    @Test
    fun minimisingLeavesTheWorkoutInTheBarAndStartContinuesIt() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.minimise))
        bar()
        shot("07-minimised")
        tap(s(Res.string.tab_start))
        // Two workouts cannot run at once: the sheet only continues this one.
        gone(hasText(s(Res.string.start_workout)))
        tapContaining(s(Res.string.workout))
        see(s(Res.string.add_set), substring = true)
    }

    @Test
    fun theBarBringsTheWorkoutBack() = flow {
        startWorkout()
        tapDesc(s(Res.string.minimise))
        bar()
        test.onNode(clickLabel(s(Res.string.back_to_workout))).performClick()
        see(s(Res.string.what_today))
    }

    @Test
    fun historyStartsEmptyAndOffersAWorkout() = flow {
        tap(s(Res.string.tab_you))
        tap(s(Res.string.all_activities))
        see(s(Res.string.no_workouts))
        tap(s(Res.string.start_a_workout))
        see(s(Res.string.what_today))
    }

    @Test
    fun aFinishedWorkoutIsInHistoryAndOpensItsDetail() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.finish))
        tap(s(Res.string.done))
        tap(s(Res.string.tab_you))
        tap(s(Res.string.all_activities))
        see("640 kg", substring = true)
        shot("08-history")
        tap(s(Res.string.workout))
        see(s(Res.string.back))
        shot("09-session-detail")
        tap(s(Res.string.back))
        see(s(Res.string.all_activities))
    }

    @Test
    fun aBodyweightExerciseLogsRepsOnly() = flow {
        startWorkout()
        addExercises("Chest Dips")
        openSet(0)
        gone(hasContentDescription(s(Res.string.field_weight)))
        repeat(10) { tapDesc("${s(Res.string.field_reps)} +") }
        tap(s(Res.string.log))
        waitUntilTrue { open().exercises.single().sets.single().let { it.done && it.reps == 10 } }
    }

    @Test
    fun aTimedExerciseLogsSecondsWithoutRir() = flow {
        startWorkout()
        addExercises("Handstand")
        openSet(0)
        gone(hasText(s(Res.string.rir)))
        tapDesc(s(Res.string.field_seconds))
        keys("45")
        tap(s(Res.string.next))
        tap(s(Res.string.log_next))
        waitUntilTrue { open().exercises.single().sets.single().let { it.done && it.seconds == 45 } }
    }

    @Test
    fun theFocusCardLogsTheNextSetAndMovesOnToTheNextExercise() = flow {
        startWorkout()
        addExercises("Bench Press BB", "Chest Dips")
        logFirstSet("80", "8")
        // Bench Press's one set is done, so the card has moved to Chest Dips.
        see(s(Res.string.now_set, "Chest Dips", 1, 1).uppercase())
        // Bench Press folded once the card moved on; opened, a new set there comes first, so the card goes back to it.
        tapDesc(s(Res.string.expand, "Bench Press BB"))
        test.onAllNodes(hasText(s(Res.string.add_set), substring = true) and hasClickAction())[0].performClick()
        see(s(Res.string.now_set, "Bench Press BB", 2, 2).uppercase())
        tapContaining(s(Res.string.log_set, 2))
        waitUntilTrue { open().exercises.first().sets.all { it.done } }
        shot("10-focus-moved")
    }

    @Test
    fun theFocusCardsEditOpensTheDockOnItsSet() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.edit))
        seeDesc(s(Res.string.field_weight))
    }

    @Test
    fun aValueFieldOpensTheKeypadOnThatField() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        test.onNodeWithTag("set Bench Press BB 1 cell 1").performClick()
        // Reps is followed by the rest, so the keypad's button says Nästa.
        see(s(Res.string.next))
        keys("8")
        see("8")
    }

    @Test
    fun anExerciseFoldsToItsOverviewAndUnfolds() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.collapse, "Bench Press BB"))
        test.waitUntil(timeoutMillis = TIMEOUT) { setRows().fetchSemanticsNodes().isEmpty() }
        see("0/1")
        tapDesc(s(Res.string.expand, "Bench Press BB"))
        test.waitUntil(timeoutMillis = TIMEOUT) { setRows().fetchSemanticsNodes().size == 1 }
    }

    @Test
    fun theStripIsOneButtonBackToTheFocusCard() = flow {
        startWorkout()
        addExercises("Bench Press BB", "Chest Dips", "Squat BB", "Pull Up", "Incline Press DB")
        logFirstSet("80", "8")
        tap(s(Res.string.skip)) // the rest card
        // Folded exercises are short; open three so the list is long enough to scroll the card away.
        for (name in listOf("Squat BB", "Pull Up", "Incline Press DB")) tapDesc(s(Res.string.expand, name))
        test.onNode(hasScrollToIndexAction()).performScrollToKey("end")
        wait("the strip") { test.onAllNodes(hasTestTag("focus-strip")).fetchSemanticsNodes().isNotEmpty() }
        shot("11-strip")
        // No Logga on the strip (point 7): the whole strip scrolls back up to the card.
        gone(hasText(s(Res.string.log_set, 1), substring = true) and hasClickAction() and !hasContentDescription(s(Res.string.log_set, 1)))
        test.onNode(clickLabel(s(Res.string.back_to_focus))).performClick()
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("focus")).fetchSemanticsNodes().isNotEmpty() }
        gone(hasTestTag("focus-strip"))
    }

    @Test
    fun theFocusCardShowsLastTimeTheChangeAndTheRest() = flow {
        designD()
        see(s(Res.string.up_by, "5"))
        see(s(Res.string.rest_after, "2:00"))
        see("80 × 8 @3", substring = true)
        // A logged set shows its RIR after the reps (point 2), and a rest line sits between the sets (point 8).
        see("8 @2")
        see(s(Res.string.rest_row, "2:00"))
        shot("14-d-pass")
    }

    @Test
    fun tappingTheExerciseNameFoldsIt() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        test.onNode(clickLabel(s(Res.string.collapse, "Bench Press BB")) and hasText("Bench Press BB")).performClick()
        test.waitUntil(timeoutMillis = TIMEOUT) { setRows().fetchSemanticsNodes().isEmpty() }
        seeDesc(s(Res.string.expand, "Bench Press BB"))
    }

    @Test
    fun theBadgeMakesAWarmUpThatMovesUpToBeNext() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.skip))
        repeat(2) { tapContaining(s(Res.string.add_set)) }
        waitUntilTrue { open().sets == 3 }
        tapDesc(s(Res.string.set_type, "3"))
        shot("15-badge-menu")
        tap(s(Res.string.kind_warmup))
        waitUntilTrue { open().exercises.single().sets[1].kind == SetKind.Warmup }
        // Working sets are numbered among themselves: 1, W, 2.
        seeDesc(s(Res.string.set_type, "W"))
        seeDesc(s(Res.string.set_type, "2"))
    }

    @Test
    fun theExerciseMenuSetsTheRestForAllSetsAndAStickyNote() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        tapDesc(s(Res.string.exercise_options))
        shot("16-exercise-menu")
        tap(s(Res.string.rest_all))
        tap("1:30")
        waitUntilTrue { open().exercises.single().sets.all { it.rest == 90 } }
        see(s(Res.string.rest_row, "1:30"))
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.sticky_note))
        type("Bench at 30°")
        tap(s(Res.string.save))
        see("Bench at 30°")
    }

    @Test
    fun theExerciseMenuSkipsAndSwaps() = flow {
        startWorkout()
        addExercises("Bench Press BB", "Squat BB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.order_skip))
        tapLastText(s(Res.string.skip)) // Squat's, the last row
        waitUntilTrue { open().exercises[1].skipped && open().sets == 1 }
        tap(s(Res.string.done))
        see(s(Res.string.skipped))
        // Swap Bench Press, the one still open, for another exercise.
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.swap))
        search("Incline Press DB")
        tap("Incline Press DB")
        waitUntilTrue { open().exercises.first().name == "Incline Press DB" }
    }

    @Test
    fun theWorkoutMenuChangesTheStartTime() = flow {
        startWorkout()
        tap(s(Res.string.workout))
        tap(s(Res.string.change_start))
        shot("17-start-time")
        val before = open().startedAt
        tap(s(Res.string.save))
        // Saved as it stood: the same minute, never later than now.
        waitUntilTrue { open().startedAt / 60_000 == before / 60_000 }
    }

    @Test
    fun theDockStepsTheRestAndLoggingStartsTheRestCard() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapDesc("${s(Res.string.field_weight)} +")
        tapDesc("${s(Res.string.field_reps)} +")
        tapDesc("${s(Res.string.rest)} +")
        see("2:15")
        tap(s(Res.string.log))
        waitUntilTrue { open().exercises.single().sets.single().rest == 135 }
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
        shot("18-rest-card")
        tap(s(Res.string.skip))
        gone(hasTestTag("rest-card"))
        // The title bar's timer starts a free rest.
        tapDesc(s(Res.string.free_rest))
        tap("1:00")
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
    }

    /** D's three states, for a person to compare with the artboards, in dark and in Increase Contrast's dark. */
    @Test
    fun designDInDark() = flow(dark = true) { designDShots("dark") }

    @Test
    fun designDInDarkHighContrast() = flow(dark = true, highContrast = true) { designDShots("darkhc") }

    private suspend fun Flow.designDShots(palette: String) {
        designD()
        shot("D-pass-$palette")
        tapDesc(s(Res.string.expand, "Incline Press DB"))
        tapDesc(s(Res.string.expand, "Lateral Raise DB"))
        test.onNode(hasScrollToIndexAction()).performScrollToKey("end")
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("focus-strip")).fetchSemanticsNodes().isNotEmpty() }
        shot("D-scroll-$palette")
        test.onNode(clickLabel(s(Res.string.back_to_focus))).performClick()
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("focus")).fetchSemanticsNodes().isNotEmpty() }
        tapDesc(s(Res.string.collapse, "Lateral Raise DB"))
        tapContaining(s(Res.string.log_set, 2))
        waitUntilTrue { open().exercises.first().sets.all { it.done } }
        tap(s(Res.string.skip))
        see(s(Res.string.now_set, "Incline Press DB", 1, 1).uppercase())
        shot("D-next-$palette")
    }

    /** Last time's Bench Press (80 × 8 @3, twice), then today's: set 1 logged at 85 × 8 @2, set 2 next, two exercises folded below. */
    private suspend fun Flow.designD() {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8", rir = "3")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        openSet(1)
        tapSegment("3")
        tap(s(Res.string.log))
        waitUntilTrue { open().logged == 2 }
        tap(s(Res.string.finish))
        tap(s(Res.string.done))
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("85", "8", rir = "2")
        // Added while Bench Press is the only exercise, so the new set copies 85 × 8 there.
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().exercises.first().sets.size == 2 }
        addExercises("Incline Press DB", "Lateral Raise DB")
        tap(s(Res.string.skip)) // the rest card; D draws the top of the workout without it
        gone(hasTestTag("rest-card"))
    }

    /** Carl's four answers (2026-11-03), each shot in dark and in Increase Contrast's dark for a person to compare with the sketches. */
    @Test
    fun aWarmUpsTypeWordTakesItsBadgesColourInDark() = flow(dark = true) { warmUpShots("dark") }

    @Test
    fun aWarmUpsTypeWordTakesItsBadgesColourInDarkHighContrast() = flow(dark = true, highContrast = true) { warmUpShots("darkhc") }

    /** A logged warm-up with its amber W, and a second warm-up next in the focus card, "UPPVÄRMNING" in the same amber. */
    private suspend fun Flow.warmUpShots(palette: String) {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("40", "10")
        tap(s(Res.string.skip)) // the rest card
        tapDesc(s(Res.string.set_type, "1"))
        tap(s(Res.string.kind_warmup))
        waitUntilTrue { open().exercises.single().sets[0].kind == SetKind.Warmup }
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        tapDesc(s(Res.string.set_type, "1"))
        tap(s(Res.string.kind_warmup))
        waitUntilTrue { open().exercises.single().sets.all { it.kind == SetKind.Warmup } }
        val word = s(Res.string.kind_warmup).uppercase()
        val label = s(Res.string.now_kind, "Bench Press BB", word).uppercase()
        see(label)
        // Only the type word is coloured; the rest of the label keeps the card's teal.
        val text = test.onAllNodes(hasText(label))[0].fetchSemanticsNode().config[SemanticsProperties.Text].first()
        assertTrue(text.spanStyles.any { it.start == text.text.lastIndexOf(word) && it.end == text.text.length && it.item.color != Color.Unspecified })
        shot("warmup-focus-$palette")
    }

    @Test
    fun failureSetsAreNumberedWithTheWorkingSetsInDark() = flow(dark = true) { failureShots("dark") }

    @Test
    fun failureSetsAreNumberedWithTheWorkingSetsInDarkHighContrast() = flow(dark = true, highContrast = true) { failureShots("darkhc") }

    private suspend fun Flow.failureShots(palette: String) {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.skip))
        repeat(2) { tapContaining(s(Res.string.add_set)) }
        waitUntilTrue { open().sets == 3 }
        tapDesc(s(Res.string.set_type, "2"))
        tap(s(Res.string.kind_failure))
        waitUntilTrue { open().exercises.single().sets[1].kind == SetKind.Failure }
        // A failure set is a working set (Q31): 1, F, then 3, not 2.
        seeDesc(s(Res.string.set_type, "F"))
        seeDesc(s(Res.string.set_type, "3"))
        see(s(Res.string.now_kind, "Bench Press BB", s(Res.string.kind_failure)).uppercase())
        shot("numbered-failure-$palette")
    }

    @Test
    fun aRestLineChangesItsRestAndTheRestCardFollowsInDark() = flow(dark = true) { restLineShots("dark") }

    @Test
    fun aRestLineChangesItsRestAndTheRestCardFollowsInDarkHighContrast() = flow(dark = true, highContrast = true) { restLineShots("darkhc") }

    /** Set 1 logged, its rest counting down in the T5 card; its line between the sets stays still and opens the dock on the rest. */
    private suspend fun Flow.restLineShots(palette: String) {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
        // The card counts in real time; stretched, it cannot run out before the edit on a slow machine.
        graph.rest.value = graph.rest.value!!.copy(seconds = 3_600)
        tapDesc(s(Res.string.rest_change, "1"))
        keys("2000")
        see("20:00")
        shot("rest-edit-$palette")
        tap(s(Res.string.save))
        // Changed, not logged again: set 2 is still to do.
        waitUntilTrue { open().exercises.single().sets.let { it[0].rest == 1_200 && it[0].done && !it[1].done } }
        see(s(Res.string.rest_row, "20:00"))
        // The card was counting down that very rest, so it follows the new length.
        assertEquals(1_200, graph.rest.value?.seconds)
    }

    @Test
    fun aSupersetStandsOpenInFocusAndFoldsIntoOneCardInDark() = flow(dark = true) { supersetShots("dark") }

    @Test
    fun aSupersetStandsOpenInFocusAndFoldsIntoOneCardInDarkHighContrast() = flow(dark = true, highContrast = true) { supersetShots("darkhc") }

    /** Queue item 7's picker with Superset (2), then G2 with the edge line: two rounds in the order you lift, Bench Press folded below. */
    private suspend fun Flow.supersetShots(palette: String) {
        startWorkout()
        tap(s(Res.string.add_exercises))
        for (name in listOf("Incline Press DB", "Lateral Raise DB")) {
            search(name)
            tap(name)
        }
        see(s(Res.string.superset_count, 2))
        shot("picker-superset-$palette")
        tap(s(Res.string.superset_count, 2))
        waitUntilTrue { open().exercises.let { it.size == 2 && it[0].superset != null && it[0].superset == it[1].superset } }
        addExercises("Bench Press BB")
        addSetTo(0) // A's
        addSetTo(1) // B's
        waitUntilTrue { open().exercises.take(2).all { it.sets.size == 2 } }
        see(s(Res.string.superset_rest, "2:00"))
        see(s(Res.string.now_set, "A · Incline Press DB", 1, 2).uppercase())
        shot("superset-open-$palette")
        tapDesc(s(Res.string.collapse, "Incline Press DB"))
        seeDesc(s(Res.string.expand, "Lateral Raise DB"))
        seeDesc(s(Res.string.expand, "Bench Press BB"))
        shot("superset-folded-$palette")
    }

    @Test
    fun aSupersetRoundsRestStartsOnlyOnceBothSetsAreLoggedInEitherOrder() = flow {
        startWorkout()
        addSuperset("Incline Press DB", "Lateral Raise DB")
        addSetTo(0)
        addSetTo(1)
        waitUntilTrue { open().exercises.all { it.sets.size == 2 } }
        // One list in the order you lift: A 1, B 1, the rest, A 2, B 2.
        assertEquals(listOf("set Incline Press DB 1", "set Lateral Raise DB 1", "set Incline Press DB 2", "set Lateral Raise DB 2"),
            setRows().fetchSemanticsNodes().map { it.config[SemanticsProperties.TestTag] })
        // B 1 first: A 1 is still to do, so no rest, and the focus stays on A 1.
        logSet("set Lateral Raise DB 1", "10", "12")
        waitUntilTrue { open().exercises[1].sets[0].done }
        assertNull(graph.rest.value)
        gone(hasTestTag("rest-card"))
        see(s(Res.string.now_set, "A · Incline Press DB", 1, 2).uppercase())
        // Then A 1: the round is done, its rest starts, and the focus moves on to A 2.
        logSet("set Incline Press DB 1", "26", "10")
        waitUntilTrue { open().exercises[0].sets[0].done }
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(open().exercises[1].sets[0].id, graph.rest.value?.setId)
        see(s(Res.string.now_set, "A · Incline Press DB", 2, 2).uppercase())
    }

    @Test
    fun theExerciseMenuMakesASupersetAndTakesAnExerciseOut() = flow {
        startWorkout()
        addExercises("Bench Press BB", "Squat BB", "Incline Press DB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.superset_create))
        tapLastText("Incline Press DB")
        // Incline Press moves up to follow Bench Press, and the two are one superset.
        waitUntilTrue {
            open().exercises.let { e -> e.map { it.name } == listOf("Bench Press BB", "Incline Press DB", "Squat BB") && e[0].superset != null && e[0].superset == e[1].superset }
        }
        // Both stand open; B's ⋯, the last, takes Incline Press out, and a superset of one is none.
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.superset_leave))
        waitUntilTrue { open().exercises.all { it.superset == null } }
    }

    @Test
    fun anEmptyWorkoutOffersQuickPicksMarkedJa() = flow {
        val bench = graph.exercises.all().first { it.isNotEmpty() }.first { it.name == "Bench Press BB" }
        graph.exercises.setMark(bench.id, Mark.Yes)
        startWorkout()
        see(s(Res.string.quick_picks))
        shot("12-quick-picks")
        test.onNode(clickLabel(s(Res.string.add_named, "Bench Press BB"))).performClick()
        waitUntilTrue { open().exercises.map { it.name } == listOf("Bench Press BB") }
        see(s(Res.string.sets_progress, 0, 1))
    }

    @Test
    fun theLibraryStillOpensFromPlan() = flow {
        tap(s(Res.string.tab_plan))
        tap(s(Res.string.library))
        see("Bench Press BB")
    }

    @Test
    fun aWorkoutLeftOpenComesBackAfterTheAppIsKilled() {
        val file = "flow-test.db"
        deleteFileDatabase(file)
        val first = fileDriver(file, StriveDatabase.Schema)
        flow(AppGraph(first)) {
            startWorkout()
            addExercises("Bench Press BB")
            logFirstSet("80", "8")
        }
        first.close() // the app is killed
        val again = fileDriver(file, StriveDatabase.Schema)
        try {
            flow(AppGraph(again)) {
                bar()
                see("1/1", substring = true)
                test.onNode(clickLabel(s(Res.string.back_to_workout))).performClick()
                seeDesc(s(Res.string.logged, 1))
            }
        } finally {
            again.close()
            deleteFileDatabase(file)
        }
    }
}

private const val TIMEOUT = 10_000L

private fun clickLabel(label: String) =
    SemanticsMatcher("click label $label") { it.config.getOrNull(SemanticsActions.OnClick)?.label == label }

/** The steps a person takes and what they check, in the words of the app. */
@OptIn(ExperimentalTestApi::class)
private class Flow(val test: SkikoComposeUiTest, val graph: AppGraph) {
    suspend fun s(res: StringResource, vararg args: Any) = getString(res, *args)

    suspend fun open(): Workout = graph.sessions.open().first()!!

    fun waitUntilTrue(check: suspend () -> Boolean) = wait("the database to agree") { runBlocking { check() } }

    fun see(text: String, substring: Boolean = false) = wait("see '$text'") { test.onAllNodes(hasText(text, substring)).fetchSemanticsNodes().isNotEmpty() }

    /** Waits, and on a timeout prints what is on screen and saves a picture of it, so a failure says what was there instead. */
    fun wait(what: String, condition: () -> Boolean) {
        try {
            test.waitUntil(what, TIMEOUT, condition)
        } catch (e: Throwable) {
            println("FAILED waiting to $what; on screen:")
            println(test.onAllNodes(isRoot()).printToString(maxDepth = 40))
            runCatching { shot("failure") }
            throw e
        }
    }

    fun seeDesc(desc: String) = test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasContentDescription(desc)).fetchSemanticsNodes().isNotEmpty() }

    /** Waits for the bar a minimised workout sits in. */
    suspend fun bar() {
        val label = s(Res.string.back_to_workout)
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(clickLabel(label)).fetchSemanticsNodes().isNotEmpty() }
    }

    fun gone(matcher: SemanticsMatcher) = wait("${matcher.description} to go") { test.onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }

    /** Taps the last clickable node that matches: what is drawn last lies on top. */
    private fun tapLast(m: SemanticsMatcher) {
        wait("tap ${m.description}") { test.onAllNodes(m).fetchSemanticsNodes().isNotEmpty() }
        test.onAllNodes(m).let { it[it.fetchSemanticsNodes().size - 1] }.performClick()
        test.waitForIdle()
    }

    // Not a text field that happens to hold the same words, such as the search field after a search.
    fun tap(text: String) = tapLast(hasText(text) and hasClickAction() and !hasSetTextAction())

    fun tapContaining(text: String) = tapLast(hasText(text, substring = true) and hasClickAction() and !hasSetTextAction())

    fun tapDesc(desc: String) = tapLast(hasContentDescription(desc))

    /** The last clickable node with exactly this text, such as a button in a dialog drawn over a row with the same word. */
    fun tapLastText(text: String) = tapLast(hasText(text) and hasClickAction())

    fun tapSegment(label: String) = tapLast(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))

    fun keys(typed: String) = typed.forEach {
        test.onNodeWithTag("key-$it").performClick()
        test.waitForIdle()
    }

    fun type(text: String) {
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        test.onAllNodes(hasSetTextAction()).let { it[it.fetchSemanticsNodes().size - 1] }.performTextReplacement(text)
        test.waitForIdle()
    }

    /** The set rows on screen, in order; each carries the tag "set <exercise> <number>". */
    fun setRows() = test.onAllNodes(SemanticsMatcher("set row") {
        val tag = it.config.getOrNull(SemanticsProperties.TestTag) ?: ""
        tag.startsWith("set ") && " cell " !in tag
    })

    fun openSet(index: Int) {
        test.waitUntil(timeoutMillis = TIMEOUT) { setRows().fetchSemanticsNodes().size > index }
        setRows()[index].performClick()
        test.waitForIdle()
    }

    fun search(name: String) {
        type(name)
        see(name)
    }

    suspend fun startWorkout() {
        tap(s(Res.string.tab_start))
        tap(s(Res.string.start_workout))
        see(s(Res.string.finish))
    }

    suspend fun addExercises(vararg names: String) {
        tap(s(Res.string.add_exercises))
        for (name in names) {
            search(name)
            tap(name)
        }
        val before = open().exercises.size
        tap(s(Res.string.add_count, names.size))
        // The database, not the screen: an exercise far down the list is not drawn until scrolled to.
        waitUntilTrue { open().exercises.size == before + names.size }
    }

    suspend fun addSuperset(vararg names: String) {
        tap(s(Res.string.add_exercises))
        for (name in names) {
            search(name)
            tap(name)
        }
        val before = open().exercises.size
        tap(s(Res.string.superset_count, names.size))
        waitUntilTrue { open().exercises.size == before + names.size }
    }

    /** Taps the [index]th open exercise's Lägg till set. */
    suspend fun addSetTo(index: Int) {
        val add = hasText(s(Res.string.add_set), substring = true) and hasClickAction()
        wait("add set $index") { test.onAllNodes(add).fetchSemanticsNodes().size > index }
        test.onAllNodes(add)[index].performClick()
        test.waitForIdle()
    }

    /** Opens the set row tagged [tag] and logs it through the dock. */
    suspend fun logSet(tag: String, weight: String, reps: String) {
        test.onNodeWithTag(tag).performClick()
        test.waitForIdle()
        tapDesc(s(Res.string.field_weight))
        keys(weight)
        tap(s(Res.string.next))
        keys(reps)
        tap(s(Res.string.next))
        tap(s(Res.string.log_next))
    }

    suspend fun logFirstSet(weight: String, reps: String, rir: String? = null) {
        openSet(0)
        rir?.let { tapSegment(it) }
        tapDesc(s(Res.string.field_weight))
        keys(weight)
        tap(s(Res.string.next))
        keys(reps)
        tap(s(Res.string.next)) // to the rest
        val before = open().logged
        tap(s(Res.string.log_next))
        waitUntilTrue { open().logged > before }
    }

    /** A picture of the screen for a person to look at; the tests never compare them. */
    @OptIn(ExperimentalForeignApi::class)
    fun shot(name: String) {
        test.waitForIdle()
        val bytes = Image.makeFromBitmap(test.onAllNodes(isRoot())[0].captureToImage().asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
        NSFileManager.defaultManager.createDirectoryAtPath(SHOTS, true, null, null)
        bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()).writeToFile("$SHOTS/$name.png", true) }
    }
}

// shortcut: screenshots go to a fixed folder outside the repo, for a person to read; pass a path from Gradle if CI should keep them.
private const val SHOTS = "/tmp/strive-flow-shots"