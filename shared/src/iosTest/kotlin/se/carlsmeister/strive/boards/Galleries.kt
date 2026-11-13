package se.carlsmeister.strive.boards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.Accent
import se.carlsmeister.strive.ui.MomentNumber
import se.carlsmeister.strive.ui.MomentTag
import se.carlsmeister.strive.ui.edge
import se.carlsmeister.strive.ui.moment

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
