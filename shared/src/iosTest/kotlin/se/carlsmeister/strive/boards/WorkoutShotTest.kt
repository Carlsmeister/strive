package se.carlsmeister.strive.boards

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
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
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.workout.now
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.exercise_options
import strive.shared.generated.resources.note
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
        val bench = graph.sessions.open().first()!!.exercises.first { it.name == "Bench Press BB" }
        onAllNodes(hasContentDescription(getString(Res.string.exercise_options)))[0].performClick()
        waitForIdle()
        onNode(hasText(getString(Res.string.note))).performClick()
        waitForIdle()
        // The board's `.body` starts 72 pt under the bar, past the strip. 50 pt back from the header's item puts the name where the
        // board draws it, measured. The strip shows only once the card has scrolled wholly away, so the card's foot shows there instead.
        onNode(hasScrollToIndexAction()).performScrollToKey("h${bench.id}")
        onNode(hasScrollToIndexAction()).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -50f * 3) }
        waitForIdle()
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
