package se.carlsmeister.strive.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import kotlin.test.Test
import kotlin.test.assertEquals

class CelebrateTest {
    // The bundled family loads only in a composition; any other family stands in for it here.
    private val family = FontFamily.Monospace

    /** moments.css .celebrate: Unbounded 700 at .88 of the size the element already has, -0.02 em, tabular figures. */
    @Test
    fun celebrateIs700AtEightyEightHundredthsOfItsHost() {
        val host = StriveTypography.displaySmall
        val s = host.celebrate(family)
        assertEquals(family, s.fontFamily)
        assertEquals(FontWeight.Bold, s.fontWeight)
        assertEquals(host.fontSize.value * 0.88f, s.fontSize.value, 0.001f)
        assertEquals((-0.02).em, s.letterSpacing)
        assertEquals("tnum", s.fontFeatureSettings)
        assertEquals(host.lineHeight, s.lineHeight)
    }

    /** Inside a value it takes no leading of its own, so it does not raise the line (moments.css, `:is(small, .val) .celebrate`). */
    @Test
    fun insideAValueCelebrateHasLineHeightOne() = assertEquals(1.em, StriveTypography.titleSmall.celebrate(family, insideValue = true).lineHeight)
}
