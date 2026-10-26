package se.carlsmeister.strive

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class StringsTest {
    // A Swedish string that is missing falls back to English without a word, so only a test notices.
    private fun keys(folder: String): Set<String> =
        Regex("""<string name="([^"]+)"""")
            .findAll(File("src/commonMain/composeResources/$folder/strings.xml").readText())
            .map { it.groupValues[1] }
            .toSet()

    @Test
    fun swedishAndEnglishHaveTheSameKeys() {
        assertEquals(keys("values"), keys("values-sv"))
    }
}
