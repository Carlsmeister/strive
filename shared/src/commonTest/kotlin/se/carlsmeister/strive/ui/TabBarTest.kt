package se.carlsmeister.strive.ui

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class TabBarTest {
    @Test
    fun theBarFloats22AboveTheEdgeOverAHomeIndicator() {
        assertEquals(22.dp, barBottom(0.dp))
        assertEquals(22.dp, barBottom(34.dp))
    }

    @Test
    fun theBarClearsATallerSystemBarBy8() = assertEquals(56.dp, barBottom(48.dp))
}
