package se.carlsmeister.strive.boards

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.Finish
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.data.SeedExercise
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.SqlExercises
import se.carlsmeister.strive.workout.now
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.exercise_options
import strive.shared.generated.resources.field_weight
import strive.shared.generated.resources.next
import strive.shared.generated.resources.note
import strive.shared.generated.resources.remove_exercise
import strive.shared.generated.resources.rest_change
import strive.shared.generated.resources.swap
import strive.shared.generated.resources.workout
import kotlin.test.Test

/** The workout's boards (#42), shot from Carl's sample data into [BOARD_SHOTS] for a person to set beside the boards. */
@OptIn(ExperimentalTestApi::class)
class WorkoutShotTest {
    /** The reference board: Push A at 24:13, Bench Press BB's warm-up and set 1 logged, set 2 in focus. */
    @Test
    fun wo02S2InDarkAndDarkHc() = workoutBoard("WO-02-s2", waitFor = "1 of 14 sets")

    /** WO-02-s2 with RIR 2 picked on the focus card. */
    @Test
    fun wo02RirInDarkAndDarkHc() = workoutBoard("WO-02-rir", waitFor = "1 of 14 sets") {
        val two = hasText("2") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab) and hasAnyAncestor(hasTestTag("focus"))
        onNode(two).performClick()
        waitUntil(timeoutMillis = 10_000) { onAllNodes(two and isSelected()).fetchSemanticsNodes().isNotEmpty() }
    }

    /** WO-02-s2 scrolled under the strip, with Bench Press BB's note for today opened from its ⋯. The keyboard is the system's. */
    @Test
    fun wo02NoteInDarkAndDarkHc() = workoutBoard("WO-02-note", waitFor = "Note for today") { graph ->
        onAllNodes(hasContentDescription(getString(Res.string.exercise_options)))[0].performClick()
        waitForIdle()
        onNode(hasText(getString(Res.string.note))).performClick()
        waitForIdle()
        pastTheFocusCard(graph)
    }

    /** WO-02-s2 resting: the rest after Bench Press BB's set 1 at 1:43 of 2:00. */
    @Test
    fun wo02S3InDarkAndDarkHc() = workoutBoard("WO-02-s3", waitFor = "1:43") { graph ->
        val bench = today(graph).exercises.first { it.name == "Bench Press BB" }
        graph.rest.value = Rest(now() - 17_000, 120, bench.sets[1].id)
    }

    /** Bench Press BB all logged at 31:05, its last rest at 1:52, the A/B superset next. */
    @Test
    fun wo02S4InDarkAndDarkHc() = workoutBoard("WO-02-s4", waitFor = "3 of 14 sets") { graph ->
        val bench = benchLogged(graph, at = 31 * 60 + 5)
        graph.rest.value = Rest(now() - 8_000, 120, bench.sets.last().id)
    }

    /** At 38:40, A1 in focus in the A/B superset, no rest running. */
    @Test
    fun wo02S5InDarkAndDarkHc() = workoutBoard("WO-02-s5", waitFor = "rest 1:30 after the round") { graph ->
        benchLogged(graph, at = 38 * 60 + 40)
    }

    /** Half a round: B1 logged (10 x 13 @2), A1 still next, and no rest until A1 is logged. */
    @Test
    fun wo02S5bInDarkAndDarkHc() = workoutBoard("WO-02-s5b", waitFor = "4 of 14 sets") { graph ->
        benchLogged(graph, at = 39 * 60 + 22)
        logFirst(graph, "Lateral Raise DB")
    }

    /** Round 1 and its rest over: A1 and B1 logged, A2 in focus. */
    @Test
    fun wo02S5cInDarkAndDarkHc() = workoutBoard("WO-02-s5c", waitFor = "5 of 14 sets") { graph ->
        benchLogged(graph, at = 41 * 60 + 38)
        logFirst(graph, "Incline Press DB")
        logFirst(graph, "Lateral Raise DB")
    }

    /** O-21's giant set: Tricep Pushdown joined to the A/B superset as C at 40:15, A1 and B1 logged, C1 in focus. */
    @Test
    fun wo02AbcInDarkAndDarkHc() = workoutBoard("WO-02-abc", waitFor = "5 of 14 sets") { graph ->
        benchLogged(graph, at = 40 * 60 + 15)
        val w = today(graph)
        graph.sessions.superset(w.exercises.first { it.name == "Lateral Raise DB" }.id, w.exercises.first { it.name == "Tricep Pushdown" }.id)
        logFirst(graph, "Incline Press DB")
        logFirst(graph, "Lateral Raise DB")
        val label = hasText("Tricep Pushdown", substring = true, ignoreCase = true) and hasAnyAncestor(hasTestTag("focus"))
        waitUntil(timeoutMillis = 10_000) { onAllNodes(label).fetchSemanticsNodes().isNotEmpty() }
    }

    /** At 52:40, Dips' set 1 logged (+10 x 10 @2), set 2 to failure in focus. */
    @Test
    fun wo02S9fInDarkAndDarkHc() = workoutBoard("WO-02-s9f", waitFor = "TO FAILURE") { graph ->
        logAll(graph, at = 52 * 60 + 40, "Bench Press BB", "Incline Press DB", "Lateral Raise DB")
        val dips = today(graph).exercises.first { it.name == "Tricep Dips" }
        graph.sessions.saveSet(dips.sets[0].copy(reps = 10, rir = 2, done = true))
    }

    /** At 58:20, everything logged but Tricep Pushdown's drop set, which is in focus. */
    @Test
    fun wo02S9dInDarkAndDarkHc() = workoutBoard("WO-02-s9d", waitFor = "DROP SET") { graph ->
        logAll(graph, at = 58 * 60 + 20, "Bench Press BB", "Incline Press DB", "Lateral Raise DB", "Tricep Dips")
        val pushdown = today(graph).exercises.first { it.name == "Tricep Pushdown" }
        graph.sessions.saveSet(pushdown.sets[0].copy(reps = 12, rir = 2, done = true))
        graph.sessions.saveSet(pushdown.sets[1].copy(reps = 11, rir = 1, done = true))
    }

    /** At 48:55, Bench Press BB and the A/B superset logged, Tricep Dips' set 1 in focus: a bodyweight exercise's added weight. */
    @Test
    fun wo02S10bwInDarkAndDarkHc() = workoutBoard("WO-02-s10bw", waitFor = "+kg") { graph ->
        logAll(graph, at = 48 * 60 + 55, "Bench Press BB", "Incline Press DB", "Lateral Raise DB")
    }

    /** At 1:03:40, all 14 sets logged, then Plank (time) and Walking Lunge (distance) added from last time, Plank in focus. */
    @Test
    fun wo02S10tdInDarkAndDarkHc() = workoutBoard("WO-02-s10td", waitFor = "14 of 18 sets") { graph ->
        val names = listOf("Plank", "Walking Lunge")
        // Last time's sets, which an added exercise takes (P14), come from a finished workout of their own four days back.
        val day = 86_400_000L
        val core = graph.sessions.start("Core", now() - 4 * day)
        graph.sessions.add(core, names.map { exerciseId(graph, it) })
        for (e in graph.sessions.workout(core).first()!!.exercises) {
            graph.sessions.addSet(e.id)
            graph.sessions.workout(core).first()!!.exercises.first { it.id == e.id }.sets.forEach {
                graph.sessions.saveSet(if (e.name == "Plank") it.copy(seconds = 45, done = true) else it.copy(metres = 20.0, done = true))
            }
        }
        graph.sessions.finish(core, Finish.WithoutThem, now() - 4 * day + 30 * 60_000L)
        logAll(graph, at = 63 * 60 + 40, "Bench Press BB", "Incline Press DB", "Lateral Raise DB", "Tricep Dips", "Tricep Pushdown")
        graph.sessions.add(today(graph).id, names.map { exerciseId(graph, it) }, lastTime = true)
    }

    /** At 1:02:10, every set logged: the count and every exercise's count turn lime, and Finish is filled. */
    @Test
    fun wo02S13InDarkAndDarkHc() = workoutBoard("WO-02-s13", waitFor = "14 of 14 sets") { graph ->
        logAll(graph, at = 62 * 60 + 10, "Bench Press BB", "Incline Press DB", "Lateral Raise DB", "Tricep Dips", "Tricep Pushdown")
    }

    /**
     * WO-02-s2 with Tricep Dips skipped: "1 of 12 sets". The overview's "Tricep Dips · Skipped" lies below the board's window, where
     * the list composes nothing, so the shot waits for the count.
     */
    @Test
    fun wo02S14InDarkAndDarkHc() = workoutBoard("WO-02-s14", waitFor = "1 of 12 sets") { graph ->
        graph.sessions.skip(today(graph).exercises.first { it.name == "Tricep Dips" }.id, true)
    }

    /** At 1:03:30, all 14 logged and Copenhagen Plank added: Carl's own exercise, time per side, never done, three empty sets. */
    @Test
    fun wo02S15InDarkAndDarkHc() = workoutBoard("WO-02-s15", waitFor = "Type a time to log") { graph ->
        (graph.exercises as SqlExercises).seed(listOf(
            SeedExercise("Copenhagen Plank", primaryMuscle = "core", secondaryMuscles = emptyList(), pattern = "core", equipment = "none",
                type = "time", perSide = true, category = "strength", stepsEn = "", tipsEn = "", stepsSv = "", tipsSv = ""),
        ))
        logAll(graph, at = 63 * 60 + 30, "Bench Press BB", "Incline Press DB", "Lateral Raise DB", "Tricep Dips", "Tricep Pushdown")
        graph.sessions.add(today(graph).id, listOf(exerciseId(graph, "Copenhagen Plank")), lastTime = true)
        val copenhagen = today(graph).exercises.first { it.name == "Copenhagen Plank" }
        repeat(2) { graph.sessions.addSet(copenhagen.id) }
    }

    /** WO-02-s2 as it opens from the left-open bar after the app was killed: the restore toast under the bar, not dismissed. */
    @Test
    fun wo02S16InDarkAndDarkHc() = workoutBoard("WO-02-s16", waitFor = "Restored.", dismissToast = false)

    /** WO-02-s2 at 24:20 with Bench Press BB removed through its menu: Incline Press DB in focus, the toast with Undo at the bottom. */
    @Test
    fun wo02RemovedInDarkAndDarkHc() = workoutBoard("WO-02-removed", waitFor = "Undo") { graph ->
        graph.sessions.setStart(today(graph).id, now() - (24 * 60 + 20) * 1_000L)
        onAllNodes(hasContentDescription(getString(Res.string.exercise_options)))[0].performClick()
        waitForIdle()
        onNode(hasText(getString(Res.string.remove_exercise))).performClick()
        waitForIdle()
    }

    /** WO-02-s2 at 24:31 with Bench Press BB swapped for Chest Press Machine through its menu and the picker (F-19, EX-04). */
    @Test
    fun wo02SwapInDarkAndDarkHc() = workoutBoard("WO-02-swap", waitFor = "Swapped for") { graph ->
        graph.sessions.setStart(today(graph).id, now() - (24 * 60 + 31) * 1_000L)
        onAllNodes(hasContentDescription(getString(Res.string.exercise_options)))[0].performClick()
        waitForIdle()
        onNode(hasText(getString(Res.string.swap))).performClick()
        val machine = hasText("Chest Press Machine") and hasClickAction()
        waitUntil(timeoutMillis = 10_000) { onAllNodes(machine).fetchSemanticsNodes().isNotEmpty() }
        onAllNodes(machine)[0].performClick()
        waitForIdle()
    }

    /** A new empty workout at 0:12, Push A discarded: the quick picks are the exercises marked Yes. */
    @Test
    fun wo01MarksInDarkAndDarkHc() = emptyWorkoutBoard("WO-01-marks", "What are you training today?", at = 12)

    /** The same with no marks, so the quick picks are the exercises done most recently, from a pull day two days back. */
    @Test
    fun wo01RecentInDarkAndDarkHc() = emptyWorkoutBoard("WO-01-recent", "Recently done", at = 12) { graph ->
        unmarkAll(graph)
        val day = 86_400_000L
        val pull = graph.sessions.start("Pull", now() - 2 * day)
        val names = listOf("Curl DB", "Hammer Curl DB", "Tricep Pushdown", "Deadlift BB", "Lat Pulldown Cable", "Seated Row Cable")
        graph.sessions.add(pull, names.map { exerciseId(graph, it) })
        graph.sessions.workout(pull).first()!!.exercises.flatMap { it.sets }.forEach { graph.sessions.saveSet(it.copy(weight = 20.0, reps = 10, done = true)) }
        graph.sessions.finish(pull, Finish.WithoutThem, now() - 2 * day + 60 * 60_000L)
    }

    /** No marks and no history: the card alone, no quick picks. */
    @Test
    fun wo01NoneInDarkAndDarkHc() = emptyWorkoutBoard("WO-01-none", "What are you training today?", at = 12) { graph ->
        unmarkAll(graph)
        graph.sessions.history().first().forEach { graph.sessions.discard(it.id) }
        // The card's question shows before and after, so the first mode waits for the picks to go.
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("Quick picks")).fetchSemanticsNodes().isEmpty() }
    }

    /** Bench Press BB from its quick pick at 0:31, with last time's sets (P14): the warm-up in focus. */
    @Test
    fun wo01PickedInDarkAndDarkHc() = emptyWorkoutBoard("WO-01-picked", "Log warm-up", at = 31) { graph ->
        graph.sessions.add(today(graph).id, listOf(exerciseId(graph, "Bench Press BB")), lastTime = true)
    }

    /** WO-01-picked at 31:40 with every set logged, and Finish filled. */
    @Test
    fun wo01LoggedInDarkAndDarkHc() = emptyWorkoutBoard("WO-01-logged", "3 of 3 sets", at = 31 * 60 + 40) { graph ->
        graph.sessions.add(today(graph).id, listOf(exerciseId(graph, "Bench Press BB")), lastTime = true)
        logAll(graph, at = 31 * 60 + 40, "Bench Press BB")
    }

    /** WO-02-s2 scrolled past the focus card: the bar on glass with the progress line, and the strip under it. */
    @Test
    fun wo03StripInDarkAndDarkHc() = workoutBoard("WO-03-strip", waitFor = "24:13 · 1/14 sets") { graph -> pastTheFocusCard(graph) }

    /** WO-03-strip with the rest after Bench Press BB's set 1 at 1:43 of 2:00. */
    @Test
    fun wo03RestInDarkAndDarkHc() = workoutBoard("WO-03-rest", waitFor = "1:43") { graph ->
        graph.rest.value = Rest(now() - 17_000, 120, today(graph).exercises.first { it.name == "Bench Press BB" }.sets[1].id)
        pastTheFocusCard(graph)
    }

    /** WO-02-s2 scrolled to the list's end, the strip on top. */
    @Test
    fun wo03EndInDarkAndDarkHc() = workoutBoard("WO-03-end", waitFor = "Discard workout") {
        onNode(hasScrollToIndexAction()).performScrollToKey("end")
        waitForIdle()
    }

    /** WO-02-s2 with the dock open on Bench Press BB's set 2, as a tap on its row opens it. */
    @Test
    fun wo04NewInDarkAndDarkHc() = workoutBoard("WO-04-new", waitFor = "Bench Press BB · Set 2") { benchDock(row = 3) }

    /** The dock on the logged set 1. */
    @Test
    fun wo04LoggedInDarkAndDarkHc() = workoutBoard("WO-04-logged", waitFor = "Save change") { benchDock(row = 2) }

    /** The dock opened from the rest line after set 2: the rest field focused with the keypad, and Save. */
    @Test
    fun wo04RestInDarkAndDarkHc() = workoutBoard("WO-04-rest", waitFor = "Rest after set 2") {
        onAllNodes(hasContentDescription(getString(Res.string.rest_change, "2")))[0].performClick()
        waitForIdle()
    }

    /** The dock on set 2 with 4+ tapped and 6 chosen: the 4 to 10 row open under the segments, the segment reading 6. */
    @Test
    fun wo04Rir4InDarkAndDarkHc() = workoutBoard("WO-04-rir4", waitFor = "Bench Press BB · Set 2") {
        benchDock(row = 3)
        val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        onAllNodes(hasText("4+") and tab).let { it[it.fetchSemanticsNodes().size - 1] }.performClick()
        waitForIdle()
        onNode(hasText("6") and tab).performClick()
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText("6") and tab and isSelected()).fetchSemanticsNodes().size == 2 }
    }

    /** The keypad on the weight, 8 then 5 typed. */
    @Test
    fun wo05WeightInDarkAndDarkHc() = workoutBoard("WO-05-weight", waitFor = "85") { benchKeypad(nexts = 0) }

    /** The keypad on the reps, fresh: 85 typed into the weight, then Next. */
    @Test
    fun wo05RepsInDarkAndDarkHc() = workoutBoard("WO-05-reps", waitFor = "Next") { benchKeypad(nexts = 1) }

    /** The keypad on the rest, where the button reads Log ›. */
    @Test
    fun wo05RestInDarkAndDarkHc() = workoutBoard("WO-05-rest", waitFor = "Log ›") { benchKeypad(nexts = 2) }
}

/**
 * Opens the dock on Bench Press BB's [row] (the warm-up is row 1) through the row's own action: a click at the row's centre lands on its
 * weight cell, which opens the keypad instead.
 */
@OptIn(ExperimentalTestApi::class)
private suspend fun ComposeUiTest.benchDock(row: Int) {
    onNode(hasTestTag("set Bench Press BB $row")).performSemanticsAction(SemanticsActions.OnClick)
    val weight = getString(Res.string.field_weight)
    waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(weight)).fetchSemanticsNodes().isNotEmpty() }
}

/** The dock on set 2 with its weight typed as 8, 5 on the keypad, then Next tapped [nexts] times. */
@OptIn(ExperimentalTestApi::class)
private suspend fun ComposeUiTest.benchKeypad(nexts: Int) {
    benchDock(row = 3)
    onNode(hasContentDescription(getString(Res.string.field_weight))).performClick()
    waitForIdle()
    "85".forEach { onNode(hasTestTag("key-$it")).performClick(); waitForIdle() }
    repeat(nexts) { onNode(hasText(getString(Res.string.next)) and hasClickAction()).performClick(); waitForIdle() }
}

/**
 * Scrolls Bench Press BB's header to where WO-02-note and WO-03-strip draw it: the board's `.body` starts 72 pt under the bar, past the
 * strip, and 50 pt back from the header's item puts the name there, measured. The card's foot left under the bar is the strip's to cover.
 */
@OptIn(ExperimentalTestApi::class)
private suspend fun ComposeUiTest.pastTheFocusCard(graph: AppGraph) {
    val bench = today(graph).exercises.first { it.name == "Bench Press BB" }
    onNode(hasScrollToIndexAction()).performScrollToKey("h${bench.id}")
    onNode(hasScrollToIndexAction()).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -50f * 3) }
    waitForIdle()
}

private suspend fun exerciseId(graph: AppGraph, name: String) = graph.exercises.all().first().first { it.name == name }.id

/** Logs every set of the [names] exercises as it stands and puts the workout's clock at [at] seconds. */
private suspend fun logAll(graph: AppGraph, at: Int, vararg names: String) {
    val w = today(graph)
    w.exercises.filter { it.name in names }.flatMap { it.sets }.filterNot { it.done }.forEach { graph.sessions.saveSet(it.copy(done = true)) }
    graph.sessions.setStart(w.id, now() - at * 1_000L)
}

private suspend fun today(graph: AppGraph) = graph.sessions.open().first()!!

/** Logs Bench Press BB's sets 2 and 3 as they stand and puts the workout's clock at [at] seconds. */
private suspend fun benchLogged(graph: AppGraph, at: Int): SessionExercise {
    val w = today(graph)
    val bench = w.exercises.first { it.name == "Bench Press BB" }
    bench.sets.filterNot { it.done }.forEach { graph.sessions.saveSet(it.copy(done = true)) }
    graph.sessions.setStart(w.id, now() - at * 1_000L)
    return bench
}

/** Logs the exercise's first set as it stands, with RIR 2, as the boards draw it. */
private suspend fun logFirst(graph: AppGraph, name: String) {
    val set = today(graph).exercises.first { it.name == name }.sets.first()
    graph.sessions.saveSet(set.copy(done = true, rir = 2))
}

/**
 * A workout board: the fixture's left-open Push A opened from its bar, the restore toast dismissed unless [dismissToast] is false,
 * then [arrange]. Later tasks add one `<id>InDarkAndDarkHc` per board through it.
 */
@OptIn(ExperimentalTestApi::class)
private fun workoutBoard(id: String, waitFor: String, dismissToast: Boolean = true, arrange: suspend ComposeUiTest.(AppGraph) -> Unit = {}) =
    boardShots(id, DarkModes, waitFor, arrange = { g -> openTheWorkout(); if (dismissToast) g.toasts.dismiss(); arrange(g) }) { ShellBoard() }

/** Takes every exercise's mark away, so the quick picks fall back to history. */
private suspend fun unmarkAll(graph: AppGraph) =
    graph.exercises.all().first().filter { it.mark != null }.forEach { graph.exercises.setMark(it.id, null) }

/**
 * An empty-workout board (WO-01): the fixture's Push A discarded and a new workout started [at] seconds ago, opened from its bar with the
 * restore toast dismissed, then [arrange].
 */
@OptIn(ExperimentalTestApi::class)
private fun emptyWorkoutBoard(id: String, waitFor: String, at: Int, arrange: suspend ComposeUiTest.(AppGraph) -> Unit = {}) =
    boardShots(id, DarkModes, waitFor, arrange = { g ->
        g.sessions.discard(today(g).id)
        g.sessions.start(getString(Res.string.workout), now() - at * 1_000L)
        openTheWorkout()
        g.toasts.dismiss()
        arrange(g)
    }) { ShellBoard() }
