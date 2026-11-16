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
}

/**
 * A workout board: the fixture's left-open Push A opened from its bar, the restore toast dismissed unless [dismissToast] is false,
 * then [arrange]. Later tasks add one `<id>InDarkAndDarkHc` per board through it.
 */
@OptIn(ExperimentalTestApi::class)
private fun workoutBoard(id: String, waitFor: String, dismissToast: Boolean = true, arrange: suspend ComposeUiTest.(AppGraph) -> Unit = {}) =
    boardShots(id, DarkModes, waitFor, arrange = { g -> openTheWorkout(); if (dismissToast) g.toasts.dismiss(); arrange(g) }) { ShellBoard() }
