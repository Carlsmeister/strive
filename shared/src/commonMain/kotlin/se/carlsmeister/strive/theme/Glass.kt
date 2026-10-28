package se.carlsmeister.strive.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/** Frosted glass: what is marked `hazeSource(state)` behind this element shows through, blurred and tinted. */
internal fun Modifier.glass(state: HazeState, materials: StriveMaterials): Modifier =
    // Reduce Transparency makes the fill solid, so there is nothing to blur.
    if (materials.regular.alpha == 1f) background(materials.regular)
    else hazeBlur(
        HazeInput.Sources(state),
        HazeBlurStyle {
            blurRadius(MaterialBlur)
            colorEffects(listOf(HazeColorEffect.tint(materials.regular)))
        },
    )
