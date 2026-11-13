package se.carlsmeister.strive.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
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

    @Test
    fun eachSchemeHasItsOwnMomentsAndRoles() {
        assertSame(darkHcMoments, Scheme.DarkHc.moments)
        assertSame(lightRoles, Scheme.Light.roles)
    }

    /** The Dynamic Island is always dark, so every mode draws it with the dark scheme's accents (design 1, polish 9). */
    @Test
    fun theIslandTakesTheDarkSchemesAccentsInEveryMode() {
        assertEquals(darkColors.primary to darkColors.tertiary, Scheme.Light.roles.islandPrimary to Scheme.Light.roles.islandTertiary)
        assertEquals(darkHcColors.primary to darkHcColors.tertiary, Scheme.LightHc.roles.islandPrimary to Scheme.LightHc.roles.islandTertiary)
        assertEquals(darkColors.primary, Scheme.Dark.roles.islandPrimary)
    }

    /** The next set's row is the scheme's teal at 12 %, 14 % in dark-HC, 10 % light, 12 % light-HC (design 1). */
    @Test
    fun theNextRowIsEachSchemesPrimaryAtItsOwnAlpha() {
        for ((s, alpha) in listOf(Scheme.Dark to .12f, Scheme.DarkHc to .14f, Scheme.Light to .1f, Scheme.LightHc to .12f)) {
            assertEquals(s.colors.primary.copy(alpha = 1f), s.roles.stateNextRow.copy(alpha = 1f), "$s")
            assertEquals(alpha, s.roles.stateNextRow.alpha, 1f / 255, "$s")
            assertEquals(s.colors.primary, s.roles.mapRoute, "$s")
        }
    }

    /** A moment's wash shows in dark and light only; the -hc modes get a wider edge and no wash (moments.css). */
    @Test
    fun highContrastMomentsHaveNoWashAndAWiderEdge() {
        assertEquals(listOf(.12f, 0f, .14f, 0f), listOf(Scheme.Dark, Scheme.DarkHc, Scheme.Light, Scheme.LightHc).map { it.moments.tint })
        assertEquals(listOf(1.5.dp, 2.dp, 1.5.dp, 2.dp), listOf(Scheme.Dark, Scheme.DarkHc, Scheme.Light, Scheme.LightHc).map { it.moments.edge })
    }

    /** Dark-HC's destructive red: light text at 7:1 on every container, and a red slab with black on it (design 1, polish 4). */
    @Test
    fun darkHcErrorIsDesign1s() {
        assertEquals(Color(0xFFFFBDB6), darkHcColors.error)
        assertEquals(Color(0xFFFF6961), darkHcColors.errorContainer)
        assertEquals(Color.Black, darkHcColors.onErrorContainer)
    }
}
