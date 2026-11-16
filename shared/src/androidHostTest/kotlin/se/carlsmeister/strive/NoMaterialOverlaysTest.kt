package se.carlsmeister.strive

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoMaterialOverlaysTest {
    private fun sources() = File("src/commonMain/kotlin").walkTopDown().filter { it.extension == "kt" }.toList()

    /** Every menu is the system's glass pull-down: a screen that opens Material's would bring back the old look unseen. */
    @Test
    fun noScreenUsesMaterialsDropdownMenu() {
        assertTrue(sources().size > 20, "the scan found the sources")
        assertEquals(emptyList(), sources().filter { "DropdownMenu" in it.readText() }.map { it.name })
    }

    /** Dialogs and sheets are the system's: nothing outside ui/Dialogs.kt calls Compose's Dialog or Material's sheet or alert. */
    @Test
    fun noDialogOrSheetIsCalledOutsideTheSystemsDialogs() {
        val users = sources().filter { Regex("""\b(Dialog|ModalBottomSheet|AlertDialog)\(""").containsMatchIn(it.readText()) }.map { it.name }
        assertEquals(listOf("Dialogs.kt"), users)
    }

    /** An alert's buttons are the system's: Material's TextButton inside an AlertBox's content would draw the old look in the new panel. */
    @Test
    fun noAlertHoldsMaterialsTextButton() {
        val inside = sources().flatMap { f ->
            val text = f.readText()
            Regex("""(?<!fun )AlertBox\(""").findAll(text).map { alertBody(text.drop(it.range.first)) }.filter { "TextButton(" in it }.map { f.name }
        }
        assertEquals(emptyList(), inside)
    }

    /** The text of an AlertBox call: from its `(` to the brace that closes its content lambda. */
    private fun alertBody(from: String): String {
        var depth = 0
        for ((i, c) in from.withIndex()) {
            if (c == '{') depth++
            if (c == '}' && --depth == 0) return from.take(i)
        }
        return from
    }
}
