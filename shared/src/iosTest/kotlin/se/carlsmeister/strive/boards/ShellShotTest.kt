package se.carlsmeister.strive.boards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import se.carlsmeister.strive.App
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.fixture.SampleData
import se.carlsmeister.strive.flows.clickLabel
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.back_to_workout
import strive.shared.generated.resources.finish
import strive.shared.generated.resources.minimise
import strive.shared.generated.resources.tab_food
import strive.shared.generated.resources.tab_plan
import strive.shared.generated.resources.tab_start
import kotlin.test.Test

/** The shell's boards, shot from Carl's sample data into [BOARD_SHOTS] for a person to set beside the boards. */
@OptIn(ExperimentalTestApi::class)
class ShellShotTest {
    /** Main's tab bar: no workout open, Home current. The Home behind it is #71's (R3). */
    @Test
    fun mainInDarkAndDarkHc() = boardShots("Main", DarkModes, waitFor = "Home",
        arrange = { graph ->
            graph.sessions.discard(graph.sessions.open().first()!!.id)
            val start = getString(Res.string.tab_start)
            waitUntil(timeoutMillis = 10_000) {
                onAllNodes(hasContentDescription(start)).fetchSemanticsNodes().size == 1 &&
                    onAllNodes(hasText("Push A", substring = true)).fetchSemanticsNodes().isEmpty()
            }
        },
    ) { ShellBoard() }

    /** The fixture's Push A, open as the app starts, reads as left open. The Home behind is #71's (R3). */
    @Test
    fun wo17KilledInDarkAndDarkHc() = boardShots("WO-17-killed", DarkModes, waitFor = "Tap to continue") { ShellBoard() }

    /** Opened once and minimised, on Plan. The Plan week behind is a later issue's (R3). */
    @Test
    fun wo17PlanInDarkAndDarkHc() = boardShots("WO-17-plan", DarkModes, waitFor = "1/14", arrange = { backOnTheTabs(); tapTab(Res.string.tab_plan) }) { ShellBoard() }

    /** A two-minute rest 17 s in: the ring 103/120 full and 1:43. The Home behind is #71's (R3). */
    @Test
    fun wo17RestInDarkAndDarkHc() = boardShots("WO-17-rest", DarkModes, waitFor = "1:43",
        arrange = { graph -> backOnTheTabs(); graph.rest.value = Rest(SampleData.now.toEpochMilliseconds() - 17_000, 120) }) { ShellBoard() }

    /** Reduce Transparency: both bars one solid surface. The board's chevron is P3's deviation, the Home behind R3's. */
    @Test
    fun sh01ReduceTransparencyInDarkAndDarkHc() =
        boardShots("SH-01-rt", DarkModes, waitFor = "1/14", reduceTransparency = true, arrange = { backOnTheTabs() }) { ShellBoard() }

    /** Start with Push A open: one bubble, Continue. The bar's chevron is P3's deviation, the Home behind R3's. */
    @Test
    fun sh03InProgressInDarkAndDarkHc() = boardShots("SH-03-inprog", DarkModes, waitFor = "Continue",
        arrange = { backOnTheTabs(); onNode(hasContentDescription(getString(Res.string.tab_start))).performClick(); waitForIdle() },
    ) { ShellBoard() }

    /** Food's placeholder: no workout open, the sample day's date under the title. */
    @Test
    fun sh05InDarkAndDarkHc() = boardShots("SH-05", DarkModes, waitFor = "Nutrition comes later",
        arrange = { graph -> graph.sessions.discard(graph.sessions.open().first()!!.id); tapTab(Res.string.tab_food) }) { ShellBoard() }
}

/** The app as a board frames it: under the board's 54 pt `.safe` band (`--safe-top`), which the test window does not have. */
@Composable
private fun ShellBoard() = Box(Modifier.padding(top = 54.dp)) { App(LocalAppGraph.current) }

/** Into the left-open workout through its bar and back, so the bar reads as an open workout's. */
@OptIn(ExperimentalTestApi::class)
private suspend fun ComposeUiTest.backOnTheTabs() {
    val back = clickLabel(getString(Res.string.back_to_workout))
    waitUntil(timeoutMillis = 10_000) { onAllNodes(back).fetchSemanticsNodes().isNotEmpty() }
    onNode(back).performClick()
    val finish = getString(Res.string.finish)
    waitUntil(timeoutMillis = 10_000) { onAllNodes(hasText(finish)).fetchSemanticsNodes().isNotEmpty() }
    onNode(hasContentDescription(getString(Res.string.minimise))).performClick()
    waitUntil(timeoutMillis = 10_000) { onAllNodes(back).fetchSemanticsNodes().isNotEmpty() }
    waitForIdle()
}

/** Taps the tab labelled [tab]. */
@OptIn(ExperimentalTestApi::class)
private suspend fun ComposeUiTest.tapTab(tab: StringResource) {
    onNode(hasText(getString(tab)) and hasClickAction()).performClick()
    waitForIdle()
}
