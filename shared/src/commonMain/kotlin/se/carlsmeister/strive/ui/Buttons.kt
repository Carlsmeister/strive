package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme

/** `.btn.filled` is a view's one primary action; `.btn` (Tinted) everything else; `.btn.danger` names what goes; `.btn.text` is bare. */
internal enum class ActionKind { Filled, Tinted, Danger, Text }

/**
 * `.btn` (#16 B3): rounded and wide, a capsule when [small]. It never sets its own width: a caller fills the row, as `.btn`'s
 * 100 % does, or gives it a weight, as `.btnrow` does. Disabled is `.btn.dis`: faded and not tappable.
 */
@Composable
internal fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ActionKind = ActionKind.Tinted,
    small: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    val (container, content) = when (kind) {
        ActionKind.Filled -> colors.primary to colors.onPrimary
        ActionKind.Tinted -> colors.surfaceContainerHigh to colors.primary
        ActionKind.Danger -> colors.surfaceContainerHigh to colors.error
        ActionKind.Text -> Color.Transparent to colors.primary
    }
    val bare = kind == ActionKind.Text // `.btn.text` comes after `.btn.small` in bundle.css: its font, padding and height win
    Box(
        modifier.heightIn(min = if (bare) StriveTheme.sizes.touch else if (small) 36.dp else 50.dp)
            .alpha(if (enabled) 1f else StriveTheme.states.disabledOpacity)
            .clip(if (small) ShapeFull else MaterialTheme.shapes.large)
            .background(container)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(if (bare) PaddingValues(space.s8) else PaddingValues(horizontal = space.s16)),
        contentAlignment = Alignment.Center,
    ) {
        val style = if (bare) type.bodyLarge else if (small) type.titleSmall else type.labelLarge
        // `.btn.small` never wraps: a label wider than its weighted share overhangs into the padding, centred, instead of clipping.
        val noWrap = if (small) Modifier.wrapContentWidth(unbounded = true) else Modifier
        Text(text, noWrap, color = content, style = style.copy(lineHeightStyle = CssLineBox), softWrap = !small)
    }
}

/** `.ibtn`: a grey disc on bars, `.plain` in lists, `.teal` without the disc, `.on` a filter in use. */
internal enum class RoundKind { Disc, Plain, Teal, On }

/** `.ibtn`: a 44 dp round target (`.sm`, 32, when [small]) around a glyph; [label] is what a screen reader says. */
@Composable
internal fun RoundButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: RoundKind = RoundKind.Disc,
    small: Boolean = false,
    iconSize: Dp = 24.dp,
) {
    val colors = MaterialTheme.colorScheme
    val (container, content) = when (kind) {
        RoundKind.Disc -> colors.surfaceContainerHigh to colors.onSurface
        RoundKind.Plain -> Color.Transparent to colors.onSurface
        RoundKind.Teal -> Color.Transparent to colors.primary
        RoundKind.On -> colors.primary to colors.onPrimary
    }
    Box(
        modifier.size(if (small) 32.dp else StriveTheme.sizes.touch).clip(ShapeFull).background(container)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = label, Modifier.size(iconSize), tint = content) }
}

/** The ⋯ pill (D): deep teal in every mode, never a near-white tonal fill. [small] is the title bar's 32 × 24. */
@Composable
internal fun MorePill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, small: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val size = if (small) 14.sp else 17.sp
    Box(
        modifier.size(if (small) 32.dp else 40.dp, if (small) 24.dp else 32.dp).clip(ShapeFull).background(colors.primaryContainer)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // `700 17px/1`: the line box is the font size.
        val style = TextStyle(fontWeight = FontWeight(700), fontSize = size, lineHeight = size, letterSpacing = 1.sp, lineHeightStyle = CssLineBox)
        Text("⋯", Modifier.clearAndSetSemantics { contentDescription = label }, color = colors.onPrimaryContainer, style = style)
    }
}
