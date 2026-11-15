package se.carlsmeister.strive.boards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import se.carlsmeister.strive.theme.LocalDarkTheme
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.glass
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
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.LocalHighContrast
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.Accent
import se.carlsmeister.strive.ui.ActionButton
import se.carlsmeister.strive.ui.ActionKind
import se.carlsmeister.strive.ui.ChoiceChip
import se.carlsmeister.strive.ui.InputField
import se.carlsmeister.strive.ui.LargeTitle
import se.carlsmeister.strive.ui.ListBlock
import se.carlsmeister.strive.ui.ListGroup
import se.carlsmeister.strive.ui.ListRow
import se.carlsmeister.strive.ui.MomentTag
import se.carlsmeister.strive.ui.MorePill
import se.carlsmeister.strive.ui.NavBar
import se.carlsmeister.strive.ui.NavText
import se.carlsmeister.strive.ui.RoundButton
import se.carlsmeister.strive.ui.RoundKind
import se.carlsmeister.strive.ui.Stat
import se.carlsmeister.strive.ui.StatTiles
import se.carlsmeister.strive.ui.SurfaceCard
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
    fun tabBarInDarkAndDarkHc() = boardShots("TabBar", DarkModes) { TabBarBoard() }

    @Test
    fun titleBarsInDarkAndDarkHc() = boardShots("TitleBars", DarkModes, waitFor = "Push A") { TitleBarsBoard() }

    /** A board's `waitFor` names what it shows once arranged, so a text only `arrange` brings on screen is waited for, not timed out on. */
    @Test
    fun boardShotsWaitsForWhatTheBoardShowsOnceArranged() {
        var arranged by mutableStateOf(false)
        boardShots("ArrangeProbe", DarkModes, waitFor = "Arranged", arrange = { arranged = true }) { if (arranged) Text("Arranged") }
    }

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

    /**
     * A row's own chevron is `.chev`, a block holding `.row`'s 22 px line box, and its icon is `vertical-align: middle`: its
     * middle sits half an x-height above the baseline, 1.53 px below the line box's middle, and Chrome paints the mask on whole
     * pixels, so the Lists card draws it 2 pt below the row's middle. A value's chevron sits in `.val`, a flex box that centres it.
     */
    @Test
    fun aRowsOwnChevronSitsTwoPointsLowAndAValuesChevronCentred() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        setContent { StriveTheme { Column {
            ListRow("Profile", Modifier.testTag("chev"), leading = StriveIcons.You, chevron = true)
            ListRow("Export", Modifier.testTag("val"), leading = StriveIcons.Share, value = "18 workouts", chevron = true)
        } } }
        // The chevron's middle below the row's, in px: the marks in the 16 dp before the row's 16 dp end padding.
        fun chevronDrop(tag: String): Float {
            val pixels = onNodeWithTag(tag).captureToImage().toPixelMap()
            val blank = pixels[2, 2]
            val xs = pixels.width - 32 * 3 until pixels.width - 16 * 3
            val ys = (0 until pixels.height).filter { y -> xs.any { x -> pixels[x, y] != blank } }
            return (ys.first() + ys.last() + 1) / 2f - pixels.height / 2f
        }
        assertEquals(2f * 3, chevronDrop("chev"), 1f, "Profile's chevron")
        assertEquals(0f, chevronDrop("val"), 1f, "Export's chevron")
    }

    /**
     * `.glass`'s box-shadow paints only outside the box, as CSS does: over content, a glass capsule's middle is its tint over that
     * content, not darkened by its own shadow underneath.
     */
    @Test
    fun glassPaintsItsShadowOnlyOutsideItsShape() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        var expected = Color.Unspecified
        setContent {
            CompositionLocalProvider(LocalDarkTheme provides true) { StriveTheme {
                expected = StriveTheme.glass.regular.compositeOver(Color.Gray)
                Box(Modifier.testTag("behind").background(Color.Gray).padding(48.dp)) {
                    Box(Modifier.size(200.dp, 100.dp).glass(null, StriveTheme.glass, shape = ShapeFull))
                }
            } }
        }
        val pixels = onNodeWithTag("behind").captureToImage().toPixelMap()
        val middle = pixels[pixels.width / 2, pixels.height / 2]
        for ((name, got, want) in listOf(Triple("red", middle.red, expected.red), Triple("green", middle.green, expected.green), Triple("blue", middle.blue, expected.blue)))
            assertEquals(want, got, 2f / 255, "$name: the middle $middle, the tint over grey $expected")
        val below = pixels[pixels.width / 2, (48 + 100 + 6) * 3] // 6 dp under the capsule, where its 8 dp offset shadow falls
        assertTrue(below.red < Color.Gray.red - 0.05f, "the shadow under the capsule: $below")
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

    /**
     * Figures follow each card's cascade: a rule that sets `font:` resets `.strive`'s tabular-nums, so its text draws proportional
     * figures (no card uses `.tnum`); a switch row's text inherits `.strive`'s font untouched and keeps them.
     */
    @Test
    fun figuresAreProportionalWhereTheCardsFontShorthandResetsThem() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        setContent { StriveTheme { Column {
            SurfaceCard("Push A 1", eyebrow = "Week 6 of 8", meta = "15 sets") { ActionButton("Start 2", {}, kind = ActionKind.Filled) }
            StatTiles(listOf(Stat("Time 3", "64", unit = "min 4", sub = "goal 2–3")))
            ListGroup("Account 5") { ListBlock(listOf { ListRow("Rest 6", value = "2:00") }) }
            NavBar("Library 7", back = "Plan 8") { NavText("Edit 9", {}); NavText("Save 10", {}, bold = true) }
            LargeTitle("Week 11")
            WorkoutBar({}, {}, {}, title = "Push B 12", progress = "24:13 · 1/14 sets")
            ActionButton("Show 13", {}, small = true)
            ActionButton("Cancel 14", {}, kind = ActionKind.Text)
            ChoiceChip("Pull 15", selected = true, onClick = {})
            ChoiceChip("Press 16", selected = false, onClick = {})
            InputField("Push A 17", {})
            InputField("", {}, placeholder = "Search 553 exercises")
            MomentTag("PR 82.5", Accent.Sun)
            ToggleRow("Keep 18 awake", false, {})
        } } }
        fun features(text: String): String? {
            val layouts = mutableListOf<TextLayoutResult>()
            onNodeWithText(text, useUnmergedTree = true).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(layouts)
            return layouts.single().layoutInput.style.fontFeatureSettings
        }
        val proportional = listOf(
            "Week 6 of 8", "Push A 1", "15 sets", "Start 2", "Time 3", "64", "min 4", "goal 2–3", "Account 5", "Rest 6", "2:00",
            "Library 7", "Plan 8", "Edit 9", "Save 10", "Week 11", "Push B 12", "24:13 · 1/14 sets", "Show 13", "Cancel 14",
            "Pull 15", "Press 16", "Push A 17", "Search 553 exercises", "PR 82.5",
        )
        assertEquals(proportional.map { it to null }, proportional.map { it to features(it) })
        assertEquals("tnum", features("Keep 18 awake"))
    }
}
