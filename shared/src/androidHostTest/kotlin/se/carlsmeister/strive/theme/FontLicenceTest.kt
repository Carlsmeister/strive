package se.carlsmeister.strive.theme

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FontLicenceTest {
    private val fonts = File("src/commonMain/composeResources/font")

    /** Unbounded ships under the OFL, which travels with the font (strive#56). */
    @Test
    fun theOflLicenceShipsBesideTheFont() {
        assertEquals(507_828L, File(fonts, "unbounded_bold.ttf").length())
        val licence = File(fonts, "unbounded_ofl.txt").readText()
        assertTrue(licence.startsWith("Copyright 2022 The Unbounded Project Authors"))
        assertTrue("SIL Open Font License, Version 1.1" in licence)
    }
}
