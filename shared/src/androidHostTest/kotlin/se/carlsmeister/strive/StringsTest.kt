package se.carlsmeister.strive

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import kotlin.test.Test
import kotlin.test.assertEquals

class StringsTest {
    // A Swedish string that is missing falls back to English without a word, so only a test notices.
    private fun keys(xml: String): Set<String> {
        val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.byteInputStream()).documentElement
        return (0 until root.childNodes.length).map { root.childNodes.item(it) }
            .filterIsInstance<Element>()
            .filter { it.tagName in setOf("string", "plurals", "string-array") }
            .map { it.getAttribute("name") }
            .toSet()
    }

    private fun keysIn(folder: String) = keys(File("src/commonMain/composeResources/$folder/strings.xml").readText())

    @Test
    fun swedishAndEnglishHaveTheSameKeys() {
        assertEquals(keysIn("values"), keysIn("values-sv"))
    }

    @Test
    fun aCommentedOutStringIsNotAKey() {
        assertEquals(setOf("back"), keys("""<resources><string name="back">B</string><!-- <string name="close">C</string> --></resources>"""))
    }

    @Test
    fun pluralsAndArraysAreKeys() {
        val xml = """<resources><plurals name="sets"><item quantity="one">set</item></plurals><string-array name="days"><item>Mon</item></string-array></resources>"""
        assertEquals(setOf("sets", "days"), keys(xml))
    }

    // Compose Resources fills only positional placeholders; a bare %d reaches the screen as "%d".
    @Test
    fun placeholdersArePositional() {
        for (folder in listOf("values", "values-sv")) {
            val text = File("src/commonMain/composeResources/$folder/strings.xml").readText()
            assertEquals(emptyList(), Regex("%[ds]").findAll(text).map { it.value }.toList(), folder)
        }
    }
}
