package se.carlsmeister.strive.boards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
import se.carlsmeister.strive.ui.Bubble
import se.carlsmeister.strive.ui.BubbleItem
import se.carlsmeister.strive.ui.BubblePanel
import se.carlsmeister.strive.ui.RestRing
import se.carlsmeister.strive.ui.TabBar
import se.carlsmeister.strive.ui.TabItem
import se.carlsmeister.strive.ui.WorkoutMiniBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.Accent
import se.carlsmeister.strive.ui.ActionButton
import se.carlsmeister.strive.ui.ActionKind
import se.carlsmeister.strive.ui.ButtonRow
import se.carlsmeister.strive.ui.ChoiceChip
import se.carlsmeister.strive.ui.InputField
import se.carlsmeister.strive.ui.LargeTitle
import se.carlsmeister.strive.ui.ListBlock
import se.carlsmeister.strive.ui.ListGroup
import se.carlsmeister.strive.ui.ListRow
import se.carlsmeister.strive.ui.MomentNumber
import se.carlsmeister.strive.ui.MomentTag
import se.carlsmeister.strive.ui.GlassMenu
import se.carlsmeister.strive.ui.MenuRow
import se.carlsmeister.strive.ui.ActionSheetPanel
import se.carlsmeister.strive.ui.AlertBox
import se.carlsmeister.strive.ui.AlertPanel
import se.carlsmeister.strive.ui.SheetAction
import se.carlsmeister.strive.ui.MenuTile
import se.carlsmeister.strive.ui.MenuTiles
import se.carlsmeister.strive.theme.LocalGlassSource
import androidx.compose.runtime.CompositionLocalProvider
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import se.carlsmeister.strive.ui.MorePill
import se.carlsmeister.strive.ui.NavBar
import se.carlsmeister.strive.ui.RoundButton
import se.carlsmeister.strive.ui.RoundKind
import se.carlsmeister.strive.ui.RowTone
import se.carlsmeister.strive.ui.Stat
import se.carlsmeister.strive.ui.StatTiles
import se.carlsmeister.strive.ui.SurfaceCard
import se.carlsmeister.strive.ui.ToggleRow
import se.carlsmeister.strive.ui.WorkoutBar
import se.carlsmeister.strive.ui.edge
import se.carlsmeister.strive.ui.moment
import se.carlsmeister.strive.workout.now
import se.carlsmeister.strive.workout.progress

/** The Icons card: every glyph at 24 dp over its CSS name, in bundle.css's order; 6 columns, not the card's 8, on a phone (R6). */
@Composable
internal fun IconsBoard() = Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    val colors = MaterialTheme.colorScheme
    // `.cap` at the card's 10 px: label-medium's 16 sp line box and weight, a smaller size.
    val cap = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeightStyle = CssLineBox)
    StriveIcons.all.chunked(6).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (name, icon) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(icon, contentDescription = null, Modifier.size(24.dp), tint = colors.onSurface)
                    // One line, as on the card's wider columns: "arrow-down" overhangs its 53 dp column into the gap instead of wrapping.
                    Text(name, Modifier.wrapContentWidth(unbounded = true), color = colors.onSurfaceVariant, style = cap, softWrap = false)
                }
            }
            repeat(6 - row.size) { Spacer(Modifier.weight(1f)) } // the last row keeps the grid's column width
        }
    }
}

/** moments.css's layer on a `.card` per accent: the edge, the wash, an accent icon, the celebration face beside the system face, a tag. */
@Composable
internal fun MomentsBoard() = Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    MomentCard(Accent.Sun, StriveIcons.Star, "82.5 kg", "Record")
    MomentCard(Accent.Flame, StriveIcons.Flame, "21 days", "3-week streak")
    MomentCard(Accent.Lime, StriveIcons.Check, "3 746 kg", "Done")
    MomentCard(Accent.Lime, StriveIcons.Check, "3 746 kg", "Done", onHigh = true)
}

/** A `.card.moment` (`.on-high` on `surfaceContainerHigh`): the icon and tag over the number in Unbounded and in the system face. */
@Composable
private fun MomentCard(accent: Accent, icon: ImageVector, number: String, tag: String, onHigh: Boolean = false) {
    Box(Modifier.fillMaxWidth().moment(accent, MaterialTheme.shapes.large, onHigh).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, Modifier.size(24.dp), tint = StriveTheme.moments.edge(accent))
                MomentTag(tag, accent, icon = icon)
            }
            // On one baseline, as two inline numbers sit in CSS; their line boxes differ, so bottom alignment would offset them.
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MomentNumber(number, accent, MaterialTheme.typography.displaySmall, Modifier.alignByBaseline(), celebrate = true)
                MomentNumber(number, accent, MaterialTheme.typography.displaySmall, Modifier.alignByBaseline())
            }
        }
    }
}

/** The Buttons card: the three wide buttons, the `.btnrow`, then the round buttons, the ⋯ pill and a disabled Save. */
@Composable
internal fun ButtonsBoard() = Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    ActionButton("Start workout", {}, Modifier.fillMaxWidth(), kind = ActionKind.Filled)
    ActionButton("Edit", {}, Modifier.fillMaxWidth())
    ActionButton("Delete template", {}, Modifier.fillMaxWidth(), kind = ActionKind.Danger)
    ButtonRow {
        ActionButton("Show all", {}, small = true)
        ActionButton("Deload now", {}, kind = ActionKind.Filled, small = true)
        ActionButton("Cancel", {}, kind = ActionKind.Text)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        RoundButton(StriveIcons.Timer, "Timer", {}, iconSize = 20.dp)
        RoundButton(StriveIcons.More, "More", {}, kind = RoundKind.Plain)
        MorePill("More", {})
        RoundButton(StriveIcons.Sliders, "Filters", {}, kind = RoundKind.On, iconSize = 20.dp)
        ActionButton("Save", {}, enabled = false)
    }
}

/** The CardsAndStats card: the planned workout's card with its button, then four stat tiles. */
@Composable
internal fun CardsAndStatsBoard() = Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SurfaceCard(
        "Push A", Modifier.fillMaxWidth(), eyebrow = "Planned today · Week 6 of 8", meta = "5 exercises · 15 sets · about 30 min",
        titleStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight(600)), // the card's inline weight
    ) { ActionButton("Start Push A", {}, Modifier.fillMaxWidth(), kind = ActionKind.Filled) }
    StatTiles(
        listOf(Stat("Time", "64", unit = "min"), Stat("Avg RIR", "1.5", sub = "goal 2–3"), Stat("Sets", "15"), Stat("Records", "3")),
        Modifier.fillMaxWidth(),
    )
}

/** The Controls card without `.segc` and `.seg` (R4, #57's): the chips scrolling in their row, two fields (the second focused), the switch. */
@Composable
internal fun ControlsBoard() = Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceChip("Vertical pull", selected = true, onClick = {})
        ChoiceChip("Horizontal press", selected = false, onClick = {})
        ChoiceChip("Knee dominant", selected = false, onClick = {})
    }
    InputField("", {}, Modifier.fillMaxWidth(), placeholder = "Search 553 exercises", leading = StriveIcons.Search)
    val focus = remember { FocusRequester() }
    InputField("Push A", {}, Modifier.fillMaxWidth().focusRequester(focus))
    LaunchedEffect(Unit) { focus.requestFocus() } // `.field.focus`: the ring and the caret
    ToggleRow("Keep the screen awake", checked = true, onCheckedChange = {}, Modifier.fillMaxWidth())
}

/** The Lists card: the Account group's block with leading tiles, then a short list as joined tiles. */
@Composable
internal fun ListsBoard() = Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    ListGroup("Account") {
        ListBlock(
            listOf(
                { ListRow("Profile", leading = StriveIcons.You, chevron = true, onClick = {}) },
                { ListRow("Settings", leading = StriveIcons.Gear, chevron = true, onClick = {}) },
                { ListRow("Export", leading = StriveIcons.Share, value = "18 workouts", chevron = true, onClick = {}) },
            ),
            hairlineInset = 56.dp, // `.row.lead + .row.lead`
        )
    }
    ListBlock(
        listOf(
            { ListRow("Default rest", value = "2:00", onClick = {}) },
            { ListRow("Units", value = "kg", onClick = {}) },
            { ListRow("Add gear", tone = RowTone.Action, centered = true, onClick = {}) },
        ),
        tiles = true,
    )
}

/**
 * The TitleBars card's four bars, each the phone's width: Library's nav bar, Plan's large title, the workout's bar at rest, then
 * scrolled on glass with Finish filled as when every set is logged, named and counted from the fixture's open Push A (`1/14`,
 * where the card draws `2/15`: R5).
 */
@Composable
internal fun TitleBarsBoard() = Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    NavBar("Library", back = "Plan") {
        RoundButton(StriveIcons.More, "More", {}, kind = RoundKind.Plain)
        RoundButton(StriveIcons.Plus, "Add", {}, kind = RoundKind.Teal)
    }
    // The card's inline `padding: 0 16px` replaces `.lt`'s 4 dp on top, so the board takes those 4 dp back.
    LargeTitle("Plan", Modifier.padding(horizontal = 16.dp).layout { m, c ->
        val p = m.measure(c)
        val top = 4.dp.roundToPx()
        layout(p.width, p.height - top) { p.place(0, -top) }
    }) { RoundButton(StriveIcons.More, "More", {}, kind = RoundKind.Plain) }
    WorkoutBar({}, {}, {})
    val sessions = LocalAppGraph.current.sessions
    val w by remember { sessions.open() }.collectAsState(null)
    WorkoutBar({}, {}, {}, allLogged = true, glassed = true, hairline = true, title = w?.name, progress = w?.let { progress(it, now()) })
}

/**
 * The TabBar card: its 390 × 200 box (border-box, so the stack sits inside the 1 dp edge) holding the bottom stack, the minimised
 * Push A resting with the card's 22 dp ring (P4) over the tab bar, Home current.
 */
@Composable
internal fun TabBarBoard() {
    val colors = MaterialTheme.colorScheme
    val corner = RoundedCornerShape(16.dp)
    Box(
        Modifier.padding(top = 16.dp).fillMaxWidth().height(200.dp).clip(corner).background(colors.background)
            .border(1.dp, colors.outlineVariant, corner).padding(1.dp),
    ) {
        Column(Modifier.align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutMiniBar("Push A", "24:13 · 2/15 sets", "Back to the workout", {}, rest = RestRing(103, 120, 22.dp, 2.2.dp))
            TabBar(
                before = listOf(TabItem(StriveIcons.Home, "Home", true) {}, TabItem(StriveIcons.Food, "Food", false) {}),
                after = listOf(TabItem(StriveIcons.Plan, "Plan", false) {}, TabItem(StriveIcons.You, "You", false) {}),
                startLabel = "Start", startOpen = false, onStart = {},
            )
        }
    }
}

/**
 * The StartBubbles card: its 390 × 320 box (border-box, as the TabBar card's) with the panel at left 26, right 26, bottom 72 and
 * 170 tall, the card's four bubbles at their centres, and the tab bar with Start open over them.
 */
@Composable
internal fun StartBubblesBoard() {
    val colors = MaterialTheme.colorScheme
    val corner = RoundedCornerShape(16.dp)
    Box(
        Modifier.padding(top = 16.dp).fillMaxWidth().height(320.dp).clip(corner).background(colors.background)
            .border(1.dp, colors.outlineVariant, corner).padding(1.dp),
    ) {
        BubblePanel(Modifier.align(Alignment.BottomCenter).padding(start = 26.dp, end = 26.dp, bottom = 72.dp).fillMaxWidth().height(170.dp))
        Bubble(BubbleItem(StriveIcons.Lift, "Push A", "today", filled = true) {}, Modifier.bubbleAt(80.dp, 160.dp))
        Bubble(BubbleItem(StriveIcons.Plus, "Empty") {}, Modifier.bubbleAt(150.dp, 108.dp))
        Bubble(BubbleItem(StriveIcons.List, "From template") {}, Modifier.bubbleAt(240.dp, 108.dp))
        Bubble(BubbleItem(StriveIcons.Run, "Run") {}, Modifier.bubbleAt(310.dp, 160.dp))
        TabBar(
            before = listOf(TabItem(StriveIcons.Home, "Home", true) {}, TabItem(StriveIcons.Food, "Food", false) {}),
            after = listOf(TabItem(StriveIcons.Plan, "Plan", false) {}, TabItem(StriveIcons.You, "You", false) {}),
            startLabel = "Close Start", startOpen = true, onStart = {},
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )
    }
}

/** A bubble by its circle's centre, as `.bub`'s left, top and −28 margins place it; the column centres the circle over its label. */
private fun Modifier.bubbleAt(x: Dp, y: Dp) = layout { m, c ->
    val p = m.measure(c.copy(minWidth = 0, minHeight = 0))
    val half = 28.dp.roundToPx()
    layout(p.width, p.height) { p.place(x.roundToPx() - ((p.width - 2 * half) / 2f).roundToInt() - half, y.roundToPx() - half) }
}

/**
 * The Menus card: its 390 x 400 box (border-box, as the TabBar card's) with the exercise menu open at top 16, right 16: anchored
 * to a point 6 dp above that corner, so the card is also the menu's own placement.
 */
@Composable
internal fun MenusBoard() {
    val colors = MaterialTheme.colorScheme
    val corner = RoundedCornerShape(16.dp)
    Box(
        Modifier.padding(top = 16.dp).fillMaxWidth().height(400.dp).clip(corner).background(colors.background)
            .border(1.dp, colors.outlineVariant, corner).padding(1.dp),
    ) {
        Box(Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 16.dp)) {
            GlassMenu(expanded = true, onDismiss = {}) {
                MenuTiles(MenuTile(StriveIcons.Note, "Note") {}, MenuTile(StriveIcons.Swap, "Swap") {})
                MenuRow("Exercise details", {}, icon = StriveIcons.Info)
                MenuRow("Sticky note", {}, icon = StriveIcons.Pin)
                MenuRow("Rest for all sets", {}, tail = "\u203a")
                MenuRow("Create superset", {})
                MenuRow("Order and skip", {})
                MenuRow("Exercise settings", {})
                MenuRow("Remove exercise", {}, icon = StriveIcons.Trash, danger = true)
            }
        }
    }
}

/**
 * P5's probe: a menu, in its popup, open over 20 lines of text marked to blur; the lines behind the panel must come out blurred.
 * The source paints its own background, as a screen's must: haze blurs only what the source draws, and over a transparent one
 * the sharp text under it shows through the tint.
 */
@Composable
internal fun MenuOverTextBoard() {
    val haze = rememberHazeState()
    CompositionLocalProvider(LocalGlassSource provides haze) {
        Box(Modifier.fillMaxWidth()) {
            LinesToBlur(haze, 20)
            Box(Modifier.align(Alignment.TopEnd).padding(top = 60.dp, end = 16.dp)) {
                GlassMenu(expanded = true, onDismiss = {}) {
                    MenuRow("Rename", {})
                    MenuRow("Session note", {})
                    MenuRow("Change start time", {})
                }
            }
        }
    }
}

/** [lines] lines of text marked to blur, on a source that paints its own background as a screen's must. */
@Composable
private fun LinesToBlur(haze: HazeState, lines: Int) =
    Column(Modifier.fillMaxWidth().hazeSource(haze).background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        repeat(lines) {
            Text("Line ${it + 1}: the quick brown fox jumps over the lazy dog", style = MaterialTheme.typography.bodyLarge, maxLines = 1, softWrap = false)
        }
    }

/** [MenuOverTextBoard]'s twin for a dialog: an empty alert panel, in its Dialog window, over 36 lines of text marked to blur. */
@Composable
internal fun DialogOverTextBoard() {
    val haze = rememberHazeState()
    CompositionLocalProvider(LocalGlassSource provides haze) {
        LinesToBlur(haze, 36)
        AlertBox(onDismiss = {}) { Spacer(Modifier.height(120.dp)) }
    }
}

/**
 * The Dialogs card: its two 390 x 420 boxes (border-box, as the TabBar card's), stacked as they wrap at 390, each over the scrim:
 * the alert in `.center`, and the action sheet 8 dp from the sides and 8 dp above the card's 34 dp safe area. The panels are drawn
 * in place; in the app each opens in its own window.
 */
@Composable
// Unbounded: the second box runs past the 844 pt screen, as the card's does in Chrome, instead of shrinking to fit.
internal fun DialogsBoard() = Column(Modifier.wrapContentHeight(Alignment.Top, unbounded = true).padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    DialogCardBox {
        AlertPanel("Discard the workout?", "Every set in it is deleted.", "Discard", {}, {}, modifier = Modifier.align(Alignment.Center).padding(24.dp))
    }
    DialogCardBox {
        ActionSheetPanel(
            "2 sets not logged",
            listOf(SheetAction("Log them as they are and save") {}, SheetAction("Save without them") {}, SheetAction("Discard workout", destructive = true) {}),
            "Continue workout", {}, Modifier.align(Alignment.BottomCenter).padding(start = 8.dp, end = 8.dp, bottom = 8.dp + 34.dp),
        )
    }
}

@Composable
private fun DialogCardBox(content: @Composable BoxScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    val corner = RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxWidth().height(420.dp).clip(corner).background(colors.background).border(1.dp, colors.outlineVariant, corner).padding(1.dp)) {
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = StriveTheme.states.scrimOpacity)))
        content()
    }
}
