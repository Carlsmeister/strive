package se.carlsmeister.strive.ui

import androidx.compose.ui.graphics.Color
import se.carlsmeister.strive.theme.Scheme
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MomentsTest {
    /** color-mix(in srgb, #ffd23f 12%, #1c1c1e) is #373222, channel by channel. */
    @Test
    fun srgbMixIsCssColorMixInSrgb() {
        val mixed = srgbMix(Color(0xFFFFD23F), Color(0xFF1C1C1E), .12f)
        assertEquals(listOf(0x37, 0x32, 0x22), listOf(mixed.red, mixed.green, mixed.blue).map { (it * 255).roundToInt() })
    }

    @Test
    fun aMomentFillsWithItsWashInDarkAndLight() {
        for (s in listOf(Scheme.Dark, Scheme.Light)) {
            val base = s.colors.surface
            assertEquals(srgbMix(s.moments.limeWash, base, s.moments.tint), momentFill(s.moments, Accent.Lime, base), "$s")
            assertNotEquals(base, momentFill(s.moments, Accent.Lime, base), "$s")
        }
    }

    /** The -hc modes get the edge only, never a fill (moments.css, design-12). */
    @Test
    fun aMomentUnderHighContrastIsTheSurfaceItSitsOn() {
        for (s in listOf(Scheme.DarkHc, Scheme.LightHc)) for (a in Accent.entries)
            assertEquals(s.colors.surfaceContainerHigh, momentFill(s.moments, a, s.colors.surfaceContainerHigh), "$s $a")
    }

    @Test
    fun eachAccentPicksItsOwnColours() {
        val m = Scheme.Dark.moments
        assertEquals(listOf(m.sun, m.flame, m.lime), Accent.entries.map { m.edge(it) })
        assertEquals(listOf(m.sunText, m.flameText, m.limeText), Accent.entries.map { m.text(it) })
        assertEquals(listOf(m.sunWash, m.flameWash, m.limeWash), Accent.entries.map { m.wash(it) })
    }
}
