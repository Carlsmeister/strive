package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveMoments
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.celebrate
import se.carlsmeister.strive.theme.celebrateFamily

/**
 * Sun marks a record, flame a streak, lime done. The moment layer's rule (moments.css): an accent marks an edge, an icon or a
 * number on design 1's surface roles. It never fills a card. Dark and light add a faint wash under the edge; the -hc modes never do.
 */
internal enum class Accent { Sun, Flame, Lime }

/** `--m`: the edge and the icon. */
internal fun StriveMoments.edge(a: Accent): Color = when (a) { Accent.Sun -> sun; Accent.Flame -> flame; Accent.Lime -> lime }

/** `--m-txt`: numbers and words. */
internal fun StriveMoments.text(a: Accent): Color = when (a) { Accent.Sun -> sunText; Accent.Flame -> flameText; Accent.Lime -> limeText }

/** `--m-w`: the bright hue a tint is mixed from. */
internal fun StriveMoments.wash(a: Accent): Color = when (a) { Accent.Sun -> sunWash; Accent.Flame -> flameWash; Accent.Lime -> limeWash }

/** CSS `color-mix(in srgb, top fraction, base)`: each sRGB channel mixed linearly, unlike Compose's `lerp`, which mixes in Oklab. */
internal fun srgbMix(top: Color, base: Color, fraction: Float): Color {
    fun mix(t: Float, b: Float) = t * fraction + b * (1 - fraction)
    return Color(mix(top.red, base.red), mix(top.green, base.green), mix(top.blue, base.blue), mix(top.alpha, base.alpha))
}

/** `.moment`'s background: the wash at the tint over [base], which is [base] itself when the tint is 0, as in the -hc modes. */
internal fun momentFill(m: StriveMoments, a: Accent, base: Color): Color = srgbMix(m.wash(a), base, m.tint)

/** `.moment` and `.moment.on-high`: the fill over `surface` (`surfaceContainerHigh` when [onHigh]), then an inset edge that adds no size. */
@Composable
internal fun Modifier.moment(accent: Accent, shape: Shape, onHigh: Boolean = false): Modifier {
    val m = StriveTheme.moments
    val colors = MaterialTheme.colorScheme
    val base = if (onHigh) colors.surfaceContainerHigh else colors.surface
    return background(momentFill(m, accent, base), shape).border(m.edge, m.edge(accent), shape)
}

/** `.m-tag`: a capsule label with a 1 dp accent edge and accent text, never a fill; the icon is the boards' `.ic.s14`. */
@Composable
internal fun MomentTag(text: String, accent: Accent, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val m = StriveTheme.moments
    Row(
        modifier.heightIn(min = 24.dp).border(1.dp, m.edge(accent), ShapeFull).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, Modifier.size(14.dp), tint = m.edge(accent))
        // `.m-tag`'s `font:` shorthand resets font-variant-numeric, so its figures are proportional, unlike labelMedium's.
        Text(text, color = m.text(accent), style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = null),
            fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
    }
}

/**
 * `.m-num`, with `.celebrate` when asked. Its own Text: `insideValue` sets the line height, which a span would drop.
 * [style] must be the host's full type style with a font size: `.celebrate` scales it to .88, and an unspecified size throws.
 */
@Composable
internal fun MomentNumber(
    text: String,
    accent: Accent,
    style: TextStyle,
    modifier: Modifier = Modifier,
    celebrate: Boolean = false,
    insideValue: Boolean = false,
) {
    val shown = if (celebrate) style.celebrate(celebrateFamily(), insideValue) else style
    Text(text, modifier, color = StriveTheme.moments.text(accent), style = shown)
}
