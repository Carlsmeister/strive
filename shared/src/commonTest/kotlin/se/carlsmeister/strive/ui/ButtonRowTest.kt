package se.carlsmeister.strive.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/** `.btnrow`'s widths as CSS flex resolves them: a base of each button's padding, the free space by grow, min widths frozen. */
class ButtonRowTest {
    private fun assertWidths(expected: List<Float>, actual: List<Float>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (e, a) -> assertEquals(e, a, 0.01f, "widths $actual") }
    }

    /** The Buttons card's row: two small buttons (16 × 2 padding) and a text button (8 × 2) share 358 dp, 8 dp apart. */
    @Test
    fun theCardsRowSharesWhatThePaddingLeaves() =
        assertWidths(listOf(119.33f, 119.33f, 103.33f), flexWidths(358f, 8f, listOf(32f, 32f, 16f), listOf(1f, 1f, 1f), listOf(0f, 0f, 0f)))

    /** `flex: 2` beside a `.btn`, as 17 of 20 rows on the boards draw it: not weight's 233.3 / 116.7. */
    @Test
    fun aDoubleGrowGetsTwiceTheFreeSpaceNotTwiceTheWidth() =
        assertWidths(listOf(222.67f, 127.33f), flexWidths(358f, 8f, listOf(32f, 32f), listOf(2f, 1f), listOf(0f, 0f)))

    /** A label wider than its share freezes at its min width; the others share what is left. */
    @Test
    fun aMinWidthFreezesItsItemAndTheRestShareTheRemainder() =
        assertWidths(listOf(150f, 104f, 88f), flexWidths(358f, 8f, listOf(32f, 32f, 16f), listOf(1f, 1f, 1f), listOf(150f, 0f, 0f)))
}
