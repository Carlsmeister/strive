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
}
