package se.carlsmeister.strive.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/** Frosted glass: what is marked `hazeSource(state)` behind this element shows through, blurred and tinted. [thick] for sheets and docks. */
internal fun Modifier.glass(state: HazeState, materials: StriveMaterials, thick: Boolean = false): Modifier {
    val tint = if (thick) materials.thick else materials.regular
    // Reduce Transparency makes the fill solid, so there is nothing to blur.
    return if (tint.alpha == 1f) background(tint)
    else hazeBlur(
        HazeInput.Sources(state),
        HazeBlurStyle {
            blurRadius(MaterialBlur)
            colorEffects(listOf(HazeColorEffect.tint(tint)))
        },
    )
}
