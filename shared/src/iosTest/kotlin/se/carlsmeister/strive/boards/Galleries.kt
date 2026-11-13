package se.carlsmeister.strive.boards

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * scrolled on glass with every set logged, named and counted from the fixture's open Push A (`1/14`, where the card draws `2/15`: R5).
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
