package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.FixedTextSize
import se.carlsmeister.strive.theme.LocalGlassSource
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass

/** The bar's gap above the screen's edge (P9): 22 dp over a home indicator, 8 dp above a taller system bar. */
internal fun barBottom(inset: Dp) = if (inset <= 34.dp) 22.dp else inset + 8.dp

/** One `.tab`: its icon, its label, and whether it is the current one (`aria-current`). */
internal class TabItem(val icon: ImageVector, val label: String, val selected: Boolean, val onClick: () -> Unit)

/**
 * `.tabbar` (SH-01): a capsule of regular glass, [before] and [after] either side of Start's teal disc, in five equal columns.
 * [startLabel] is what a screen reader calls Start; [startOpen] turns its plus 45°.
 */
@Composable
internal fun TabBar(
    before: List<TabItem>,
    after: List<TabItem>,
    startLabel: String,
    startOpen: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) = FixedTextSize {
    val colors = MaterialTheme.colorScheme
    Row(
        // Border-box, as CSS: the hairline edge takes its room inside the capsule, before the 4 dp padding.
        modifier.fillMaxWidth().glass(LocalGlassSource.current, StriveTheme.glass, shape = ShapeFull)
            .padding(StriveTheme.spacing.s4 + StriveTheme.borders.hairline).selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        before.forEach { Tab(it) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(50.dp).pressable(true, onStart).semantics { contentDescription = startLabel }
                    .background(colors.primary, ShapeFull),
                contentAlignment = Alignment.Center,
            ) {
                Icon(StriveIcons.Plus, null, Modifier.size(28.dp).rotate(if (startOpen) 45f else 0f), tint = colors.onPrimary)
            }
        }
        after.forEach { Tab(it) }
    }
}

/** `.tab`: a 24 dp icon over its label-small label; the current one in primary, weight 600, on a capsule of thick material. */
@Composable
private fun RowScope.Tab(item: TabItem) {
    val colors = MaterialTheme.colorScheme
    val color = if (item.selected) colors.primary else colors.onSurfaceVariant
    // `materials`, not `glass`: under Reduce Transparency the capsule stays the thick tint on the solid bar (SH-01-rt).
    val fill = if (item.selected) StriveTheme.materials.thick else Color.Transparent
    Column(
        Modifier.weight(1f).heightIn(min = 52.dp).pressable(true, item.onClick, Role.Tab).semantics { selected = item.selected }
            .clip(ShapeFull).background(fill).padding(start = 2.dp, top = 6.dp, end = 2.dp, bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(item.icon, null, Modifier.size(StriveTheme.sizes.icon), tint = color)
        val type = MaterialTheme.typography.labelSmall
        // `.tab` sets `font:`, which resets `.strive`'s tabular-nums.
        val style = type.copy(fontWeight = if (item.selected) FontWeight(600) else type.fontWeight, fontFeatureSettings = null, lineHeightStyle = CssLineBox)
        Text(item.label, color = color, style = style, maxLines = 1, softWrap = false)
    }
}
