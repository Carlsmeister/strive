package se.carlsmeister.strive.data

import se.carlsmeister.strive.data.WeightUnit.Kg
import se.carlsmeister.strive.data.WeightUnit.Lb
import kotlin.test.Test
import kotlin.test.assertEquals

class UnitsTest {
    @Test
    fun convertsLbToKgAndBack() {
        assertEquals(45.359237, 100.0.convert(Lb, Kg), 1e-9)
        assertEquals(100.0, 45.359237.convert(Kg, Lb), 1e-9)
        assertEquals(20.0, 20.0.convert(Kg, Kg))
    }
}
