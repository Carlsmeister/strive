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
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import se.carlsmeister.strive.App
import se.carlsmeister.strive.data.LocalAppGraph
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.tab_home
import strive.shared.generated.resources.tab_start
import kotlin.test.Test

/** The shell's boards, shot from Carl's sample data into [BOARD_SHOTS] for a person to set beside the boards. */
@OptIn(ExperimentalTestApi::class)
class ShellShotTest {
    /** Main's tab bar: no workout open, Home current. The Home behind it is #71's (R3). */
    @Test
    fun mainInDarkAndDarkHc() = boardShots("Main", DarkModes, waitFor = text(Res.string.tab_home),
        arrange = { graph ->
            graph.sessions.discard(graph.sessions.open().first()!!.id)
            val start = getString(Res.string.tab_start)
            waitUntil(timeoutMillis = 10_000) {
                onAllNodes(hasContentDescription(start)).fetchSemanticsNodes().size == 1 &&
                    onAllNodes(hasText("Push A", substring = true)).fetchSemanticsNodes().isEmpty()
            }
        },
    ) { ShellBoard() }
}

/** [res] in the simulator's language, which need not be the boards' English. */
private fun text(res: StringResource) = runBlocking { getString(res) }

/** The app as a board frames it: under the board's 54 pt `.safe` band (`--safe-top`), which the test window does not have. */
@Composable
private fun ShellBoard() = Box(Modifier.padding(top = 54.dp)) { App(LocalAppGraph.current) }

/** Taps the tab labelled [tab]. */
@OptIn(ExperimentalTestApi::class)
private suspend fun ComposeUiTest.tapTab(tab: StringResource) {
    onNode(hasText(getString(tab)) and hasClickAction()).performClick()
    waitForIdle()
}
