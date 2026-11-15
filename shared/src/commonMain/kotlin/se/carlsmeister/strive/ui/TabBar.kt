package se.carlsmeister.strive.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import se.carlsmeister.strive.theme.Motion
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import se.carlsmeister.strive.workout.clock
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
 * [startLabel] is what a screen reader calls Start; [startOpen] turns its plus 45° to ×.
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
                // MO-start: opening turns it with the panel, closing returns it with the bubbles; under Reduce Motion it is × at once.
                val spec: FiniteAnimationSpec<Float> = when {
                    StriveTheme.reduceMotion -> snap()
                    startOpen -> startOpening()
                    else -> Motion.fade()
                }
                val turn by animateFloatAsState(if (startOpen) 45f else 0f, spec)
                Icon(StriveIcons.Plus, null, Modifier.size(28.dp).rotate(turn), tint = colors.onPrimary)
            }
        }
        after.forEach { Tab(it) }
    }
}

/** A rest running in the minimised workout: [left] of [of] seconds, drawn as a ring [size] across with a [stroke] wide arc (P4). */
internal class RestRing(val left: Int, val of: Int, val size: Dp = 26.dp, val stroke: Dp = 3.25.dp)

/**
 * `.minibar` (WO-17): the minimised workout as a capsule of regular glass, [title] over [sub], each one line; while a [rest] runs,
 * its ring and time at the end in tertiary. No chevron (P3). [clickLabel] is what a screen reader says a tap does.
 */
@Composable
internal fun WorkoutMiniBar(
    title: String,
    sub: String,
    clickLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rest: RestRing? = null,
) = FixedTextSize {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val edge = StriveTheme.borders.hairline
    // Each of `.minibar`'s texts sets `font:`, which resets `.strive`'s tabular-nums.
    fun TextStyle.css() = copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox)
    Row(
        // Border-box, as the tab bar: the hairline edge takes its room inside the capsule, before the padding.
        modifier.fillMaxWidth().pressable(true, onClick, label = clickLabel).heightIn(min = 52.dp)
            .glass(LocalGlassSource.current, StriveTheme.glass, shape = ShapeFull)
            .padding(start = StriveTheme.spacing.s20 + edge, top = StriveTheme.spacing.s8 + edge, end = StriveTheme.spacing.s12 + edge, bottom = StriveTheme.spacing.s8 + edge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.onSurface, style = type.titleSmall.css(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, color = colors.onSurfaceVariant, style = type.labelMedium.css(), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        rest?.let { r ->
            Row(Modifier.testTag("bar-rest"), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Canvas(Modifier.size(r.size)) {
                    // The card's 64-unit ring: radius 23.5, the arc from 12 o'clock clockwise over what is left.
                    val radius = size.width * 23.5f / 64
                    val corner = Offset(center.x - radius, center.y - radius)
                    val box = Size(2 * radius, 2 * radius)
                    drawArc(colors.surfaceContainerHighest, 0f, 360f, false, corner, box, style = Stroke(r.stroke.toPx()))
                    drawArc(colors.tertiary, -90f, 360f * r.left / r.of, false, corner, box, style = Stroke(r.stroke.toPx(), cap = StrokeCap.Round))
                }
                Text(clock(r.left.toLong()), color = colors.tertiary, style = type.titleSmall.css(), maxLines = 1, softWrap = false)
            }
        }
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
