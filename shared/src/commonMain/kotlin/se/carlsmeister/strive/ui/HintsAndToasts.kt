package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme

/**
 * `.toast`: a capsule in inverse-surface confirming what just happened, on one line; an [icon] first (16 dp, in the text's colour
 * unless [iconTint]) and an [action] last, tapped across a touch while the capsule keeps its height. Only the toast itself: no host,
 * queue or timer until a screen shows one.
 */
@Composable
internal fun Toast(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = Color.Unspecified,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    // `.toast` sets `font:`, which resets `.strive`'s tabular-nums.
    val style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox)
    Row(
        modifier.inverseBox(ShapeFull).semantics { liveRegion = LiveRegionMode.Polite }.padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = iconTint.takeOrElse { colors.inverseOnSurface })
        Text(text, color = colors.inverseOnSurface, style = style, maxLines = 1, softWrap = false)
        if (action != null) Box(Modifier.touchTall().pressable(true, onAction), contentAlignment = Alignment.Center) {
            Text(action, color = colors.inversePrimary, style = style, maxLines = 1, softWrap = false)
        }
    }
}

/**
 * `.hint`: a first-use hint in inverse-surface, its [title], [text] and a [dismiss] label, with an arrow toward its control: under
 * the box, or over it when [arrowUp], [arrowAt] of the way across. At most 270 dp wide, else as wide as its text.
 */
@Composable
internal fun Hint(
    title: String,
    text: String,
    dismiss: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    arrowUp: Boolean = false,
    arrowAt: Float = .5f,
) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    // `.hint b`, `p` and `.got` each set `font:`, which resets `.strive`'s tabular-nums.
    val titleStyle = type.titleSmall.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox)
    Column(
        modifier.widthIn(max = 270.dp).inverseBox(MaterialTheme.shapes.medium)
            // `::after`: a 14 dp square turned 45°, 6 dp past the edge (its middle 1 dp inside it), in the box's fill and with no shadow.
            .drawBehind {
                val side = 14.dp.toPx()
                val middle = Offset(size.width * arrowAt, if (arrowUp) 1.dp.toPx() else size.height - 1.dp.toPx())
                rotate(45f, middle) { drawRect(colors.inverseSurface, middle - Offset(side / 2, side / 2), Size(side, side)) }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = colors.inverseOnSurface, style = titleStyle)
        Text(text, color = colors.inverseOnSurface, style = type.bodySmall.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
        Box(
            Modifier.align(Alignment.End).touchTall().pressable(true, onDismiss).heightIn(min = 32.dp).padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) { Text(dismiss, color = colors.inversePrimary, style = titleStyle, maxLines = 1, softWrap = false) }
    }
}

/** The inverse-surface fill under `.glass`'s shadow, `0 8px 24px` in the material's shadow colour; opaque, so none shows through. */
@Composable
private fun Modifier.inverseBox(shape: Shape) =
    dropShadow(shape, Shadow(24.dp, StriveTheme.materials.shadow, offset = DpOffset(0.dp, 8.dp))).background(MaterialTheme.colorScheme.inverseSurface, shape)
