package se.carlsmeister.strive.flows

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.material3.ButtonDefaults
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import androidx.compose.runtime.CompositionLocalProvider
import se.carlsmeister.strive.App
import se.carlsmeister.strive.boards.savePng
import se.carlsmeister.strive.theme.LocalDarkTheme
import se.carlsmeister.strive.theme.LocalHighContrast
import se.carlsmeister.strive.theme.LocalReduceMotionSetting
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.theme.Scheme
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.deleteFileDatabase
import se.carlsmeister.strive.data.fileDriver
import se.carlsmeister.strive.data.memoryDriver
import se.carlsmeister.strive.db.StriveDatabase
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.exercise_settings
import strive.shared.generated.resources.exercise_details
import strive.shared.generated.resources.tab_about
import strive.shared.generated.resources.tab_history
import strive.shared.generated.resources.tab_chart
import strive.shared.generated.resources.tab_records
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
import strive.shared.generated.resources.workout_options
import strive.shared.generated.resources.mark_none
import strive.shared.generated.resources.field_reps
import strive.shared.generated.resources.field_seconds
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
import strive.shared.generated.resources.exercise_removed
import strive.shared.generated.resources.remove_exercise
import strive.shared.generated.resources.rename
import strive.shared.generated.resources.restored
import strive.shared.generated.resources.undo
import strive.shared.generated.resources.rir
import strive.shared.generated.resources.save
import strive.shared.generated.resources.save_change
import strive.shared.generated.resources.save_without
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.start_a_workout
import strive.shared.generated.resources.bubble_continue
import strive.shared.generated.resources.bubble_empty_desc
import strive.shared.generated.resources.close_start
import strive.shared.generated.resources.start_run
import strive.shared.generated.resources.tab_food
import strive.shared.generated.resources.food_later_title
import strive.shared.generated.resources.food_log_later
import se.carlsmeister.strive.boards.BOARD_SHOTS
import se.carlsmeister.strive.boards.inEnglish
import se.carlsmeister.strive.boards.clearLeftOverLanguage
import strive.shared.generated.resources.next_screen
import androidx.compose.runtime.DisposableEffect
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import strive.shared.generated.resources.tab_home
import strive.shared.generated.resources.tab_plan
import strive.shared.generated.resources.tab_start
import strive.shared.generated.resources.tab_you
import strive.shared.generated.resources.unlogged
import strive.shared.generated.resources.workout
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToKey
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.data.WeightUnit
import se.carlsmeister.strive.workout.grouped
import kotlin.math.roundToLong
import strive.shared.generated.resources.add_named
import strive.shared.generated.resources.collapse
import strive.shared.generated.resources.edit
import strive.shared.generated.resources.expand
import strive.shared.generated.resources.now_set
import strive.shared.generated.resources.now_set_kind
import strive.shared.generated.resources.quick_picks
import strive.shared.generated.resources.sets_progress
import strive.shared.generated.resources.time_sets
import strive.shared.generated.resources.what_today
import strive.shared.generated.resources.back_to_focus
import strive.shared.generated.resources.change_start
import strive.shared.generated.resources.hour
import strive.shared.generated.resources.minute
import strive.shared.generated.resources.free_rest
import strive.shared.generated.resources.kind_warmup
import strive.shared.generated.resources.kind_normal
import strive.shared.generated.resources.kind_drop
import strive.shared.generated.resources.order_skip
import strive.shared.generated.resources.rest
import strive.shared.generated.resources.rest_all
import strive.shared.generated.resources.rest_row
import strive.shared.generated.resources.set_type
import strive.shared.generated.resources.set_failure
import strive.shared.generated.resources.skip
import strive.shared.generated.resources.skipped
import strive.shared.generated.resources.sticky_note
import strive.shared.generated.resources.clear_text
import strive.shared.generated.resources.custom_time
import strive.shared.generated.resources.swap
import strive.shared.generated.resources.up_by
import strive.shared.generated.resources.rest_after
import strive.shared.generated.resources.last_set
import strive.shared.generated.resources.hold_last_set
import strive.shared.generated.resources.kind_failure
import strive.shared.generated.resources.now_kind
import strive.shared.generated.resources.rest_change
import strive.shared.generated.resources.dock_rest_after
import strive.shared.generated.resources.dock_set
import strive.shared.generated.resources.superset_count
import strive.shared.generated.resources.superset_create
import strive.shared.generated.resources.superset_leave
import strive.shared.generated.resources.superset_rest
import androidx.compose.ui.graphics.Color
import se.carlsmeister.strive.workout.wallClock
import se.carlsmeister.strive.workout.now
import androidx.compose.ui.test.hasAnyAncestor
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.fixture.SampleData
import strive.shared.generated.resources.bar_left_open
import strive.shared.generated.resources.tap_to_continue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import androidx.compose.ui.test.performScrollToIndex
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
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
    private fun flow(graph: AppGraph = AppGraph(memoryDriver()), dark: Boolean? = null, highContrast: Boolean? = null, reduceMotion: Boolean? = null, block: suspend Flow.() -> Unit) {
        // iOS runs every test class in one process: a clock another test left pinned would freeze this flow's rest timers.
        check(wallClock === Clock.System) { "a test left the clock pinned" }
        clearLeftOverLanguage()
        try {
            runSkikoComposeUiTest(Size(1284f, 2778f), Density(3f)) {
                val back = DirectNavigationEventInput()
                setContent {
                    // The system's back, as a gesture or button sends it, through the host's own dispatcher.
                    val dispatcher = LocalNavigationEventDispatcherOwner.current!!.navigationEventDispatcher
                    DisposableEffect(dispatcher) {
                        dispatcher.addInput(back)
                        onDispose { dispatcher.removeInput(back) }
                    }
                    CompositionLocalProvider(LocalDarkTheme provides dark, LocalHighContrast provides highContrast, LocalReduceMotionSetting provides reduceMotion) {
                        App(graph)
                    }
                }
                Flow(this, graph, back).block()
            }
        } finally {
            graph.close() // the toasts' timers run on the graph's scope (P5)
        }
    }

    /**
     * The sample's Push A (Bench Press BB's warm-up and set 1 logged, set 2 next, four exercises after it), opened from its bar. The
     * sample pins the clock, so it is let go after.
     */
    private fun pushA(dark: Boolean? = null, highContrast: Boolean? = null, reduceMotion: Boolean? = null, block: suspend Flow.() -> Unit) =
        flow(dark = dark, highContrast = highContrast, reduceMotion = reduceMotion) {
        try {
            SampleData.load(graph)
            bar()
            test.onNode(clickLabel(s(Res.string.back_to_workout))).performClick()
            see(s(Res.string.finish))
            block()
        } finally {
            wallClock = Clock.System
        }
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
        tapDesc(weightLabel("kg"))
        shot("dark-04-keypad")
        keys("80")
        tap(s(Res.string.next))
        keys("8")
        tap(s(Res.string.next))
        tap(s(Res.string.log_next))
        // Its only set logged, the workout is complete and Bench Press folds into the overview (WO-01-logged).
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        // A clock that stands still, so the bar's time reads "0:00" for as long as the test looks.
        val start = Clock.System.now()
        wallClock = object : Clock {
            override fun now() = start
        }
        try {
            startWorkout()
            see(s(Res.string.what_today))
            // With no working sets the bar reads the time alone (P10), not "0:00 · 0/0 sets".
            see("0:00")
            gone(hasText("0/0", substring = true))
            shot("01-empty-workout")
        } finally {
            wallClock = Clock.System
        }
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
        tapDesc(weightLabel("kg"))
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
        tapDesc(weightLabel("kg"))
        keys("805⌫")
        tap(s(Res.string.hide))
        // The keypad is gone, the steppers are back, and the field kept what was typed.
        seeDesc("${weightLabel("kg")} +")
        see("80")
    }

    @Test
    fun stepsAndRirLogFromTheDock() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        repeat(2) { tapDesc("${weightLabel("kg")} +") } // 2,5 kg a step
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
        tapDesc("${weightLabel("kg")} +")
        tapDesc("${s(Res.string.field_reps)} +")
        tap(s(Res.string.log))
        waitUntilTrue { open().exercises.single().sets.single().rir == 7 }
    }

    @Test
    fun anEmptySetsCheckOpensTheDockInsteadOfLogging() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.log_set, 1))
        seeDesc(weightLabel("kg"))
        assertEquals(false, open().exercises.single().sets.single().done)
    }

    @Test
    fun logWithAValueMissingGoesToTheEmptyField() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapDesc("${weightLabel("kg")} +")
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        gone(hasContentDescription(weightLabel("kg")))
    }

    @Test
    fun theDockClosesWhenItsGrabberIsDraggedDown() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        test.onNodeWithContentDescription(s(Res.string.close)).performTouchInput { swipeDown(endY = centerY + 900f) }
        gone(hasContentDescription(weightLabel("kg")))
    }

    @Test
    fun theGrabbersCloseActionClosesTheDockForVoiceOver() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        // VoiceOver does not tap the grabber, it performs its action.
        test.onNodeWithContentDescription(s(Res.string.close)).performSemanticsAction(SemanticsActions.OnClick)
        gone(hasContentDescription(weightLabel("kg")))
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
        gone(hasContentDescription(weightLabel("kg")))
        tap(s(Res.string.delete))
        waitUntilTrue { open().exercises.single().sets.isEmpty() }
    }

    /**
     * A quick flick left shorter than 88 pt rests open on Delete: 150 px (50 pt) in 50 ms is about 3 000 px/s, past the 300 pt/s
     * (900 px/s) flick, and short of the 88 pt that opens on distance alone. Only the release velocity can open it.
     */
    @Test
    fun aQuickShortFlickLeftShowsDelete() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        setRows()[0].performTouchInput { swipeLeft(startX = centerX, endX = centerX - 150f, durationMillis = 50) }
        idle() // settled: shut, or open on Delete
        see(s(Res.string.delete))
    }

    @Test
    fun aTapOnARowSwipedOpenShutsItInsteadOfOpeningTheDock() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        setRows()[0].performTouchInput { swipeLeft(startX = centerX, endX = centerX - 400f) }
        see(s(Res.string.delete))
        setRows()[0].performClick()
        gone(hasText(s(Res.string.delete)))
        gone(hasContentDescription(weightLabel("kg")))
        assertEquals(1, open().exercises.single().sets.size)
    }

    @Test
    fun theExerciseMenuAddsANoteAndRemovesTheExercise() = flow {
        graph.toastHoldMillis = 300 // the removal lands when the toast ends
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.note))
        type("Elbows in")
        waitUntilTrue { open().exercises.single().note == "Elbows in" }
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.remove_exercise))
        see(s(Res.string.exercise_removed, "Bench Press BB"))
        waitUntilTrue { open().exercises.isEmpty() }
    }

    /**
     * Criterion 4: Undo on the removed toast puts the exercise back on screen where it was, its logged sets shown and the focus on its
     * set 2 again; and once the toast's hold has passed, the exercise is still in the database, so Undo cancelled the removal (P4).
     */
    @Test
    fun undoBringsARemovedExerciseBackInPlace() = pushA {
        // A hold that ends, read at the show: long enough to tap Undo, short enough to wait past.
        val hold = 5_000.milliseconds
        graph.toastHoldMillis = hold.inWholeMilliseconds
        tapDesc(s(Res.string.exercise_options)) // Bench's, the one exercise standing open
        tap(s(Res.string.remove_exercise))
        val shown = TimeSource.Monotonic.markNow()
        see(s(Res.string.exercise_removed, "Bench Press BB"))
        gone(hasText("Bench Press BB"))
        tap(s(Res.string.undo))
        gone(hasText(s(Res.string.exercise_removed, "Bench Press BB")))

        // On screen: Bench's header first, above every other exercise; its warm-up and set 1 logged; the focus on its set 2.
        val header = hasText("Bench Press BB") and hasClickAction()
        wait("Bench's header") { test.onAllNodes(header).fetchSemanticsNodes().isNotEmpty() }
        val top = test.onNode(header).fetchSemanticsNode().boundsInRoot.top
        assertTrue(test.onAllNodes(hasText("Incline Press DB", substring = true)).fetchSemanticsNodes().all { it.boundsInRoot.top > top })
        for (row in 1..2) test.onNode(hasContentDescription(s(Res.string.logged)) and hasAnyAncestor(hasTestTag("set Bench Press BB $row"))).assertExists()
        test.onNode(hasText(s(Res.string.log_set, "2")) and hasAnyAncestor(hasTestTag("focus"))).assertExists()
        test.onNode(hasText("Bench Press BB", substring = true, ignoreCase = true) and hasAnyAncestor(hasTestTag("focus"))).assertExists()

        // Past the hold, the removal it would have made never lands.
        wait("the toast's hold to pass") { shown.elapsedNow() > hold + 1_000.milliseconds }
        val bench = open().exercises.first()
        assertEquals("Bench Press BB", bench.name)
        assertEquals(2, bench.sets.count { it.done }) // the warm-up and set 1
        see("Bench Press BB")
    }

    /** Hardening H1: a name too long for the toast's line gives way to Undo, which keeps its full tap target and still undoes. */
    @Test
    fun undoStaysTappableWhenTheRemovedNameIsTooLongForTheToast() = flow {
        val name = "Standing Bent-Over Two-Arm Triceps Extension"
        graph.toastHoldMillis = Long.MAX_VALUE // only Undo ends this toast
        startWorkout()
        addExercises(name)
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.remove_exercise))
        see(s(Res.string.exercise_removed, name))
        val undo = test.onNode(hasText(s(Res.string.undo)) and hasClickAction()).fetchSemanticsNode()
        val touch = with(test.density) { 44.dp.toPx() }
        assertTrue(undo.boundsInRoot.width >= touch, "Undo is ${undo.boundsInRoot.width} px wide, under 44 pt")
        tap(s(Res.string.undo))
        gone(hasText(s(Res.string.exercise_removed, name)))
        assertEquals(listOf(name), open().exercises.map { it.name })
        see(name, substring = true)
    }

    /** Review Focus 1 (P4): finishing while the removed toast still holds settles the removal first, so the summary leaves the exercise out. */
    @Test
    fun finishingDuringTheUndoToastLeavesTheExerciseOut() = pushA {
        graph.toastHoldMillis = Long.MAX_VALUE // only the finish ends this toast
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.remove_exercise))
        see(s(Res.string.exercise_removed, "Bench Press BB"))
        tap(s(Res.string.finish))
        tap(s(Res.string.save_without)) // the other exercises' sets are not all logged
        see(s(Res.string.done))
        gone(hasText("Bench Press BB", substring = true))
        assertTrue(graph.sessions.history().first().first().exercises.none { it.name == "Bench Press BB" })
    }

    /**
     * Each of the five menus the built screens open, shot as `menu-<name>-dark` beside the Menus card: drawn in English, as the
     * board is, while the flow still reads its texts from the app's strings.
     */
    @Test
    fun everyMenuIsTheGlassPullDown() = inEnglish {
        flow(dark = true) {
            startWorkout(); addExercises("Bench Press BB")
            for ((button, name) in listOf(Res.string.exercise_options to "exercise", Res.string.workout_options to "workout", Res.string.free_rest to "rest")) {
                tapDesc(s(button)); openMenu(name)
            }
            tapDesc(s(Res.string.set_type, "1")); openMenu("set")          // the set badge
            tapDesc(s(Res.string.minimise)); bar()
            tap(s(Res.string.tab_plan)); tap(s(Res.string.library)); search("Bench Press BB"); tap("Bench Press BB")
            tap(s(Res.string.mark_none)); openMenu("mark")                  // In my programmes; a fresh graph has no mark set
        }
    }

    @Test
    fun aMenuOpenedAtTheBottomOfTheScreenStaysOnIt() = flow {
        startWorkout(); addExercises("Bench Press BB")
        // Through the database: the Add set row is a lazy item that leaves the screen once the rows fill it.
        repeat(12) { graph.sessions.addSet(open().exercises.single().id) }
        waitUntilTrue { open().sets == 13 }
        // Six rows, under the header and the focus card, reach the bottom of the flow's 926 dp screen.
        wait("the rows to fill the screen") { setRows().fetchSemanticsNodes().size >= 6 }
        // The lowest badge on screen: the last node described as a set's type that lies wholly inside the screen.
        val screen = test.onAllNodes(isRoot())[0].fetchSemanticsNode().boundsInRoot
        val badgePrefix = s(Res.string.set_type, "")
        val badges = test.onAllNodes(SemanticsMatcher("set badge on screen") {
            (it.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull() ?: "").startsWith(badgePrefix) && it.boundsInRoot.bottom <= screen.bottom
        })
        val lowest = badges[badges.fetchSemanticsNodes().lastIndex]
        val badge = lowest.fetchSemanticsNode().boundsInRoot
        lowest.performClick()
        wait("the menu") { test.onAllNodes(hasTestTag("glass-menu")).fetchSemanticsNodes().size == 1 }
        val menu = test.onNode(hasTestTag("glass-menu")).fetchSemanticsNode().boundsInRoot
        val root = test.onAllNodes(isRoot())[0].fetchSemanticsNode().boundsInRoot   // the screen: a popup is a second root
        assertTrue(menu.top >= root.top && menu.bottom <= root.bottom && menu.left >= root.left && menu.right <= root.right, "$menu inside $root")
        // Opened above its badge: otherwise the badge sat high enough for the menu to fit below, and nothing here was at the bottom.
        assertTrue(menu.bottom <= badge.top, "$menu above $badge")
    }

    @Test
    fun backClosesAMenuAndLeavesTheScreenWhereItWas() = flow {
        tap(s(Res.string.tab_plan)); tap(s(Res.string.library)); search("Bench Press BB"); tap("Bench Press BB")
        tap(s(Res.string.mark_none))
        wait("the menu") { test.onAllNodes(hasTestTag("glass-menu")).fetchSemanticsNodes().size == 1 }
        back()
        gone(hasTestTag("glass-menu"))
        see(s(Res.string.mark_none))   // still the exercise, not the library behind it
    }

    @Test
    fun theWorkoutMenuRenamesAndKeepsANote() = flow {
        startWorkout()
        tap(s(Res.string.workout))
        tap(s(Res.string.rename))
        type("Push A")
        shot("18-rename-prompt")
        tap(s(Res.string.save))
        see("Push A")
        tap("Push A")
        tap(s(Res.string.session_note))
        type("Slept badly")
        tap(s(Res.string.save))
        waitUntilTrue { open().name == "Push A" && open().note == "Slept badly" }
    }

    /** WO-08-note-empty: a workout without a note offers what to write in it, and says where it shows. In English, as the board. */
    @Test
    fun theNotePromptShowsItsPlaceholderAndHint() = inEnglish {
        flow {
            startWorkout()
            tap(s(Res.string.workout))
            tap(s(Res.string.session_note))
            see("Energy, sleep, pain…")
            see("Shown in the summary and in History.")
        }
    }

    /** The sticky note's clear glyph empties the field, and Save then keeps the note empty. */
    @Test
    fun theClearGlyphEmptiesAStickyNote() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.sticky_note))
        type("Arch, feet planted.")
        tap(s(Res.string.save))
        waitUntilTrue { open().exercises.single().pinned == "Arch, feet planted." }
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.sticky_note))
        tapDesc(s(Res.string.clear_text))
        gone(hasSetTextAction() and hasText("Arch, feet planted."))
        tap(s(Res.string.save))
        waitUntilTrue { open().exercises.single().pinned == "" }
    }

    /** WO-07-custom keeps its own look through the shared prompt: no clear glyph once a time is typed. The time still starts a rest. */
    @Test
    fun theCustomRestTimeHasNoClearGlyph() = flow {
        startWorkout()
        tapDesc(s(Res.string.free_rest))
        tap(s(Res.string.custom_time))
        type("1:30")
        assertTrue(test.onAllNodes(hasContentDescription(s(Res.string.clear_text))).fetchSemanticsNodes().isEmpty(), "no clear glyph")
        tap(s(Res.string.save))
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
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
        seeDesc(s(Res.string.tab_start))
        assertNull(graph.sessions.open().first())
    }

    @Test
    fun finishingWithSetsLeftAsksAndLogsThemAsTheyStand() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        seeDesc(s(Res.string.tab_start))
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

    /** A tap on a prompt's title is a tap on its panel, not on the scrim: the prompt stays open with what was typed. */
    @Test
    fun aTapOnAPromptsTitleKeepsWhatWasTyped() = flow {
        startWorkout()
        tap(s(Res.string.workout))
        tap(s(Res.string.rename))
        type("Push A")
        tapOnText(s(Res.string.rename))
        assertTrue(test.onAllNodes(hasSetTextAction() and hasText("Push A")).fetchSemanticsNodes().isNotEmpty(), "the prompt, still holding Push A")
    }

    /** A tap on a header sheet's title leaves the sheet open, its choice unsaved but still chosen. */
    @Test
    fun aTapOnASheetsTitleKeepsItsChoice() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.exercise_settings))
        tapSegment("5")
        tapOnText(s(Res.string.exercise_settings))
        test.onAllNodes(hasText("5") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).onLast().assertIsSelected()
    }

    /** A tap on an action sheet's header row leaves the sheet open. */
    @Test
    fun aTapOnAnActionSheetsHeaderKeepsItOpen() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.finish))
        val header = getPluralString(Res.plurals.unlogged, 1, 1)
        see(header)
        tapOnText(header)
        assertTrue(test.onAllNodes(hasText(s(Res.string.keep_going))).fetchSemanticsNodes().isNotEmpty(), "the sheet, still open")
    }

    /** In the simulator's language, the discard alert's two labels are each one line and inside their buttons: stacked if need be. */
    @Test
    fun theDiscardAlertsLabelsStayOnOneLine() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.discard_workout))
        see(s(Res.string.discard_question))
        val inAlert = hasAnyAncestor(hasTestTag("glass-alert"))
        for (label in listOf(s(Res.string.cancel), s(Res.string.discard_workout))) {
            val text = test.onNode(hasText(label) and inAlert, useUnmergedTree = true).fetchSemanticsNode()
            val layouts = mutableListOf<TextLayoutResult>()
            text.config[SemanticsActions.GetTextLayoutResult].action!!(layouts)
            val button = test.onNode(hasText(label) and hasClickAction() and inAlert).fetchSemanticsNode().boundsInRoot
            // Its drawn height, not the layout's line count: the layout the action hands back measured the label at its widest.
            val oneLine = layouts.single().layoutInput.let { with(it.density) { it.style.lineHeight.toPx() } }
            assertTrue(text.boundsInRoot.height <= oneLine + 1, "'$label' one line (${oneLine}px) tall: ${text.boundsInRoot}")
            assertTrue(text.boundsInRoot.width <= button.width, "'$label' (${text.boundsInRoot}) inside its button ($button)")
        }
    }

    @Test
    fun discardFromTheListAsksFirst() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tap(s(Res.string.discard_workout))
        see(s(Res.string.discard_question))
        shot("19-discard-alert")
        tap(s(Res.string.cancel))
        open()
        tap(s(Res.string.discard_workout))
        see(s(Res.string.discard_question))
        tap(s(Res.string.discard_workout)) // the alert's button is drawn last
        seeDesc(s(Res.string.tab_start))
        assertNull(graph.sessions.open().first())
    }

    @Test
    fun minimisingLeavesTheWorkoutInTheBarAndStartContinuesIt() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        tapDesc(s(Res.string.minimise))
        bar()
        shot("07-minimised")
        tapDesc(s(Res.string.tab_start))
        // Two workouts cannot run at once: Start only continues this one.
        gone(hasContentDescription(s(Res.string.bubble_empty_desc)))
        gone(hasContentDescription(s(Res.string.start_run)))
        tapDescContaining(s(Res.string.bubble_continue))
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

    /** Past a third of the screen, a drag down the title bar minimises (criterion 8, P9). */
    @Test
    fun draggingTheTitleBarDownMinimises() = flow {
        startWorkout()
        dragTheTitleBarDown(0.4f, millis = 600) // about 1 850 px/s, under the flick: only the distance minimises
        barAboveTheTabs()
    }

    /**
     * Short of a third, a flick minimises (criterion 8): 15 % in 80 ms is about 5 200 px/s at 3x, past the flick's 1000 pt/s (3 000 px/s).
     * It passes only if the release velocity reaches `minimises` downward-positive and in px, as the drag does.
     */
    @Test
    fun flickingTheTitleBarDownMinimises() = flow {
        startWorkout()
        dragTheTitleBarDown(0.15f, millis = 80)
        barAboveTheTabs()
    }

    /** Under Reduce Motion the screen does not scale and minimising cross-fades (P9), but the drag still minimises. */
    @Test
    fun draggingTheTitleBarDownMinimisesUnderReduceMotion() = flow(reduceMotion = true) {
        startWorkout()
        dragTheTitleBarDown(0.4f, millis = 600)
        barAboveTheTabs()
    }

    /** Short of a third and slow, the drag springs back: the workout stays, its title bar where it started. */
    @Test
    fun aShortDragOnTheTitleBarSpringsBack() = flow {
        startWorkout()
        val top = test.onNodeWithTag("title-bar").getBoundsInRoot().top
        dragTheTitleBarDown(0.15f, millis = 1500)
        idle()
        seeDesc(s(Res.string.minimise))
        assertEquals(top, test.onNodeWithTag("title-bar").getBoundsInRoot().top)
    }

    /**
     * The bar is 50 pt with its title and without, as `.wtb`'s min-height: the title's 46 pt middle runs into the 6 pt foot, so the
     * title fading in on scroll never moves the list.
     */
    @Test
    fun theTitleBarIsFiftyPointsWithOrWithoutItsTitle() = flow {
        startWorkout() // empty, so the title shows
        val title = clickLabel(s(Res.string.workout_options)) and hasAnyAncestor(hasTestTag("title-bar"))
        wait("the title") { test.onAllNodes(title).fetchSemanticsNodes().isNotEmpty() }
        val shown = test.onNodeWithTag("title-bar").getBoundsInRoot().height
        addExercises("Bench Press BB") // at the list's top the title fades out
        gone(title)
        val bar = test.onNodeWithTag("title-bar").getBoundsInRoot()
        // With no middle the 44 pt buttons fill the bar down to its foot, so ⌄'s top is where the top inset ends.
        val inset = test.onNodeWithContentDescription(s(Res.string.minimise)).getBoundsInRoot().top - bar.top
        assertEquals(shown.value, bar.height.value, 0.01f)
        assertEquals(50f, (bar.height - inset).value, 0.01f)
    }

    /** Only the title bar minimises: the same drag on the list at its top scrolls, or bounces, and never minimises (Review Focus 4). */
    @Test
    fun aDownwardScrollOnTheListNeverMinimises() = pushA {
        val screen = test.onRoot().fetchSemanticsNode().size.height.toFloat()
        test.onNodeWithTag("focus").performTouchInput { swipeDown(startY = centerY, endY = centerY + screen * 0.4f, durationMillis = 600) }
        idle()
        seeDesc(s(Res.string.minimise))
    }

    /** The rest's time in the bar while it runs, and both gone at zero. The ring is a drawing with no text: the WO-17-rest shot shows it. */
    @Test
    fun theBarShowsTheRestAsARingAndTimeAndDropsThemAtZero() = flow {
        startWorkout()
        tapDesc(s(Res.string.minimise))
        bar()
        // Unmerged: the bar is one button, and a test tag inside it does not merge up into the button's node.
        fun restShows() = test.onAllNodes(hasTestTag("bar-rest"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        graph.rest.value = Rest(now() - 17_000, 120)
        // 1:43 as it starts; the clock ticks on, so only the minute and the tens.
        val time = hasText("1:4", substring = true) and hasAnyAncestor(hasTestTag("bar-rest"))
        wait("the rest's time in the bar") { test.onAllNodes(time, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        graph.rest.value = Rest(now() - 121_000, 120)
        wait("the rest to leave the bar") { !restShows() }
    }

    @Test
    fun aLongWorkoutNameKeepsTheBarOneLineTall() = flow {
        startWorkout()
        val short = barHeightAfterRenaming("Push A")
        assertEquals(short, barHeightAfterRenaming("Överkropp tung med extra armar och lite till för säkerhets skull"))
    }

    @Test
    fun aLongNameEndsInAnEllipsisUnderTheContinueBubble() = flow {
        startWorkout()
        val name = "Överkropp tung med extra armar och lite till för säkerhets skull"
        graph.sessions.rename(open().id, name)
        tapDesc(s(Res.string.minimise)); bar()
        tapDesc(s(Res.string.tab_start))
        val label = test.onNode(hasText(name.take(10), substring = true) and hasAnyAncestor(hasTestTag("start-bubbles"))).fetchSemanticsNode().boundsInRoot
        val root = test.onAllNodes(isRoot())[0].fetchSemanticsNode().boundsInRoot
        assertTrue(label.left >= root.left && label.right <= root.right, "$label inside $root")
        assertTrue(label.width <= 160 * 3 + 1, "the label is capped at 160 dp")
        assertTrue(label.height <= 13 * 3 + 1, "one line of label-small (13 dp), not wrapped")
    }

    /** Centre of the node with [desc], in px. */
    private fun Flow.centre(desc: String) = test.onNode(hasContentDescription(desc)).fetchSemanticsNode().boundsInRoot.center

    @Test
    fun twoBubblesSitInTheArcOverStart() = flow {
        tapDesc(s(Res.string.tab_start))
        val start = centre(s(Res.string.close_start))
        val empty = centre(s(Res.string.bubble_empty_desc))
        val run = centre(s(Res.string.start_run))
        // P2: (−47, −157) and (47, −157) dp from Start's centre, at density 3, within a pixel.
        assertEquals(-141f, empty.x - start.x, 1f); assertEquals(-471f, empty.y - start.y, 1f)
        assertEquals(141f, run.x - start.x, 1f); assertEquals(-471f, run.y - start.y, 1f)
    }

    @Test
    fun aTapOutsideOrOnTheCrossClosesStart() = flow {
        tapDesc(s(Res.string.tab_start)); seeDesc(s(Res.string.bubble_empty_desc))
        test.onNode(clickLabel(s(Res.string.close_start))).performClick()   // the scrim
        gone(hasContentDescription(s(Res.string.bubble_empty_desc)))
        tapDesc(s(Res.string.tab_start)); tapDesc(s(Res.string.close_start)) // the ×
        gone(hasContentDescription(s(Res.string.bubble_empty_desc)))
    }

    @Test
    fun anEmptyWorkoutOrARunStartsInTwoTapsFromAnyTab() = flow {
        for (tab in listOf(Res.string.tab_home, Res.string.tab_food, Res.string.tab_plan, Res.string.tab_you)) {
            tap(s(tab))
            tapDesc(s(Res.string.tab_start)); tapDesc(s(Res.string.start_run))                 // two taps
            see(s(Res.string.close)); tap(s(Res.string.close))
            tapDesc(s(Res.string.tab_start)); tapDesc(s(Res.string.bubble_empty_desc))         // two taps
            see(s(Res.string.what_today))
            tapDesc(s(Res.string.minimise)); bar()
            runBlocking { graph.sessions.discard(open().id) }
            gone(clickLabel(s(Res.string.back_to_workout)))
        }
    }

    @Test
    fun aDoubleTapOnEmptyStartsOneWorkout() = flow {
        tapDesc(s(Res.string.tab_start))
        test.onNode(hasContentDescription(s(Res.string.bubble_empty_desc))).performTouchInput { click(); click() }
        see(s(Res.string.what_today))
        runBlocking { graph.sessions.discard(open().id) }
        waitUntilTrue { graph.sessions.open().first() == null }   // a second workout would still be open
    }

    @Test
    fun underReduceMotionTheBubblesAppearInPlace() = flow(reduceMotion = true) { assertTrue(bubbleMovesAfterTheTap() == 0f) }

    @Test
    fun theBubblesSpringUpFromStart() = flow { assertTrue(bubbleMovesAfterTheTap() > 0f) }

    /** How far the Empty workout and Run bubbles move between 16 ms after Start's tap and settling: the larger, in px. */
    private suspend fun Flow.bubbleMovesAfterTheTap(): Float {
        test.mainClock.autoAdvance = false
        test.onNode(hasContentDescription(s(Res.string.tab_start))).performClick()
        test.mainClock.advanceTimeBy(16)
        val early = listOf(centre(s(Res.string.bubble_empty_desc)), centre(s(Res.string.start_run)))
        test.mainClock.autoAdvance = true
        idle()
        val settled = listOf(centre(s(Res.string.bubble_empty_desc)), centre(s(Res.string.start_run)))
        return early.zip(settled) { a, b -> (a - b).getDistance() }.max()
    }

    /**
     * MO-start's three key frames, 0, 120 and 470 ms after Start's tap, at the flow tests' size: compared by eye with the motion board
     * (P11), so drawn in English as the board is.
     */
    @Test
    fun moStartFramesInDarkAndDarkHc() = inEnglish {
        for ((highContrast, mode) in listOf(false to "dark", true to "dark-hc")) flow(dark = true, highContrast = highContrast) {
            test.mainClock.autoAdvance = false
            test.onNode(hasContentDescription(s(Res.string.tab_start))).performClick()
            test.savePng(BOARD_SHOTS, "MO-start-0-$mode")
            test.mainClock.advanceTimeBy(120)
            test.savePng(BOARD_SHOTS, "MO-start-120-$mode")
            test.mainClock.advanceTimeBy(350)
            test.savePng(BOARD_SHOTS, "MO-start-470-$mode")
            seeDesc(s(Res.string.bubble_empty_desc)); seeDesc(s(Res.string.start_run))
            test.mainClock.autoAdvance = true
        }
    }

    /**
     * MO-log's key frames, 0, 100, 450 and 500 ms after set 2's ✓ in Push A, and the pop's peak at 170, compared by eye with the motion board, so drawn in English
     * as the board is. At 100 ms the ✓ has pressed to .96 and has not yet popped back.
     */
    @Test
    fun moLogFramesInDarkHc() = inEnglish {
        pushA(dark = true, highContrast = true) {
            test.mainClock.autoAdvance = false
            test.onNode(setTwosCheckMatcher()).performClick()
            test.savePng(BOARD_SHOTS, "MO-log-0-dark-hc")
            test.mainClock.advanceTimeBy(100)
            val pressed = setTwosCheck()
            test.savePng(BOARD_SHOTS, "MO-log-100-dark-hc")
            // Not one of the board's frames: the pop's 1.14 peak and the full lime edge, which follow the 100 ms press.
            test.mainClock.advanceTimeBy(70)
            test.savePng(BOARD_SHOTS, "MO-log-170-dark-hc")
            test.mainClock.advanceTimeBy(280)
            test.savePng(BOARD_SHOTS, "MO-log-450-dark-hc")
            test.mainClock.advanceTimeBy(50)
            test.savePng(BOARD_SHOTS, "MO-log-500-dark-hc")
            test.mainClock.autoAdvance = true
            idle()
            val settled = setTwosCheck()
            assertTrue(pressed.width < settled.width, "the ✓ at 100 ms is ${pressed.width} px wide, settled ${settled.width}")
        }
    }

    /**
     * MO-log under Reduce Motion: no press and no pop, so the ✓ is its settled size at 100 ms. Its size, not its place: the focus card
     * above it loses set 2's "up 2.5 kg" line, so the row moves up whatever the motion does.
     */
    @Test
    fun underReduceMotionTheCheckDoesNotScale() = pushA(reduceMotion = true) {
        test.mainClock.autoAdvance = false
        test.onNode(setTwosCheckMatcher()).performClick()
        test.mainClock.advanceTimeBy(100)
        val early = setTwosCheck()
        test.mainClock.autoAdvance = true
        idle()
        waitUntilTrue { open().exercises.first().sets[2].done }
        assertEquals(setTwosCheck().size, early.size)
    }

    /**
     * MO-log on the log that completes Push A: the count still rolls, now in P11's lime with its check. Compared in pixels, since the
     * count reads as one text: 100 ms in, the roll is drawn mid-way, unlike the settled count.
     */
    @Test
    fun theCountRollsOnTheLogThatCompletesTheWorkout() = inEnglish {
        pushA(dark = true, highContrast = true) {
            val last = open().exercises.first().sets.last()
            for (set in open().exercises.flatMap { it.sets }) if (!set.done && set.id != last.id) graph.sessions.saveSet(set.copy(done = true))
            see(s(Res.string.sets_progress, 13, 14))
            test.mainClock.autoAdvance = false
            test.onNode(hasContentDescription(s(Res.string.log_set, "3")) and hasAnyAncestor(hasTestTag("set Bench Press BB 4"))).performClick()
            test.mainClock.advanceTimeBy(100)
            val count = hasText(s(Res.string.sets_progress, 14, 14))
            val rolling = test.onNode(count).captureToImage().toPixelMap()
            test.savePng(BOARD_SHOTS, "MO-log-complete-100-dark-hc")
            test.mainClock.autoAdvance = true
            idle()
            val settled = test.onNode(count).captureToImage().toPixelMap()
            assertTrue(rolling.width != settled.width || rolling.height != settled.height || !rolling.buffer.contentEquals(settled.buffer),
                "the count at 100 ms is drawn as it settles: it did not roll")
        }
    }

    /** MO-log's rise belongs to the log: the rest card coming back as the dock closes stands where it was from its first frame. */
    @Test
    fun theRestCardDoesNotRiseAgainWhenTheDockCloses() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapDesc(s(Res.string.expand, "Bench Press BB"))
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
        // The card counts in real time; stretched, it cannot run out on a slow machine.
        graph.rest.value = graph.rest.value!!.copy(seconds = 3_600)
        idle()
        val settled = test.onNodeWithTag("rest-card").fetchSemanticsNode().boundsInRoot
        tapDesc(s(Res.string.rest_change, "1"))
        gone(hasTestTag("rest-card"))
        test.mainClock.autoAdvance = false
        test.onNode(hasText(s(Res.string.save)) and hasClickAction()).performClick()
        test.mainClock.advanceTimeByFrame()
        val back = test.onNodeWithTag("rest-card").fetchSemanticsNode().boundsInRoot
        test.mainClock.autoAdvance = true
        assertEquals(settled, back)
    }

    /** Bench Press BB's set 2 ✓ in Push A (its third row, after the warm-up), before and after it is logged. */
    private suspend fun Flow.setTwosCheckMatcher() =
        (hasContentDescription(s(Res.string.log_set, "2")) or hasContentDescription(s(Res.string.logged))) and
            hasAnyAncestor(hasTestTag("set Bench Press BB 3"))

    private suspend fun Flow.setTwosCheck() = test.onNode(setTwosCheckMatcher()).fetchSemanticsNode().boundsInRoot

    @Test
    fun aDoubleTapOnContinueOpensTheWorkoutOnce() = flow {
        startWorkout()
        tapDesc(s(Res.string.minimise)); bar()
        tapDesc(s(Res.string.tab_start))
        test.onNode(descContains(s(Res.string.bubble_continue))).performTouchInput { click(); click() }
        see(s(Res.string.what_today))
        tapDesc(s(Res.string.minimise))
        gone(hasContentDescription(s(Res.string.minimise)))   // a second copy of the workout would still show
        bar()
    }

    @Test
    fun aDoubleTapOnRunOpensItOnce() = flow {
        tapDesc(s(Res.string.tab_start))
        test.onNode(hasContentDescription(s(Res.string.start_run))).performTouchInput { click(); click() }
        see(s(Res.string.close)); tap(s(Res.string.close))
        gone(hasText(s(Res.string.close)))   // a second run page would still show
        seeDesc(s(Res.string.tab_start))
    }

    @Test
    fun aTapOnABubblesLabelActsAsTheBubble() = flow {
        tapDesc(s(Res.string.tab_start))
        test.onNode(hasText(s(Res.string.start_run)) and hasAnyAncestor(hasTestTag("start-bubbles"))).performClick()
        see(s(Res.string.close))
    }

    @Test
    fun aTapOnThePanelBetweenTheBubblesLeavesStartOpen() = flow {
        tapDesc(s(Res.string.tab_start))
        val empty = centre(s(Res.string.bubble_empty_desc))
        val run = centre(s(Res.string.start_run))
        test.onAllNodes(isRoot())[0].performTouchInput { click(Offset((empty.x + run.x) / 2, empty.y)) }
        idle()
        seeDesc(s(Res.string.bubble_empty_desc)); seeDesc(s(Res.string.close_start))
    }

    @Test
    fun backClosesStartAndLeavesTheTabWhereItWas() = flow {
        tap(s(Res.string.next_screen))
        val second = "${s(Res.string.tab_home)} 2"
        see(second)
        tapDesc(s(Res.string.tab_start)); seeDesc(s(Res.string.bubble_empty_desc))
        back()
        gone(hasContentDescription(s(Res.string.bubble_empty_desc)))
        see(second)
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
    fun foodSaysNutritionComesLaterAndItsButtonGoesNowhere() = flow {
        tap(s(Res.string.tab_food))
        see(s(Res.string.food_later_title))
        tap(s(Res.string.food_log_later))
        see(s(Res.string.food_later_title))
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
        backReads(s(Res.string.all_activities))
        shot("09-session-detail")
        tapBack()
        backReads(s(Res.string.tab_you))
        tapBack()
        see(s(Res.string.all_activities))
    }

    /** An lb exercise is logged in lb, and History's volume converts it into the app default, kg (#58, acceptance 2). */
    @Test
    fun anLbExerciseLogsInLbAndTheTotalsConvert() = inEnglish {
        flow {
            startWorkout()
            curlInLb()
            addExercises("Bench Press BB", "Curl DB")
            logFirstSet("100", "5")
            // Bench Press done, the focus moves on and Curl DB opens.
            test.onNodeWithTag("set Curl DB 1").performClick()
            idle()
            see("Weight, lb · step 5")
            tapDesc(weightLabel("lb", "5"))
            keys("25")
            tap(s(Res.string.next))
            keys("12")
            tap(s(Res.string.next))
            tap(s(Res.string.log_next))
            waitUntilTrue { open().exercises.all { e -> e.sets.all { it.done } } }
            tap(s(Res.string.finish))
            tap(s(Res.string.done))
            tap(s(Res.string.tab_you))
            tap(s(Res.string.all_activities))
            // 100 × 5 kg plus 25 × 12 lb in kg: 636, not the 800 the raw numbers add up to.
            see("${grouped((500 + 300 * 0.45359237).roundToLong(), swedish = false)} kg", substring = true)
        }
    }

    /** Ruling 2: a bar-less exercise's weight label names its step, one with a bar does not, and neither crashes on lb (#58). */
    @Test
    fun lbDockWithoutABarShowsTheStep() = inEnglish {
        flow {
            startWorkout()
            curlInLb()
            addExercises("Bench Press BB", "Curl DB")
            openSet(0)
            see("Weight, kg")
            test.onNodeWithContentDescription(s(Res.string.close)).performSemanticsAction(SemanticsActions.OnClick)
            gone(hasText("Weight, kg"))
            tapDesc(s(Res.string.expand, "Curl DB"))
            test.onNodeWithTag("set Curl DB 1").performClick()
            idle()
            see("Weight, lb · step 5")
        }
    }

    @Test
    fun aBodyweightExerciseLogsRepsOnly() = flow {
        startWorkout()
        addExercises("Chest Dips")
        openSet(0)
        gone(descContains(weightLabel("kg")))
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
        seeDesc(weightLabel("kg"))
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
        tapSegment("2") // RIR picked on the card, which the strip does not draw but VoiceOver reads
        // Folded exercises are short; open three so the list is long enough to scroll the card away. Bottom up, so an opened
        // exercise never pushes the next one to open below the window, where the list does not draw it.
        for (name in listOf("Incline Press DB", "Pull Up", "Squat BB")) tapDesc(s(Res.string.expand, name))
        test.onNode(hasScrollToIndexAction()).performScrollToKey("end")
        wait("the strip") { test.onAllNodes(hasTestTag("focus-strip")).fetchSemanticsNodes().isNotEmpty() }
        shot("11-strip")
        wait("the strip's RIR for VoiceOver") {
            test.onAllNodes(hasTestTag("focus-strip") and hasContentDescription("RIR 2", substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
        // No Logga on the strip (point 7): the whole strip scrolls back up to the card.
        gone(hasText(s(Res.string.log_set, 1), substring = true) and hasClickAction() and !hasContentDescription(s(Res.string.log_set, 1)))
        test.onNode(clickLabel(s(Res.string.back_to_focus))).performClick()
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("focus")).fetchSemanticsNodes().isNotEmpty() }
        gone(hasTestTag("focus-strip"))
    }

    @Test
    fun theFocusCardShowsLastTimeTheChangeAndTheRest() = flow {
        designD()
        see(s(Res.string.up_by, "5", "kg"))
        // Set 2 of 2 is Bench Press's last, so the meta row says so where it said the rest (#42 P6).
        see(s(Res.string.last_set))
        see("80 × 8 @3", substring = true)
        // A logged set shows its RIR after the reps (point 2), and a rest line sits between the sets (point 8).
        see("8 @2")
        see(s(Res.string.rest_row, "2:00"))
        shot("14-d-pass")
    }

    /** The meta row: "last set" on an exercise's last set, "hold · last set" on it while a rest runs, otherwise the rest after it (#42 P6). */
    @Test
    fun theFocusCardReadsLastSetAndHoldLastSetWhileResting() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        see(s(Res.string.last_set))
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        see(s(Res.string.rest_after, "2:00"))
        // Logging set 1 starts its rest, with set 2, the last, in focus.
        logFirstSet("80", "8")
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
        see(s(Res.string.hold_last_set))
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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

    /** P8: the set type menu reads Warm-up, Working set, Drop set, To failure, each with the badge the set would take: W, 4, D, 4. */
    @Test
    fun theSetMenuNamesTheSetsOwnNumber() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        repeat(3) { graph.sessions.addSet(open().exercises.single().id) }
        waitUntilTrue { open().sets == 4 }
        tapDesc(s(Res.string.set_type, "4"))
        wait("the menu") { test.onAllNodes(hasTestTag("glass-menu")).fetchSemanticsNodes().size == 1 }
        val rows = test.onAllNodes(hasAnyAncestor(hasTestTag("glass-menu")) and hasClickAction()).fetchSemanticsNodes()
            .map { row -> row.config[SemanticsProperties.Text].joinToString(" ") { it.text } }
        val kinds = listOf(Res.string.kind_warmup to "W", Res.string.kind_normal to "4", Res.string.kind_drop to "D", Res.string.kind_failure to "4")
        assertEquals(kinds.map { (kind, tail) -> "${s(kind)} $tail" }, rows)
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
        shot("16b-rest-submenu")
        tap("1:30")
        waitUntilTrue { open().exercises.single().sets.all { it.rest == 90 } }
        see(s(Res.string.rest_row, "1:30"))
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.sticky_note))
        type("Bench at 30°")
        tap(s(Res.string.save))
        see("Bench at 30°")
    }

    /**
     * Acceptance 4: Exercise details, two taps from the workout (O-02), opens EX-05 as a sheet over it; Done closes the sheet and the
     * workout is where it was, with nothing pushed on any back stack.
     */
    @Test
    fun exerciseDetailsOpensAsASheetOverTheWorkout() = pushA {
        val backs = test.onAllNodes(hasTestTag("back")).fetchSemanticsNodes().size
        tapDesc(s(Res.string.exercise_options)) // Bench's, the one exercise standing open
        tap(s(Res.string.exercise_details))
        for (tab in listOf(Res.string.tab_about, Res.string.tab_history, Res.string.tab_chart, Res.string.tab_records)) see(s(tab))
        shot("ex05-sheet")
        // Over the workout, not in place of it: its title and clock stay under the sheet, and the sheet has no back label of its own.
        see("Push A")
        see(s(Res.string.finish))
        assertEquals(backs, test.onAllNodes(hasTestTag("back")).fetchSemanticsNodes().size)
        tap(s(Res.string.done))
        gone(hasText(s(Res.string.tab_records)))
        see(s(Res.string.finish))
        test.onNode(hasContentDescription(s(Res.string.minimise))).assertExists()
        assertEquals(backs, test.onAllNodes(hasTestTag("back")).fetchSemanticsNodes().size)
    }

    @Test
    fun theExerciseMenuSkipsAndSwaps() = flow {
        startWorkout()
        addExercises("Bench Press BB", "Squat BB")
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.order_skip))
        tapLastText(s(Res.string.skip)) // Squat's, the last row
        waitUntilTrue { open().exercises[1].skipped && open().sets == 1 }
        shot("20-order-sheet")
        tap(s(Res.string.done))
        see("Squat BB · ${s(Res.string.skipped)}") // WO-02-s14's overview row
        // Swap Bench Press, the one still open, for another exercise.
        tapDesc(s(Res.string.exercise_options))
        tap(s(Res.string.swap))
        search("Incline Press DB")
        tap("Incline Press DB")
        waitUntilTrue { open().exercises.first().name == "Incline Press DB" }
    }

    @Test
    fun theWorkoutMenuChangesTheStartTime() = startedAt1726 {
        // Untouched, the wheels open on the start, VoiceOver reads those rows as selected, and Save keeps it.
        tap(s(Res.string.workout))
        tap(s(Res.string.change_start))
        test.onNode(wheelRow(s(Res.string.hour), "17")).assertIsSelected()
        test.onNode(wheelRow(s(Res.string.minute), "26")).assertIsSelected()
        tap(s(Res.string.save))
        gone(hasText(s(Res.string.save)))
        assertEquals(onTheDay(17, 26), open().startedAt)
        changeStart("16", "45")
        waitUntilTrue { open().startedAt == onTheDay(16, 45) }
    }

    @Test
    fun theWheelSelectsTheFirstAndLastRow() = startedAt1726 {
        changeStart("00", "00")
        waitUntilTrue { open().startedAt == onTheDay(0, 0) }
        // 23:59 is later than now, so the start stops at now, as withTime clamps it.
        changeStart("23", "59")
        waitUntilTrue { open().startedAt == onTheDay(17, 26) }
    }

    /** A workout started with [wallClock] standing at 17:26 on Monday 9 November, in the simulator's zone. */
    private fun startedAt1726(block: suspend Flow.() -> Unit) = flow {
        val now = Instant.fromEpochMilliseconds(onTheDay(17, 26))
        wallClock = object : Clock {
            override fun now() = now
        }
        try {
            startWorkout()
            block()
        } finally {
            wallClock = Clock.System
        }
    }

    private fun onTheDay(hour: Int, minute: Int) =
        LocalDateTime(2026, 11, 9, hour, minute).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    /** Change start time from the workout's menu: the hour and minute rows tapped on their wheels, then Save. */
    private suspend fun Flow.changeStart(hour: String, minute: String) {
        tap(s(Res.string.workout))
        tap(s(Res.string.change_start))
        shot("17-start-time")
        for ((column, value) in listOf(s(Res.string.hour) to hour, s(Res.string.minute) to minute)) {
            val row = wheelRow(column, value)
            // A row off the wheel's five is scrolled into view first; a row in view is only tapped.
            if (test.onAllNodes(row).fetchSemanticsNodes().isEmpty())
                test.onNode(hasContentDescription(column) and hasScrollToIndexAction()).performScrollToIndex(value.toInt())
            test.onNode(row).performClick()
            test.waitForIdle()
            test.onNode(row).assertIsSelected()
        }
        tap(s(Res.string.save))
    }

    /** [value]'s row on the wheel labelled [column]: the hour and minute wheels share "00" to "23". */
    private fun wheelRow(column: String, value: String) = hasText(value) and hasClickAction() and hasAnyAncestor(hasContentDescription(column))

    @Test
    fun theDockStepsTheRestAndLoggingStartsTheRestCard() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        openSet(0)
        tapDesc("${weightLabel("kg")} +")
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        // Picked, Bench Press comes with last time's two sets (P14); D draws today's set 2 at 85, so it is written there.
        waitUntilTrue { open().exercises.first().sets.size == 2 }
        logFirstSet("85", "8", rir = "2")
        graph.sessions.saveSet(open().exercises.first().sets[1].copy(weight = 85.0))
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        assertEquals(Scheme.Dark.colors.tertiary.takeIf { palette == "dark" } ?: Scheme.DarkHc.colors.tertiary, typeWordColour(label, word))
        shot("warmup-focus-$palette")
    }

    /** The colour on the last [word] of the label reading [label], null if it has none: only the type word is coloured. */
    private fun Flow.typeWordColour(label: String, word: String): Color? {
        val text = test.onAllNodes(hasText(label))[0].fetchSemanticsNode().config[SemanticsProperties.Text].first()
        val start = text.text.lastIndexOf(word)
        assertTrue(text.spanStyles.none { it.start < start }, "only the type word is coloured")
        return text.spanStyles.firstOrNull { it.start == start && it.end == start + word.length }?.item?.color
    }

    private fun accents(palette: String) = if (palette == "dark") Scheme.Dark.accents else Scheme.DarkHc.accents

    @Test
    fun failureSetsAreNumberedWithTheWorkingSetsInDark() = flow(dark = true) { failureShots("dark") }

    @Test
    fun failureSetsAreNumberedWithTheWorkingSetsInDarkHighContrast() = flow(dark = true, highContrast = true) { failureShots("darkhc") }

    private suspend fun Flow.failureShots(palette: String) {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.skip))
        tapDesc(s(Res.string.expand, "Bench Press BB"))
        repeat(2) { tapContaining(s(Res.string.add_set)) }
        waitUntilTrue { open().sets == 3 }
        tapDesc(s(Res.string.set_type, "2"))
        tap(s(Res.string.kind_failure))
        waitUntilTrue { open().exercises.single().sets[1].kind == SetKind.Failure }
        // A failure set is a working set (Q31) and shows its number, 1, 2, 3, the 2 in coral; VoiceOver adds the type (Carl, 2026-11-03).
        seeDesc(s(Res.string.set_failure, "2"))
        seeDesc(s(Res.string.set_type, "3"))
        val word = s(Res.string.kind_failure).uppercase()
        val label = s(Res.string.now_set_kind, "Bench Press BB", "2", 3, word).uppercase()
        see(label)
        assertEquals(accents(palette).failure, typeWordColour(label, word))
        // The current set's badge is teal whatever its type; set 3 to failure too shows a coral number beside it.
        tapDesc(s(Res.string.set_type, "3"))
        tap(s(Res.string.kind_failure))
        waitUntilTrue { open().exercises.single().sets[2].kind == SetKind.Failure }
        seeDesc(s(Res.string.set_failure, "3"))
        shot("numbered-failure-$palette")
    }

    @Test
    fun aDropSetKeepsItsDAndTakesItsOwnColourInDark() = flow(dark = true) { dropShots("dark") }

    @Test
    fun aDropSetKeepsItsDAndTakesItsOwnColourInDarkHighContrast() = flow(dark = true, highContrast = true) { dropShots("darkhc") }

    /** Set 1 logged, set 2 a drop set next: D in violet, unnumbered, so set 3 is the second working set; "DROPSET" in the same violet. */
    private suspend fun Flow.dropShots(palette: String) {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.skip))
        tapDesc(s(Res.string.expand, "Bench Press BB"))
        repeat(2) { tapContaining(s(Res.string.add_set)) }
        waitUntilTrue { open().sets == 3 }
        tapDesc(s(Res.string.set_type, "2"))
        tap(s(Res.string.kind_drop))
        waitUntilTrue { open().exercises.single().sets[1].kind == SetKind.Drop }
        seeDesc(s(Res.string.set_type, "D"))
        seeDesc(s(Res.string.set_type, "2"))
        val word = s(Res.string.kind_drop).uppercase()
        val label = s(Res.string.now_kind, "Bench Press BB", word).uppercase()
        see(label)
        assertEquals(accents(palette).drop, typeWordColour(label, word))
        // A second drop after it, so a violet D shows beside the current set's teal badge.
        tapDesc(s(Res.string.set_type, "2"))
        tap(s(Res.string.kind_drop))
        waitUntilTrue { open().exercises.single().sets[2].kind == SetKind.Drop }
        shot("drop-set-$palette")
    }

    /** P7: the dock opened from the rest line after set 1 is titled for that rest, not for the set (WO-04-rest). */
    @Test
    fun theDockFromARestLineIsTitledRestAfterTheSet() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tapDesc(s(Res.string.expand, "Bench Press BB"))
        tapContaining(s(Res.string.add_set))
        waitUntilTrue { open().sets == 2 }
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasTestTag("rest-card")).fetchSemanticsNodes().isNotEmpty() }
        graph.rest.value = graph.rest.value!!.copy(seconds = 3_600)
        tapDesc(s(Res.string.rest_change, "1"))
        see(s(Res.string.dock_rest_after, "Bench Press BB", 1))
        gone(hasText(s(Res.string.dock_set, "Bench Press BB", 1)))
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
        tapDesc(s(Res.string.expand, "Bench Press BB"))
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
        logSet("set Lateral Raise DB 1", "10", "12", weightLabel("kg", "2"))
        waitUntilTrue { open().exercises[1].sets[0].done }
        assertNull(graph.rest.value)
        gone(hasTestTag("rest-card"))
        see(s(Res.string.now_set, "A · Incline Press DB", 1, 2).uppercase())
        // Then A 1: the round is done, its rest starts, and the focus moves on to A 2.
        logSet("set Incline Press DB 1", "26", "10", weightLabel("kg", "2"))
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
        shot("21-superset-alert")
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

    /**
     * Acceptance criterion 2: two exercises with last time's sets, three working sets each, at most three taps a set. Inside a set's block
     * every click goes through the local `countedTap` and no other clicking helper; unfolding Incline Press DB is a tap of its set 1.
     * Adding the exercises and scrolling an exercise into view come between the blocks. Each set is logged by its own row's check, so
     * Bench Press BB's warm-up stays unlogged and uncounted (O-25).
     */
    @Test
    fun twoExercisesThreeSetsEachTakeAtMostThreeTapsASet() = flow {
        try {
            SampleData.load(graph)
            graph.sessions.discard(open().id)
            startWorkout()
            // Quick picks show only while the workout is empty, so the second exercise comes through the picker.
            test.onNode(clickLabel(s(Res.string.add_named, "Bench Press BB"))).performClick()
            waitUntilTrue { open().exercises.size == 1 }
            addExercises("Incline Press DB")
            // Last time's sets come with them (P14): Bench Press BB's warm-up and three, Incline Press DB's three.
            assertEquals(listOf(4, 3), open().exercises.map { it.sets.size })

            var taps = 0
            fun countedTap(matcher: SemanticsMatcher) {
                test.onNode(matcher).performClick()
                taps++
                idle()
            }
            var logged = 0
            suspend fun inView(name: String) {
                test.onNode(hasScrollToIndexAction()).performScrollToKey("h${open().exercises.first { it.name == name }.id}")
                idle()
            }
            // The header's count, or the bar's while the header is scrolled away.
            suspend fun countReads(n: Int) {
                val header = s(Res.string.sets_progress, n, 6)
                val bar = s(Res.string.time_sets, "", n, 6)
                wait("the count to read $n of 6") {
                    test.onAllNodes(hasText(header)).fetchSemanticsNodes().isNotEmpty() ||
                        test.onAllNodes(hasText(bar, substring = true)).fetchSemanticsNodes().isNotEmpty()
                }
            }
            /** Logs one working set by its row's check; a [folded] exercise is unfolded first, and that tap counts too. */
            suspend fun workingSet(name: String, row: Int, number: Int, folded: Boolean = false) {
                val id = open().exercises.first { it.name == name }.id
                val check = hasContentDescription(s(Res.string.log_set, "$number")) and hasAnyAncestor(hasTestTag("set $name $row"))
                if (folded) {
                    // Its overview row 100 pt down the list, clear of the strip on top and the rest card below.
                    test.onNode(hasScrollToIndexAction()).performScrollToKey("o$id")
                    test.onNode(hasScrollToIndexAction()).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -100f * 3) }
                    idle()
                } else {
                    inView(name)
                    wait("$name's set $number") { test.onAllNodes(check).fetchSemanticsNodes().isNotEmpty() }
                }
                // The block: every tap on screen from here until the set is logged. Scrolling is not a tap.
                taps = 0
                if (folded) {
                    countedTap(hasContentDescription(s(Res.string.expand, name)))
                    inView(name)
                    wait("$name's set $number") { test.onAllNodes(check).fetchSemanticsNodes().isNotEmpty() }
                }
                countedTap(check)
                assertTrue(taps <= 3, "$name's set $number took $taps taps")
                countReads(++logged)
            }

            for (n in 1..3) workingSet("Bench Press BB", row = n + 1, number = n)
            // Bench Press BB's warm-up is still the next set, so Incline Press DB waits folded: its set 1 takes the unfold and the check.
            for (n in 1..3) workingSet("Incline Press DB", row = n, number = n, folded = n == 1)

            test.onNode(hasScrollToIndexAction()).performScrollToKey("header")
            idle()
            see(s(Res.string.sets_progress, 6, 6))
            assertEquals(false, open().exercises.first().sets.first().done)
        } finally {
            wallClock = Clock.System
        }
    }

    @Test
    fun theLibraryStillOpensFromPlan() = flow {
        tap(s(Res.string.tab_plan))
        tab(s(Res.string.tab_plan)).assertIsSelected()
        tab(s(Res.string.tab_home)).assertIsNotSelected()
        tap(s(Res.string.library))
        see("Bench Press BB")
    }

    @Test
    fun backNamesTheParentInTheLibrary() = flow {
        tap(s(Res.string.tab_plan)); tap(s(Res.string.library))
        backReads(s(Res.string.tab_plan))
        test.onNode(hasTestTag("back")).assert(clickLabel(s(Res.string.back))) // VoiceOver: "Plan", and a tap goes back
        shot("22-library")
        search("Bench Press BB"); tap("Bench Press BB")
        backReads(s(Res.string.library))
        shot("23-exercise-detail")
        tapBack()
        backReads(s(Res.string.tab_plan))
        tapBack()
        gone(hasTestTag("back"))            // Plan's first screen has no parent
    }

    @Test
    fun aNameWiderThanTheBarKeepsDoneAndBackOnScreen() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        val name = "Överkropp tung med extra armar och lite till för säkerhets skull"
        graph.sessions.rename(open().id, name)
        tap(s(Res.string.finish))
        see(s(Res.string.done))
        shot("25-long-name-summary")
        val touch = with(test.density) { 44.dp.toPx() }
        // Done is a Material button: never squeezed below its own size, which Material pads out to a 48 dp touch.
        val (minWidth, minHeight) = with(test.density) { ButtonDefaults.MinWidth.toPx() to ButtonDefaults.MinHeight.toPx() }
        val done = test.onNode(hasText(s(Res.string.done)) and hasClickAction()).fetchSemanticsNode().boundsInRoot
        assertTrue(done.width >= minWidth && done.height >= minHeight, "Done at its full size: $done")
        tap(s(Res.string.done))
        seeDesc(s(Res.string.tab_start))
        tap(s(Res.string.tab_you))
        tap(s(Res.string.all_activities))
        tap(name)
        backReads(s(Res.string.all_activities))
        shot("26-long-name-detail")
        val back = test.onNode(hasTestTag("back")).fetchSemanticsNode().boundsInRoot
        assertTrue(back.width >= touch && back.height >= touch, "back a full touch in size: $back")
        tapBack()
        backReads(s(Res.string.tab_you))
    }

    @Test
    fun aNameThatFitsBesideTheBackLabelIsDrawnWhole() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        tap(s(Res.string.finish))
        tap(s(Res.string.done))
        tap(s(Res.string.tab_you))
        tap(s(Res.string.all_activities))
        tap(s(Res.string.workout))
        backReads(s(Res.string.all_activities))
        val id = graph.sessions.history().first().single().id
        // Each name has room between the back label and the bar's end, so none is cut to the room a centred title would have.
        val drawn = listOf("Workout", "Överkropp A", "Överkropp tung A").associateWith { name ->
            graph.sessions.rename(id, name)
            see(name)
            shot("27-title-${name.length}")
            val title = test.onNode(hasText(name), useUnmergedTree = true).fetchSemanticsNode()
            val back = test.onNode(hasTestTag("back")).fetchSemanticsNode().boundsInRoot
            assertTrue(title.boundsInRoot.left >= back.right, "'$name' (${title.boundsInRoot}) clear of the back label ($back)")
            drawnWidthAndRoom(title)
        }
        assertTrue(drawn.values.all { (width, room) -> width < room }, "every name drawn whole: $drawn")
    }

    @Test
    fun aLongWorkoutNameLeavesTheBackLabelWhole() = flow {
        startWorkout()
        addExercises("Bench Press BB")
        logFirstSet("80", "8")
        val name = "Push A, heavy upper body"
        graph.sessions.rename(open().id, name)
        tap(s(Res.string.finish))
        tap(s(Res.string.done))
        tap(s(Res.string.tab_you))
        tap(s(Res.string.all_activities))
        tap(name)
        val parent = s(Res.string.all_activities)
        backReads(parent)
        shot("24-long-name-detail")
        val text = test.onNode(hasText(parent) and hasAnyAncestor(hasTestTag("back")), useUnmergedTree = true).fetchSemanticsNode()
        val layouts = mutableListOf<TextLayoutResult>()
        text.config[SemanticsActions.GetTextLayoutResult].action!!(layouts)
        val layout = layouts.single()
        // The bar gives the label all it needs and the name what is left; a bar too narrow for even the label ends it in an ellipsis.
        val (width, room) = drawnWidthAndRoom(text)
        assertTrue(width < room, "'$parent' whole: drawn $width px of $room px")
        assertEquals(TextOverflow.Ellipsis, layout.layoutInput.overflow)
        assertEquals(1, layout.layoutInput.maxLines)
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
                see(s(Res.string.tap_to_continue))
                see(s(Res.string.bar_left_open, s(Res.string.workout), ""), substring = true) // the title up to its time
                test.onNode(clickLabel(s(Res.string.back_to_workout))).performClick()
                see(s(Res.string.restored))
                seeDesc(s(Res.string.logged, 1))
                // Opened once, it reads as any open workout again.
                tapDesc(s(Res.string.minimise))
                bar()
                see("1/1", substring = true)
                gone(hasText(s(Res.string.tap_to_continue)))
            }
        } finally {
            again.close()
            deleteFileDatabase(file)
        }
    }
}

private const val TIMEOUT = 10_000L
private const val IDLE_TIMEOUT = 60_000L

/**
 * A one-line text's drawn width and the width it was allowed. A text cut short fills its allowance exactly; one drawn whole is
 * narrower. Skia reports neither the ellipsis (`isLineEllipsized`) nor the overflow, and its intrinsic width runs about a tenth
 * under what it draws, so neither tells a cut text from a whole one.
 */
private fun drawnWidthAndRoom(text: SemanticsNode): Pair<Int, Int> {
    val layouts = mutableListOf<TextLayoutResult>()
    text.config[SemanticsActions.GetTextLayoutResult].action!!(layouts)
    return layouts.single().let { it.size.width to it.layoutInput.constraints.maxWidth }
}

internal fun clickLabel(label: String) =
    SemanticsMatcher("click label $label") { it.config.getOrNull(SemanticsActions.OnClick)?.label == label }

/** A node whose content description contains [text]: for a description that carries a running time. */
private fun descContains(text: String) = SemanticsMatcher("description contains $text") {
    it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { d -> text in d } == true
}

/** The steps a person takes and what they check, in the words of the app. */
@OptIn(ExperimentalTestApi::class)
private class Flow(val test: SkikoComposeUiTest, val graph: AppGraph, private val backInput: DirectNavigationEventInput) {
    /** The system's back: a completed back gesture. */
    fun back() {
        test.runOnIdle { backInput.backCompleted() }
        idle()
    }

    /**
     * Waits for the screen to settle, a frame at a time as `waitForIdle` does. That wait has no limit of its own, so a screen that
     * never settled spun for an hour (#57); this one fails after [IDLE_TIMEOUT] and says how busy the screen was, which state it
     * still writes and what is on it.
     */
    fun idle() {
        val from = test.mainClock.currentTime
        val applied = changesApplied()
        var frames = -1
        try {
            test.waitUntil("the screen to settle", IDLE_TIMEOUT) { frames++; !test.hasPendingWork() }
        } catch (e: ComposeTimeoutException) {
            println("NEVER IDLE: $frames frames and ${test.mainClock.currentTime - from} ms on the test's clock in ${IDLE_TIMEOUT / 1_000} s, " +
                "${changesApplied() - applied} of them recomposed; one more frame writes ${writtenInAFrame()}; on screen:")
            dump()
            throw e
        }
        // No waitForIdle after it: it has no bound either. What it added comes later: the next node lookup or the test's end rethrows
        // what the screen threw, and shot() waits for its own last frame.
    }

    /** How many times recomposition has changed the screen; an animation alone draws frames without one. */
    private fun changesApplied() = Recomposer.runningRecomposers.value.sumOf { it.changeCount }

    /** The state written while one more frame runs: what an animation that never ends keeps changing. */
    private fun writtenInAFrame(): List<String> {
        val written = mutableListOf<String>()
        val observer = Snapshot.registerApplyObserver { changed, _ -> changed.mapTo(written) { it.toString().take(120) } }
        test.mainClock.advanceTimeByFrame()
        observer.dispose()
        return written
    }

    /** Prints what is on screen and saves a picture of it, without waiting for a screen that may never settle. */
    private fun dump() {
        test.runWithoutImplicitWait { println(test.onAllNodes(isRoot()).printToString(maxDepth = 40)) }
        if (!test.hasPendingWork()) runCatching { shot("failure") }
    }

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
            dump()
            throw e
        }
    }

    fun seeDesc(desc: String) = test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasContentDescription(desc)).fetchSemanticsNodes().isNotEmpty() }

    /** Waits for the bar a minimised workout sits in. */
    suspend fun bar() {
        val label = s(Res.string.back_to_workout)
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(clickLabel(label)).fetchSemanticsNodes().isNotEmpty() }
    }

    /** The bar a minimised workout sits in, and that it sits above the tabs (criterion 8's QA). */
    suspend fun barAboveTheTabs() {
        bar()
        val bar = test.onNode(clickLabel(s(Res.string.back_to_workout))).getBoundsInRoot()
        val home = test.onAllNodes(hasText(s(Res.string.tab_home)) and hasClickAction()).onLast().getBoundsInRoot()
        assertTrue(bar.bottom <= home.top, "the bar's bottom ${bar.bottom} is above the tabs' top ${home.top}")
    }

    /** Drags the workout's title bar down by [share] of the screen's height over [millis]. */
    fun dragTheTitleBarDown(share: Float, millis: Long) {
        val screen = test.onRoot().fetchSemanticsNode().size.height.toFloat()
        test.onNodeWithTag("title-bar").performTouchInput { swipeDown(startY = centerY, endY = centerY + screen * share, durationMillis = millis) }
    }

    /** Renames the open workout, minimises it if it shows, and returns the bar's height once the bar shows the new name. */
    suspend fun barHeightAfterRenaming(name: String): Int {
        graph.sessions.rename(open().id, name)
        val minimise = s(Res.string.minimise)
        if (test.onAllNodes(hasContentDescription(minimise)).fetchSemanticsNodes().isNotEmpty()) tapDesc(minimise)
        bar()
        see(name.take(10), substring = true)
        return test.onNode(clickLabel(s(Res.string.back_to_workout))).fetchSemanticsNode().size.height
    }

    fun gone(matcher: SemanticsMatcher) = wait("${matcher.description} to go") { test.onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }

    /** Taps the last clickable node that matches: what is drawn last lies on top. */
    private fun tapLast(m: SemanticsMatcher) {
        wait("tap ${m.description}") { test.onAllNodes(m).fetchSemanticsNodes().isNotEmpty() }
        test.onAllNodes(m).let { it[it.fetchSemanticsNodes().size - 1] }.performClick()
        idle()
    }

    // Not a text field that happens to hold the same words, such as the search field after a search.
    fun tap(text: String) = tapLast(hasText(text) and hasClickAction() and !hasSetTextAction())

    fun tapContaining(text: String) = tapLast(hasText(text, substring = true) and hasClickAction() and !hasSetTextAction())

    fun tapDesc(desc: String) = tapLast(hasContentDescription(desc))

    /** A tap where [text] is drawn, on something that is not a button: the last such text, as what is drawn last lies on top. */
    fun tapOnText(text: String) {
        test.onAllNodes(hasText(text) and !hasClickAction()).onLast().performTouchInput { click() }
        idle()
    }

    fun tapDescContaining(text: String) = tapLast(descContains(text))

    /** The last clickable node with exactly this text, such as a button in a dialog drawn over a row with the same word. */
    fun tapLastText(text: String) = tapLast(hasText(text) and hasClickAction())

    /** The back label on screen reads [parent]. */
    fun backReads(parent: String) = wait("back to read '$parent'") {
        test.onAllNodes(hasTestTag("back") and hasText(parent)).fetchSemanticsNodes().isNotEmpty()
    }

    fun tapBack() = tapLast(hasTestTag("back"))

    /** The tab bar's tab labelled [label]. */
    fun tab(label: String) = test.onNode(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))

    fun tapSegment(label: String) = tapLast(hasText(label) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))

    fun keys(typed: String) = typed.forEach {
        test.onNodeWithTag("key-$it").performClick()
        idle()
    }

    fun type(text: String) {
        test.waitUntil(timeoutMillis = TIMEOUT) { test.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        test.onAllNodes(hasSetTextAction()).let { it[it.fetchSemanticsNodes().size - 1] }.performTextReplacement(text)
        idle()
    }

    /** The set rows on screen, in order; each carries the tag "set <exercise> <number>". */
    fun setRows() = test.onAllNodes(SemanticsMatcher("set row") {
        val tag = it.config.getOrNull(SemanticsProperties.TestTag) ?: ""
        tag.startsWith("set ") && " cell " !in tag
    })

    fun openSet(index: Int) {
        test.waitUntil(timeoutMillis = TIMEOUT) { setRows().fetchSemanticsNodes().size > index }
        setRows()[index].performClick()
        idle()
    }

    fun search(name: String) {
        type(name)
        see(name)
    }

    suspend fun startWorkout() {
        tapDesc(s(Res.string.tab_start))
        tapDesc(s(Res.string.bubble_empty_desc))
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

    /** Curl DB in lb with a 5 lb step and no bar, as WO-04-lb draws it. */
    suspend fun curlInLb() {
        val curl = graph.exercises.all().first { it.isNotEmpty() }.first { it.name == "Curl DB" }
        graph.exercises.setSettings(curl.id, step = 5.0, bar = null, unit = WeightUnit.Lb)
    }

    /** Taps the [index]th open exercise's Lägg till set. */
    suspend fun addSetTo(index: Int) {
        val add = hasText(s(Res.string.add_set), substring = true) and hasClickAction()
        wait("add set $index") { test.onAllNodes(add).fetchSemanticsNodes().size > index }
        test.onAllNodes(add)[index].performClick()
        idle()
    }

    /** Opens the set row tagged [tag] and logs it through the dock, whose weight field reads [label]. */
    suspend fun logSet(tag: String, weight: String, reps: String, label: String) {
        test.onNodeWithTag(tag).performClick()
        idle()
        tapDesc(label)
        keys(weight)
        tap(s(Res.string.next))
        keys(reps)
        tap(s(Res.string.next))
        tap(s(Res.string.log_next))
    }

    suspend fun logFirstSet(weight: String, reps: String, rir: String? = null) {
        openSet(0)
        rir?.let { tapSegment(it) }
        tapDesc(weightLabel("kg"))
        keys(weight)
        tap(s(Res.string.next))
        keys(reps)
        tap(s(Res.string.next)) // to the rest
        val before = open().logged
        tap(s(Res.string.log_next))
        waitUntilTrue { open().logged > before }
    }

    /** Waits for the one open menu, saves it as `menu-[name]-dark` beside the Menus card, and closes it. */
    fun openMenu(name: String) {
        wait("the $name menu") { test.onAllNodes(hasTestTag("glass-menu")).fetchSemanticsNodes().size == 1 }
        test.savePng(BOARD_SHOTS, "menu-$name-dark")
        closeMenu()
    }

    /** A tap on the screen outside the menu, at its top-left corner; the screen is the first root, as a popup adds a second. */
    fun closeMenu() {
        test.onAllNodes(isRoot())[0].performTouchInput { click(Offset(1f, 1f)) }
        gone(hasTestTag("glass-menu"))
    }

    /** A picture of the screen for a person to look at; the tests never compare them. */
    fun shot(name: String) = test.savePng(SHOTS, name)
}

// shortcut: screenshots go to a fixed folder outside the repo, for a person to read; pass a path from Gradle if CI should keep them.
private const val SHOTS = "/tmp/strive-flow-shots"