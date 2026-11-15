package se.carlsmeister.strive.ui

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import kotlin.math.roundToInt

/** `.btn.filled` is a view's one primary action; `.btn` (Tinted) everything else; `.btn.danger` names what goes; `.btn.text` is bare. */
internal enum class ActionKind { Filled, Tinted, Danger, Text }

/**
 * `.btn` (#16 B3): rounded and wide, a capsule when [small]. It never sets its own width: a caller fills the row, as `.btn`'s
 * 100 % does, or sets it in a [ButtonRow], as `.btnrow` does. Disabled is `.btn.dis`: faded and not tappable.
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
    val inset = if (bare) space.s8 else space.s16
    Box(
        modifier.then(FlexBasis(inset * 2)).heightIn(min = if (bare) StriveTheme.sizes.touch else if (small) 36.dp else 50.dp)
            .alpha(if (enabled) 1f else StriveTheme.states.disabledOpacity)
            .pressable(enabled, onClick)
            .clip(if (small) ShapeFull else MaterialTheme.shapes.large)
            .background(container)
            .padding(if (bare) PaddingValues(inset) else PaddingValues(horizontal = inset)),
        contentAlignment = Alignment.Center,
    ) {
        val style = if (bare) type.bodyLarge else if (small) type.titleSmall else type.labelLarge
        // `.btn`, `.btn.small` and `.btn.text` set `font:`, which resets `.strive`'s tabular-nums.
        Text(text, color = content, style = style.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox),
            textAlign = TextAlign.Center, softWrap = !small)
    }
}

/**
 * A button's flex basis: its own horizontal padding, where a border-box `flex: 1` starts. Parent data, so only a [ButtonRow] reads
 * it; in any other layout the button measures as it would without it.
 */
private data class FlexBasis(val width: Dp) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@FlexBasis
}

/**
 * `.btnrow`: buttons side by side 8 dp apart, as tall as the tallest, sized as CSS's `flex` sizes them. Each starts from its
 * padding and takes its [weights] share (default 1, the card's `flex: 1`; 2 is the boards' `flex: 2`) of the space left, never
 * narrower than its label.
 */
@Composable
internal fun ButtonRow(modifier: Modifier = Modifier, weights: List<Float>? = null, content: @Composable () -> Unit) {
    val gap = StriveTheme.spacing.s8
    Layout(content, modifier) { buttons, constraints ->
        val gapPx = gap.toPx()
        val mins = buttons.map { it.minIntrinsicWidth(constraints.maxHeight).toFloat() }
        val row = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() else mins.sum() + gapPx * (buttons.size - 1)
        val widths = flexWidths(
            row, gapPx, buttons.map { (it.parentData as? FlexBasis)?.width?.toPx() ?: 0f },
            buttons.indices.map { weights?.getOrNull(it) ?: 1f }, mins,
        )
        // Round the edges, not the widths, so the row's pixels add up exactly.
        val starts = widths.runningFold(0f) { x, w -> x + w + gapPx }
        val sizes = widths.indices.map { (starts[it] + widths[it]).roundToInt() - starts[it].roundToInt() }
        val height = buttons.indices.maxOfOrNull { buttons[it].maxIntrinsicHeight(sizes[it]) }?.coerceIn(constraints.minHeight, constraints.maxHeight)
            ?: constraints.minHeight
        val placed = buttons.indices.map { buttons[it].measure(Constraints.fixed(sizes[it], height)) }
        layout(row.roundToInt(), height) { placed.forEachIndexed { i, p -> p.place(starts[i].roundToInt(), 0) } }
    }
}

// shortcut: grow only, as `.btnrow` uses it; rows wider than their space overflow instead of shrinking. Add flex-shrink when one must.
/**
 * CSS's flex resolution for a growing row: every item gets its [bases] width plus its [grows] share of the free space; an item
 * that lands under its [mins] width is frozen there and the rest share again, until none does.
 */
internal fun flexWidths(row: Float, gap: Float, bases: List<Float>, grows: List<Float>, mins: List<Float>): List<Float> {
    val frozen = MutableList<Float?>(bases.size) { null }
    while (true) {
        val open = bases.indices.filter { frozen[it] == null }
        val free = row - gap * (bases.size - 1) - frozen.sumOf { (it ?: 0f).toDouble() }.toFloat() - open.sumOf { bases[it].toDouble() }.toFloat()
        val grow = open.sumOf { grows[it].toDouble() }.toFloat()
        val widths = bases.indices.map { frozen[it] ?: (bases[it] + if (grow > 0f) free * grows[it] / grow else 0f) }
        val under = open.filter { widths[it] < mins[it] }
        if (under.isEmpty()) return widths
        under.forEach { frozen[it] = mins[it] }
    }
}

/**
 * The Buttons README's press, in place of the ripple: scale .96 and dim while held, 100 ms down, the release curve back up.
 * Reduce Motion keeps the dim and drops the scale. A disabled control never reports a press, so it does not react.
 */
@Composable
internal fun Modifier.pressable(enabled: Boolean, onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val states = StriveTheme.states
    val spec: TweenSpec<Float> = if (pressed) Motion.press() else Motion.release()
    val scale = animateFloatAsState(if (pressed && !StriveTheme.reduceMotion) states.pressedScale else 1f, spec)
    val dim = animateFloatAsState(if (pressed) states.pressedOpacity else 1f, spec)
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        alpha = dim.value.coerceAtMost(1f) // the release curve overshoots
    }.clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
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
        modifier.size(if (small) 32.dp else StriveTheme.sizes.touch).pressable(true, onClick).clip(ShapeFull).background(container),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, contentDescription = label, Modifier.size(iconSize), tint = content) }
}

/** The ⋯ pill (D): deep teal in every mode, never a near-white tonal fill. [small] is the title bar's 32 × 24. */
@Composable
internal fun MorePill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, small: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val size = if (small) 14.sp else 17.sp
    val (width, height) = if (small) 32.dp to 24.dp else 40.dp to 32.dp
    // Taps land across the touch size centred on the pill; the extra hangs over its neighbours, so what it draws stays its layout.
    Box(
        modifier.size(width, height).requiredSize(StriveTheme.sizes.touch).pressable(true, onClick).wrapContentSize().size(width, height)
            .clip(ShapeFull).background(colors.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        // `700 17px/1`: the line box is the font size.
        val style = TextStyle(fontWeight = FontWeight(700), fontSize = size, lineHeight = size, letterSpacing = 1.sp, lineHeightStyle = CssLineBox)
        Text("⋯", Modifier.clearAndSetSemantics { contentDescription = label }, color = colors.onPrimaryContainer, style = style)
    }
}
