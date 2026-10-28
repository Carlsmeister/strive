package se.carlsmeister.strive.data

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
}
