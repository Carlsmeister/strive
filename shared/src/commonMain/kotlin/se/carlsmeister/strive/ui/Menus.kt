package se.carlsmeister.strive.ui

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.LocalGlassSource
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass

/**
 * `.menu` (the Menus card, choice 4): a 264 dp panel of thick glass under the button that opened it, 6 dp below with the right
 * edges aligned; above the button if it would pass the bottom of the window, and 8 dp inside the window's sides. Call it inside
 * the Box that holds its button. It fades in and out, scaling from .9 at its anchored corner unless Reduce Motion; a tap outside
 * or back closes it. [content] is [MenuRow]s, [MenuTiles], [MenuTitle] and [MenuSeparator].
 *
 * A popup (P5): its glass blurs the screen behind it as in-tree glass does, provided that screen's haze source paints its own
 * background. A source without one leaves its text sharp behind every glass, in a popup or not.
 */
@Composable
internal fun GlassMenu(expanded: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shown = remember { MutableTransitionState(false) }
    shown.targetState = expanded
    if (!shown.currentState && !shown.targetState) return
    val density = LocalDensity.current
    var origin by remember { mutableStateOf(TransformOrigin(1f, 0f)) }
    val placement = remember(density) { with(density) { MenuPlacement(6.dp.roundToPx(), 8.dp.roundToPx()) { origin = it } } }
    val reduceMotion = StriveTheme.reduceMotion
    Popup(popupPositionProvider = placement, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        val progress by rememberTransition(shown).animateFloat({ Motion.fade() }) { if (it) 1f else 0f }
        // The corner is read as the layer draws, as Material's menu does: placement chooses it after composition, so a transition
        // built in composition scaled a menu that opens above from its top corner on its first frame.
        MenuPanel(modifier.graphicsLayer {
            alpha = progress
            val scale = if (reduceMotion) 1f else .9f + .1f * progress
            scaleX = scale
            scaleY = scale
            transformOrigin = origin
        }, content)
    }
}

/** Where [GlassMenu] opens, 6 dp ([gap]) from its button and [margin] inside the window's sides; tells [onOrigin] the corner it grows from. */
private class MenuPlacement(private val gap: Int, private val margin: Int, private val onOrigin: (TransformOrigin) -> Unit) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val rtl = layoutDirection == LayoutDirection.Rtl
        val aligned = if (rtl) anchorBounds.left else anchorBounds.right - popupContentSize.width
        val x = aligned.coerceIn(margin, maxOf(margin, windowSize.width - margin - popupContentSize.width))
        val above = anchorBounds.bottom + gap + popupContentSize.height > windowSize.height
        val y = if (above) anchorBounds.top - gap - popupContentSize.height else anchorBounds.bottom + gap
        onOrigin(TransformOrigin(if (rtl) 0f else 1f, if (above) 1f else 0f))
        return IntOffset(x, y.coerceIn(0, maxOf(0, windowSize.height - popupContentSize.height)))
    }
}

/**
 * The panel itself, border-box as CSS: the hairline edge takes its room inside the 264 dp. Children stack as `display: grid` does;
 * `.mrow + .mrow`'s hairline shows only between two rows, so each row brings its own line and the panel lays out only those that
 * follow a row.
 */
// shortcut: no max height and no scroll; the tallest menu (tiles and 7 rows, about 430 dp) fits a portrait phone, so large text or
// landscape clips it; cap it at the window and scroll once a menu or a text size outgrows that.
@Composable
private fun MenuPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val edge = StriveTheme.borders.hairline
    Layout(
        content,
        modifier.testTag("glass-menu").width(264.dp)
            .glass(LocalGlassSource.current, StriveTheme.glass, thick = true, shape = MaterialTheme.shapes.large).padding(edge),
    ) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0, minWidth = constraints.maxWidth)
        var afterRow = false
        val placeables = measurables.mapNotNull { m ->
            val line = m.layoutId == LINE
            val keep = !line || afterRow
            afterRow = m.layoutId == ROW
            if (keep) m.measure(loose) else null
        }
        layout(constraints.maxWidth, placeables.sumOf { it.height }) {
            var y = 0
            for (p in placeables) { p.place(0, y); y += p.height }
        }
    }
}

private const val ROW = "menu-row"
private const val LINE = "menu-line"

/** A `.mrow` text sets `font:`, which resets `.strive`'s tabular-nums. */
private fun TextStyle.css() = copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox)

/**
 * `.mrow`: [text] left, a 20 dp [icon] or a [tail] right. [danger] is `.mrow.danger`, in error. A non-null [checked] adds `.lead`'s
 * 20 dp column holding `Check` in primary when true; [back] is a submenu's back row, in label-large with `Back` in that column.
 * It presses as a list row does: surface-container-highest, no scale.
 */
@Composable
internal fun MenuRow(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    tail: String? = null,
    tailColor: Color = Color.Unspecified,
    danger: Boolean = false,
    checked: Boolean? = null,
    back: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val ink = if (danger) colors.error else colors.onSurface
    val lead = checked != null || back
    // A border is whole device pixels, as Chrome draws the board's: 0.5 dp is 1 px at 3x, where a 0.5 dp layout rounds to 2.
    val line = with(LocalDensity.current) { StriveTheme.borders.hairline.toPx().toInt().coerceAtLeast(1).toDp() }
    Box(Modifier.layoutId(LINE).fillMaxWidth().height(line).background(colors.outlineVariant))
    Row(
        Modifier.layoutId(ROW).fillMaxWidth()
            .clickable(source, indication = null, role = Role.Button, onClick = onClick)
            .then(if (checked != null) Modifier.semantics { selected = checked } else Modifier)
            .background(if (pressed) colors.surfaceContainerHighest else Color.Transparent)
            .heightIn(min = StriveTheme.sizes.touch).padding(start = if (lead) space.s12 else space.s16, end = space.s16, top = 11.dp, bottom = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(space.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (lead) {
            Box(Modifier.size(20.dp)) {
                when {
                    back -> Icon(StriveIcons.Back, null, Modifier.size(20.dp), tint = ink)
                    checked == true -> Icon(StriveIcons.Check, null, Modifier.size(20.dp), tint = colors.primary)
                }
            }
        }
        Text(text, Modifier.weight(1f), color = ink, style = (if (back) type.labelLarge else type.bodyLarge).css())
        icon?.let { Icon(it, null, Modifier.size(20.dp), tint = ink) }
        tail?.let {
            Text(it, color = if (tailColor == Color.Unspecified) colors.onSurfaceVariant else tailColor, style = type.bodyMedium.css(),
                maxLines = 1, softWrap = false)
        }
    }
}

/** `.mtitle`: a section's title in label-medium, on-surface-variant. */
@Composable
internal fun MenuTitle(text: String) = Text(
    text, Modifier.fillMaxWidth().padding(start = StriveTheme.spacing.s16, top = StriveTheme.spacing.s8, end = StriveTheme.spacing.s16),
    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium.css(),
)

/** `.msep`: 8 dp of black at 12 % between sections. */
@Composable
internal fun MenuSeparator() = Box(Modifier.fillMaxWidth().height(8.dp).background(Color.Black.copy(alpha = .12f)))

/** One `.tile` of [MenuTiles]. */
internal class MenuTile(val icon: ImageVector, val label: String, val onClick: () -> Unit)

/** `.tiles`: M2's two palette tiles on top of the exercise menu, equal and 8 dp apart, with 8 dp round them. */
@Composable
internal fun MenuTiles(first: MenuTile, second: MenuTile) =
    Row(Modifier.fillMaxWidth().padding(StriveTheme.spacing.s8), horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        for (tile in listOf(first, second)) {
            Column(
                Modifier.weight(1f).pressable(true, tile.onClick)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium)
                    .padding(horizontal = StriveTheme.spacing.s12, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4),
            ) {
                Icon(tile.icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                Text(tile.label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium.css())
            }
        }
    }
