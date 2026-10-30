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
    private fun flow(graph: AppGraph = AppGraph(memoryDriver()), dark: Boolean? = null, block: suspend Flow.() -> Unit) =
        runSkikoComposeUiTest(Size(1284f, 2778f), Density(3f)) {
            setContent { CompositionLocalProvider(LocalDarkTheme provides dark) { App(graph) } }
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
        tap(s(Res.string.log_next))
        tap(s(Res.string.add_set))
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
        see(s(Res.string.empty_workout))
        see("0/0", substring = true)
        shot("01-empty-workout")
    }

    @Test
    fun thePickerAddsExercisesInTheOrderTicked() = flow {
        startWorkout()
        addExercises("Chest Dips", "Bench Press BB")
        assertEquals(listOf("Chest Dips", "Bench Press BB"), open().exercises.map { it.name })
        assertEquals(listOf(1, 1), open().exercises.map { it.sets.size })
    }

    @Test
    fun cancelInThePickerAddsNothing() = flow {
        startWorkout()
        tap(s(Res.string.add_exercises))
        search("Bench Press BB")
        tap("Bench Press BB")
        tap(s(Res.string.cancel))
        see(s(Res.string.empty_workout))
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
        tap(s(Res.string.log_next))
        see("80 × 8")
        see("1/1", substring = true)
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
        see("5 × 2 @2")
    }

    @Test
    fun rirFourPlusOpensFourToTenAndShowsThePick() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapSegment("4+")
        tapSegment("7")
        gone(hasText("4+"))
        tapDesc("${s(Res.string.field_weight)} +")
        tapDesc("${s(Res.string.field_reps)} +")
        tap(s(Res.string.log))
        see("2,5 × 1 @7".let { if (s(Res.string.field_weight).startsWith("Vikt")) it else it.replace(',', '.') })
        assertEquals(7, open().exercises.single().sets.single().rir)
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
        // Reps is empty, so Logga opens the keypad on it rather than logging half a set.
        see(s(Res.string.log_next))
        assertEquals(false, open().exercises.single().sets.single().done)
    }

    @Test
    fun aNewSetCopiesTheLastAndItsCheckLogsItInOneTap() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.add_set))
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
        openSetWithText("80 × 8")
        tapDesc("${s(Res.string.field_reps)} +")
        tap(s(Res.string.save_change))
        see("80 × 9")
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
        tap(s(Res.string.add_set))
        waitUntilTrue { open().exercises.single().sets.size == 2 }
        rows("–")[1].performTouchInput { swipeLeft(startX = right - 10f, endX = left + 10f) }
        waitUntilTrue { open().exercises.single().sets.size == 1 }
    }

    @Test
    fun aShortSwipeShowsDeleteAndATapOnItDeletes() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        rows("–")[0].performTouchInput { swipeLeft(startX = right - 10f, endX = right - 400f) }
        see(s(Res.string.delete))
        // The swipe began on the set's ✓ and ended over the row: neither may count as a tap.
        gone(hasContentDescription(s(Res.string.field_weight)))
        tap(s(Res.string.delete))
        waitUntilTrue { open().exercises.single().sets.isEmpty() }
    }

    @Test
    fun aTapOnARowSwipedOpenShutsItInsteadOfOpeningTheDock() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        rows("–")[0].performTouchInput { swipeLeft(startX = centerX, endX = centerX - 400f) }
        see(s(Res.string.delete))
        rows("–")[0].performClick()
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
        tap(s(Res.string.add_set))
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
        tap(s(Res.string.add_set))
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
        see(s(Res.string.add_set))
    }

    @Test
    fun theBarBringsTheWorkoutBack() = flow {
        startWorkout()
        tapDesc(s(Res.string.minimise))
        bar()
        test.onNode(clickLabel(s(Res.string.back_to_workout))).performClick()
        see(s(Res.string.empty_workout))
    }

    @Test
    fun historyStartsEmptyAndOffersAWorkout() = flow {
        tap(s(Res.string.tab_you))
        tap(s(Res.string.all_activities))
        see(s(Res.string.no_workouts))
        tap(s(Res.string.start_a_workout))
        see(s(Res.string.empty_workout))
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
        tap(s(Res.string.log_next))
        see("0:45")
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
                see("80 × 8")
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

    fun rows(text: String) = test.onAllNodes(hasText(text) and hasClickAction() and !hasSetTextAction())

    fun openSet(index: Int) {
        see("–")
        rows("–")[index].performClick()
        test.waitForIdle()
    }

    fun openSetWithText(text: String) {
        see(text)
        rows(text)[0].performClick()
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
        tap(s(Res.string.add_count, names.size))
        for (name in names) see(name)
    }

    suspend fun logFirstSet(weight: String, reps: String) {
        openSet(0)
        tapDesc(s(Res.string.field_weight))
        keys(weight)
        tap(s(Res.string.next))
        keys(reps)
        tap(s(Res.string.log_next))
        see("$weight × $reps")
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