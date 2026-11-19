package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsTest {
    private val settings = SqlSettings(StriveDatabase(memoryDriver()))

    @Test
    fun aWrittenSettingReadsBack() = runTest {
        settings.put("unit", "kg")
        assertEquals("kg", settings.get("unit").first())
    }

    @Test
    fun anUnsetSettingIsNull() = runTest {
        assertNull(settings.get("unit").first())
    }

    @Test
    fun writingAgainReplacesTheValue() = runTest {
        settings.put("unit", "kg")
        settings.put("unit", "lb")
        assertEquals("lb", settings.get("unit").first())
    }

    @Test
    fun defaultUnitIsKgUntilOneIsStored() = runTest {
        assertEquals(WeightUnit.Kg, settings.defaultUnit().first())
        settings.put(UNIT_KEY, "lb")
        assertEquals(WeightUnit.Lb, settings.defaultUnit().first())
    }

    @Test
    fun thePlatesDefaultToEveryKgPlateBut15() = runTest {
        assertEquals(listOf(25.0, 20.0, 10.0, 5.0, 2.5, 1.25), settings.plates(WeightUnit.Kg).first())
        assertEquals(listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5), settings.plates(WeightUnit.Lb).first())
    }

    @Test
    fun storedPlatesReplaceTheDefault() = runTest {
        settings.put("plates_kg", "25,10,1.25")
        assertEquals(listOf(25.0, 10.0, 1.25), settings.plates(WeightUnit.Kg).first())
    }

    @Test
    fun badPlateValuesFallBackToTheDefaults() = runTest {
        settings.put("plates_kg", "")
        assertEquals(WeightUnit.Kg.defaultPlates, settings.plates(WeightUnit.Kg).first())
        settings.put("plates_kg", "abc,25,-5,0")
        assertEquals(listOf(25.0), settings.plates(WeightUnit.Kg).first())
        settings.put("plates_kg", "abc,-5")
        assertEquals(WeightUnit.Kg.defaultPlates, settings.plates(WeightUnit.Kg).first())
        settings.put("plates_kg", "NaN,Infinity,25")
        assertEquals(listOf(25.0), settings.plates(WeightUnit.Kg).first())
        // Under a hundredth the plate maths drops a plate, so a list of only those is no list.
        settings.put("plates_kg", "0.001")
        assertEquals(WeightUnit.Kg.defaultPlates, settings.plates(WeightUnit.Kg).first())
    }

    @Test
    fun theBarsDefaultToCarlsGym() = runTest {
        assertEquals(listOf(20.0, 15.0, 7.5, 25.0), settings.bars(WeightUnit.Kg).first())
        assertEquals(listOf(45.0, 35.0, 15.0, 55.0), settings.bars(WeightUnit.Lb).first())
        settings.put("bars_kg", "20,10")
        assertEquals(listOf(20.0, 10.0), settings.bars(WeightUnit.Kg).first())
        assertNull(namedBar(10.0, WeightUnit.Kg))
        assertEquals(Bars[2], namedBar(7.5, WeightUnit.Kg))
    }
}
