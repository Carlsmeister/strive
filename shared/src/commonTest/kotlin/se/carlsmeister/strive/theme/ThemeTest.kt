package se.carlsmeister.strive.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ThemeTest {
    @Test
    fun lightWithoutHighContrastIsLight() = assertEquals(Scheme.Light, scheme(dark = false, highContrast = false))

    @Test
    fun darkWithoutHighContrastIsDark() = assertEquals(Scheme.Dark, scheme(dark = true, highContrast = false))

    @Test
    fun lightWithHighContrastIsLightHc() = assertEquals(Scheme.LightHc, scheme(dark = false, highContrast = true))

    @Test
    fun darkWithHighContrastIsDarkHc() = assertEquals(Scheme.DarkHc, scheme(dark = true, highContrast = true))

    @Test
    fun eachSchemeHasItsOwnColoursAndGlass() {
        assertSame(darkHcColors, Scheme.DarkHc.colors)
        assertSame(lightHcMaterials, Scheme.LightHc.materials)
    }

    @Test
    fun reduceTransparencyMakesGlassTheSolidSurface() {
        val surface = Scheme.Light.colors.surface
        val glass = glass(Scheme.Light, reduceTransparency = true)
        assertEquals(surface, glass.regular)
        assertEquals(surface, glass.thick)
    }

    @Test
    fun withoutReduceTransparencyGlassIsTheSchemesMaterial() =
        assertSame(darkMaterials, glass(Scheme.Dark, reduceTransparency = false))

    /** The set kinds' and the superset's colours read on the rows and the badge they sit on: 4.5:1, or 7:1 under Increase Contrast (Q6). */
    @Test
    fun eachSchemesAccentsReadOnWhatTheySitOn() {
        fun ratio(a: Color, b: Color) = (maxOf(a.luminance(), b.luminance()) + 0.05f) / (minOf(a.luminance(), b.luminance()) + 0.05f)
        for (s in Scheme.entries) {
            val need = if (s == Scheme.LightHc || s == Scheme.DarkHc) 7f else 4.5f
            val c = s.colors
            for (accent in listOf(c.tertiary, s.accents.failure, s.accents.drop, s.accents.superset))
                for (bg in listOf(c.background, c.surface, c.surfaceContainerHigh))
                    assertTrue(ratio(accent, bg) >= need, "$s: $accent on $bg is ${ratio(accent, bg)}:1")
            // Failure is its own coral, not the red of Ta bort.
            assertNotEquals(c.error, s.accents.failure)
        }
    }
}
