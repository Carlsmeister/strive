package se.carlsmeister.strive

import kotlin.test.Test
import kotlin.test.assertEquals

class PopTest {
    @Test
    fun popRemovesTheTopScreen() {
        val stack = mutableListOf("Home", "Home 2")
        stack.pop()
        assertEquals(listOf("Home"), stack)
    }

    @Test
    fun popNeverEmptiesTheStack() {
        // A second tap on Back or Close can land during the exit animation; an empty stack crashes NavDisplay.
        val stack = mutableListOf("Home")
        stack.pop()
        assertEquals(listOf("Home"), stack)
    }
}
