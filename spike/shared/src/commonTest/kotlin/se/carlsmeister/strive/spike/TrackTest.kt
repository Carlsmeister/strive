package se.carlsmeister.strive.spike

import kotlin.test.Test
import kotlin.test.assertEquals

class TrackTest {
    // Umeå, then 0.001° north (about 111 m) one second later, then a 40 s gap.
    private val a = Fix(0, 63.8258, 20.2630, 5.0)
    private val b = Fix(1_000, 63.8268, 20.2630, 5.0)
    private val c = Fix(41_000, 63.8268, 20.2630, 50.0)

    @Test
    fun distanceIsAboutOneHundredElevenMetersPerThousandthDegreeOfLatitude() {
        assertEquals(111.2, listOf(a, b).distanceMeters(), 0.1)
    }

    @Test
    fun inaccurateFixesAreLeftOutOfTheFilteredDistance() {
        assertEquals(0.0, listOf(a, c).distanceMeters(maxAccuracyMeters = 20.0), 0.0)
    }

    @Test
    fun gapsAreTheSecondsBetweenNeighbouringFixes() {
        assertEquals(listOf(1.0, 40.0), listOf(a, b, c).gapsSeconds())
    }
}