package se.carlsmeister.strive

import kotlin.test.Test
import kotlin.test.assertEquals

class CiProofTest {
    @Test
    fun ciProofReturnsTwo() = assertEquals(2, ciProof())
}
