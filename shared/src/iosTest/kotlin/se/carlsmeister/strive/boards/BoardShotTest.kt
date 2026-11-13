package se.carlsmeister.strive.boards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.LocalHighContrast
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.Accent
import se.carlsmeister.strive.ui.Toggle
import se.carlsmeister.strive.ui.ToggleRow
import se.carlsmeister.strive.ui.moment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The system's galleries, shot in every mode at 390 × 844 pt into [BOARD_SHOTS] for a person to set beside the cards. */
@OptIn(ExperimentalTestApi::class)
class BoardShotTest {
    @Test
    fun iconsInFourModes() = boardShots("Icons", Mode.entries) { IconsBoard() }

    @Test
    fun momentsInFourModes() = boardShots("Moments", Mode.entries) { MomentsBoard() }

    @Test
    fun buttonsInDarkAndDarkHc() = boardShots("Buttons", DarkModes) { ButtonsBoard() }

    @Test
    fun cardsAndStatsInDarkAndDarkHc() = boardShots("CardsAndStats", DarkModes) { CardsAndStatsBoard() }

    @Test
    fun controlsInDarkAndDarkHc() = boardShots("Controls", DarkModes) { ControlsBoard() }

    @Test
    fun listsInDarkAndDarkHc() = boardShots("Lists", DarkModes) { ListsBoard() }

    /** A switch row toggles from a tap on its text; a switch on its own draws 32 dp but is a touch (44 dp) tall. */
    @Test
    fun aSwitchRowTogglesFromItsTextAndASwitchIsATouchTall() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        var on by mutableStateOf(false)
        setContent { StriveTheme { Column {
            ToggleRow("Keep the screen awake", on, { on = it }, Modifier.testTag("row"))
            Toggle(false, {}, "Alone", Modifier.testTag("alone"))
        } } }
        onNodeWithText("Keep the screen awake", useUnmergedTree = true).performClick()
        assertTrue(on)
        onNodeWithTag("row").assertIsOn()
        assertTrue(onNodeWithTag("alone").fetchSemanticsNode().size.height >= 44 * 3)
    }

    /** .moment draws its edge inset: a marked card takes exactly the room an unmarked one does (moments.css). */
    @Test
    fun theMomentEdgeChangesNoSize() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        setContent {
            CompositionLocalProvider(LocalHighContrast provides true) { StriveTheme { Column {
                Box(Modifier.testTag("plain").padding(16.dp)) { Text("82.5 kg") }
                Box(Modifier.testTag("moment").moment(Accent.Sun, MaterialTheme.shapes.large).padding(16.dp)) { Text("82.5 kg") }
            } } }
        }
        assertEquals(onNodeWithTag("plain").fetchSemanticsNode().size, onNodeWithTag("moment").fetchSemanticsNode().size)
    }
}
