package se.carlsmeister.strive.theme

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

internal enum class Scheme(val colors: ColorScheme, val materials: StriveMaterials) {
    Light(lightColors, lightMaterials),
    Dark(darkColors, darkMaterials),
    LightHc(lightHcColors, lightHcMaterials),
    DarkHc(darkHcColors, darkHcMaterials),
}

internal fun scheme(dark: Boolean, highContrast: Boolean) = when {
    dark && highContrast -> Scheme.DarkHc
    dark -> Scheme.Dark
    highContrast -> Scheme.LightHc
    else -> Scheme.Light
}

// Reduce Transparency: the solid surface instead of a translucent fill; edge and shadow stay.
internal fun glass(scheme: Scheme, reduceTransparency: Boolean): StriveMaterials {
    if (!reduceTransparency) return scheme.materials
    val solid = scheme.colors.surface
    return StriveMaterials(regular = solid, thick = solid, edge = scheme.materials.edge, shadow = scheme.materials.shadow)
}

/** The phone's settings the theme follows beside light and dark. */
internal data class Accessibility(
    val highContrast: Boolean = false,
    val reduceTransparency: Boolean = false,
    val reduceMotion: Boolean = false,
)

/** The current settings, recomposing when one changes. */
@Composable
internal expect fun accessibility(): Accessibility

/** Text that grows with the phone's text size setting, up to its largest accessibility size. */
@Composable
internal expect fun ProvideTextScale(content: @Composable () -> Unit)

/** Text that keeps its standard size whatever the phone's text size setting, as iOS's own tab bar does (#30). */
@Composable
internal fun FixedTextSize(content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density), content = content)

private val LocalGlass = staticCompositionLocalOf { lightMaterials }

/** Light or dark regardless of the phone, for the tests that look at both; null follows the phone. */
internal val LocalDarkTheme = staticCompositionLocalOf<Boolean?> { null }
private val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
internal fun StriveTheme(content: @Composable () -> Unit) {
    val settings = accessibility()
    val scheme = scheme(LocalDarkTheme.current ?: isSystemInDarkTheme(), settings.highContrast)
    CompositionLocalProvider(
        LocalGlass provides glass(scheme, settings.reduceTransparency),
        LocalReduceMotion provides settings.reduceMotion,
    ) {
        MaterialTheme(colorScheme = scheme.colors, shapes = StriveShapes, typography = StriveTypography) {
            // Every text that names no colour reads on the background; without this, anything outside a Scaffold (the workout,
            // the picker, the dock, dialogs) fell back to black, which is invisible in dark mode.
            CompositionLocalProvider(LocalContentColor provides scheme.colors.onBackground) { ProvideTextScale(content) }
        }
    }
}

/** Strive's own groups beside MaterialTheme's colours, type and shapes. */
internal object StriveTheme {
    val spacing = Spacing()
    val sizes = Sizes()
    val borders = Borders()
    val states = States()
    val glass: StriveMaterials @Composable get() = LocalGlass.current
    /** Reduce Motion is on: what an animation does instead is the animating component's call. */
    val reduceMotion: Boolean @Composable get() = LocalReduceMotion.current
}
/**
 * Critically damped, response about 0.3 s: apple-motion's default for anything that moves into place. Not a token, since the
 * CSS prototypes have no springs, so it lives here rather than in the generated Tokens.kt.
 */
internal fun <T> Motion.settle(visibilityThreshold: T? = null): SpringSpec<T> =
    spring(dampingRatio = 1f, stiffness = 400f, visibilityThreshold = visibilityThreshold)
