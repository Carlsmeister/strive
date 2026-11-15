package se.carlsmeister.strive.boards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.LocalHighContrast
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.Accent
import se.carlsmeister.strive.ui.MorePill
import se.carlsmeister.strive.ui.NavBar
import se.carlsmeister.strive.ui.NavText
import se.carlsmeister.strive.ui.RoundButton
import se.carlsmeister.strive.ui.RoundKind
import se.carlsmeister.strive.ui.Toggle
import se.carlsmeister.strive.ui.ToggleRow
import se.carlsmeister.strive.ui.WorkoutBar
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

    @Test
    fun titleBarsInDarkAndDarkHc() = boardShots("TitleBars", DarkModes, waitFor = "Push A") { TitleBarsBoard() }

    /**
     * A switch row toggles from a tap on its text; a switch on its own draws 32 dp but is a touch (44 dp) tall. A switch row alone
     * takes taps across a touch too, yet keeps its 32 dp of layout, so nothing drawn moves.
     */
    @Test
    fun aSwitchRowTogglesFromItsTextAndASwitchIsATouchTall() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        var on by mutableStateOf(false)
        setContent { StriveTheme { Column {
            Box(Modifier.testTag("rowFootprint")) { ToggleRow("Keep the screen awake", on, { on = it }, Modifier.testTag("row")) }
            Toggle(false, {}, "Alone", Modifier.testTag("alone"))
        } } }
        onNodeWithText("Keep the screen awake", useUnmergedTree = true).performClick()
        assertTrue(on)
        onNodeWithTag("row").assertIsOn()
        assertTrue(onNodeWithTag("alone").fetchSemanticsNode().size.height >= 44 * 3)
        assertTrue(onNodeWithTag("row").fetchSemanticsNode().size.height >= 44 * 3, "the row's tap area")
        assertEquals(32 * 3, onNodeWithTag("rowFootprint").fetchSemanticsNode().size.height, "the row's layout")
    }

    /** The ⋯ pill draws 40 × 32 and keeps that as its layout, yet takes taps across a touch (44 dp) each way. */
    @Test
    fun aMorePillIsATouchEachWayAndKeepsItsLayout() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        setContent { StriveTheme { Box(Modifier.testTag("pillFootprint")) { MorePill("More", {}, Modifier.testTag("pill")) } } }
        val tapArea = onNodeWithTag("pill").fetchSemanticsNode().size
        val footprint = onNodeWithTag("pillFootprint").fetchSemanticsNode().size
        assertTrue(tapArea.width >= 44 * 3 && tapArea.height >= 44 * 3, "the pill's tap area: $tapArea")
        assertEquals(40 * 3 to 32 * 3, footprint.width to footprint.height, "the pill's layout")
    }

    /**
     * Scrolled, the workout's name fades into the bar's middle; cleared, it keeps showing while it fades away, then goes (the
     * TitleBars README). Finish draws a 40 dp capsule but is a touch (44 dp) tall.
     */
    @Test
    fun theWorkoutBarsNameFadesInAndOutAndFinishIsATouchTall() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        var title by mutableStateOf<String?>(null)
        mainClock.autoAdvance = false
        setContent { StriveTheme { WorkoutBar({}, {}, {}, title = title, progress = "24:13 · 1/14 sets") } }
        mainClock.advanceTimeBy(500)
        onNodeWithText("Push A").assertDoesNotExist()
        title = "Push A"
        mainClock.advanceTimeBy(500)
        onNodeWithText("Push A").assertExists()
        title = null
        mainClock.advanceTimeBy(100)
        onNodeWithText("Push A").assertExists() // mid-fade, still the name it had
        mainClock.advanceTimeBy(500)
        onNodeWithText("Push A").assertDoesNotExist()
        assertTrue(onAllNodes(hasClickAction()).onLast().fetchSemanticsNode().size.height >= 44 * 3)
    }

    /**
     * A long title ends in an ellipsis and leaves the sides what they need: on a 390 dp phone every side control of both bars stays
     * inside its bar and at least a touch (44 dp) wide, and the ⋯ pill, drawn 32 × 24 beside the workout's name, takes taps across a
     * touch each way.
     */
    @Test
    fun aLongTitleLeavesTheBarsSideControlsATouchWide() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        setContent { StriveTheme { Column {
            NavBar("Bulgarian Split Squat DB with a long pause", Modifier.testTag("nav"), back = "Library") {
                RoundButton(StriveIcons.More, "More", {}, kind = RoundKind.Plain)
                RoundButton(StriveIcons.Plus, "Add", {}, kind = RoundKind.Teal)
            }
            WorkoutBar({}, {}, {}, Modifier.testTag("wtb"), title = "Upper Body Strength and Conditioning", progress = "24:13 · 1/14 sets")
        } } }
        // Left to right, not by label: the simulator may run in Swedish. The nav bar's are back, More, Add; the workout bar's ⌄, ⏱, ⋯, Finish.
        fun taps(bar: String) =
            onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag(bar))).fetchSemanticsNodes().map { it.boundsInRoot }.sortedBy { it.left }
        val nav = taps("nav")
        val (minimise, rest, pill, finish) = taps("wtb").also { assertEquals(4, it.size) }
        assertEquals(3, nav.size)
        for ((bar, sides) in listOf("nav" to nav, "wtb" to listOf(minimise, rest, finish))) {
            val edge = onNodeWithTag(bar).fetchSemanticsNode().boundsInRoot
            for (b in sides) assertTrue(b.width >= 44 * 3 && b.left >= edge.left && b.right <= edge.right, "$bar: $b, bar $edge")
        }
        assertTrue(minimise.right <= rest.left, "⌄ $minimise, ⏱ $rest")
        // Its tap area is centred on what it draws: 32 dp wide, 6 dp after the name, its right edge clear of Finish.
        val name = onNodeWithText("Upper Body Strength and Conditioning").fetchSemanticsNode().boundsInRoot
        val (drawnLeft, drawnRight) = pill.center.x - 16 * 3 to pill.center.x + 16 * 3
        assertTrue(pill.width >= 44 * 3 && pill.height >= 44 * 3 && pill.left >= rest.right && drawnRight <= finish.left, "the pill: $pill")
        assertEquals(name.right + 6 * 3, drawnLeft, .5f, "the pill beside the name: $pill, name $name")
    }

    /** `.navtext` is a touch tall and acts; `.navtext.dim` cannot act yet. */
    @Test
    fun aNavTextIsATouchTallAndADimOneIsDisabled() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        setContent { StriveTheme { Row {
            NavText("Edit", {}, bold = true)
            NavText("Save", {}, dim = true)
        } } }
        onNodeWithText("Edit").assertIsEnabled().assertHasClickAction()
        onNodeWithText("Save").assertIsNotEnabled()
        assertTrue(onNodeWithText("Edit").fetchSemanticsNode().size.height >= 44 * 3)
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
