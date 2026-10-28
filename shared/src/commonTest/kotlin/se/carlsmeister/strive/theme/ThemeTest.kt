package se.carlsmeister.strive.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

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
}
