package se.carlsmeister.strive.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/** The haze state of the screen a glass overlay sits on; null where nothing is marked to blur. */
internal val LocalGlassSource = staticCompositionLocalOf<HazeState?> { null }

/**
 * Frosted glass: what is marked `hazeSource(state)` behind this element shows through, blurred and tinted. [thick] for sheets and docks.
 * A null [state] tints without blurring. With a [shape], `.glass` whole: its shadow outside the shape, the fill clipped to it, and its hairline edge.
 */
internal fun Modifier.glass(state: HazeState?, materials: StriveMaterials, thick: Boolean = false, shape: Shape? = null): Modifier {
    val tint = if (thick) materials.thick else materials.regular
    // `.glass`'s box-shadow: 0 8px 24px in the material's shadow colour.
    val framed = if (shape == null) this
    else outerShadow(shape, Shadow(24.dp, materials.shadow, offset = DpOffset(0.dp, 8.dp))).clip(shape)
    // Reduce Transparency makes the fill solid, so there is nothing to blur.
    val filled = if (state == null || tint.alpha == 1f) framed.background(tint)
    else framed.hazeBlur(
        HazeInput.Sources(state),
        HazeBlurStyle {
            blurRadius(MaterialBlur)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
        },
    )
    return if (shape == null) filled else filled.border(StriveTheme.borders.hairline, materials.edge, shape)
}

/** A box-shadow as CSS paints it, only outside [shape]: Compose's `dropShadow` also paints under it, darkening translucent glass. */
private fun Modifier.outerShadow(shape: Shape, shadow: Shadow): Modifier = composed {
    val context = LocalGraphicsContext.current
    val painter = remember(shape, shadow) { context.shadowContext.createDropShadowPainter(shape, shadow) }
    drawWithCache {
        val outline = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        onDrawBehind { clipPath(outline, ClipOp.Difference) { with(painter) { draw(size) } } }
    }
}
