package se.carlsmeister.strive.ui

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StartBubblesTest {
    @Test
    fun aWorkoutOpenOffersOnlyToContinueIt() = assertEquals(listOf(StartChoice.Continue), startChoices(workoutOpen = true))

    @Test
    fun noWorkoutOpenOffersAnEmptyWorkoutAndARun() = assertEquals(listOf(StartChoice.Empty, StartChoice.Run), startChoices(workoutOpen = false))

    @Test
    fun oneBubbleSitsOverStartLiftedByTheBarAboveIt() {
        assertEquals(listOf(DpOffset(0.dp, (-160).dp)), bubbleCentres(1))
        assertEquals(listOf(DpOffset(0.dp, (-220).dp)), bubbleCentres(1, lift = 60.dp))
    }

    @Test
    fun twoBubblesTakeTheArcsInnerPair() =
        assertEquals(listOf(DpOffset((-47).dp, (-157).dp), DpOffset(47.dp, (-157).dp)), bubbleCentres(2))

    @Test
    fun threeAndFourBubblesTakeTheBoardsPositions() {
        assertEquals(listOf(DpOffset((-112).dp, (-106).dp), DpOffset(0.dp, (-164).dp), DpOffset(112.dp, (-106).dp)), bubbleCentres(3))
        assertEquals(listOf(DpOffset((-112).dp, (-106).dp), DpOffset((-47).dp, (-157).dp), DpOffset(47.dp, (-157).dp), DpOffset(112.dp, (-106).dp)), bubbleCentres(4))
    }

    @Test
    fun moreThanFourBubblesAreRefused() {
        assertFailsWith<IllegalArgumentException> { bubbleCentres(5) }
    }
}
