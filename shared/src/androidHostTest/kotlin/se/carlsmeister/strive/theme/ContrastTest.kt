package se.carlsmeister.strive.theme

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContrastTest {
    @Test
    fun standardContrastIsNotHigh() = assertFalse(highContrast(0f))

    @Test
    fun mediumContrastIsHigh() = assertTrue(highContrast(0.5f))

    @Test
    fun highContrastIsHigh() = assertTrue(highContrast(1f))

    @Test
    fun reducedContrastIsNotHigh() = assertFalse(highContrast(-0.5f))
}
