package se.carlsmeister.strive.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlatesTest {
    private val kg = listOf(25.0, 20.0, 10.0, 5.0, 2.5, 1.25)
    private val lb = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)

    @Test
    fun fewestPlatesForATypedWeight() {
        assertEquals(Load.Plates(listOf(25.0, 5.0, 1.25)), load(82.5, 20.0, kg))
        assertEquals(Load.Plates(listOf(25.0, 25.0, 25.0, 25.0, 1.25)), load(222.5, 20.0, kg))
        assertEquals(Load.Plates(listOf(20.0)), load(60.0, 20.0, kg))
        // Greedy would give 25, 10, 5.
        assertEquals(Load.Plates(listOf(20.0, 20.0)), load(100.0, 20.0, kg))
    }

    @Test
    fun aMissingPlateSizeIsMadeFromTheOthers() {
        assertEquals(Load.Plates(listOf(10.0, 5.0)), load(50.0, 20.0, kg))
        assertEquals(Load.Plates(listOf(10.0, 10.0)), load(60.0, 20.0, listOf(25.0, 10.0, 5.0)))
        // Greedy takes 25 and finds nothing.
        assertEquals(Load.Plates(listOf(10.0, 10.0, 10.0)), load(80.0, 20.0, listOf(25.0, 10.0)))
    }

    @Test
    fun lbPlatesOnAnLbBar() {
        assertEquals(Load.Plates(listOf(45.0)), load(135.0, 45.0, lb))
        assertEquals(Load.Plates(listOf(45.0, 25.0)), load(185.0, 45.0, lb))
    }

    @Test
    fun aWeightThatCannotBeMadeIsReported() {
        assertEquals(Load.CannotLoad, load(83.0, 20.0, kg))
        assertEquals(Load.CannotLoad, load(82.5, 20.0, listOf(25.0, 5.0)))
    }

    @Test
    fun atOrBelowTheBar() {
        assertEquals(Load.Plates(emptyList()), load(20.0, 20.0, kg))
        assertEquals(Load.CannotLoad, load(15.0, 20.0, kg))
    }

    @Test
    fun hundredthsNeverDrift() {
        assertEquals(82.5, weightOf(20.0, listOf(25.0, 5.0, 1.25)))
        // A Double sum gives 1.9999999999999998.
        assertEquals(2.0, weightOf(0.0, List(10) { 0.1 }))
        // 0.7 × 350 in Double is 244.99999999999997 and floors to 242.5.
        assertEquals(Rung(245.0, 3), ladder(350.0, 20.0, 2.5, kg)[2])
    }

    @Test
    fun plateTextGroupsRepeats() {
        assertEquals("25 + 5 + 1.25", plateText(listOf(25.0, 5.0, 1.25), false))
        assertEquals("4 × 25 + 1.25", plateText(listOf(25.0, 25.0, 25.0, 25.0, 1.25), false))
        assertEquals("10 + 5 + 2,5", plateText(listOf(10.0, 5.0, 2.5), true))
    }

    @Test
    fun eachRungOfTheLadder() {
        assertEquals(
            listOf(Rung(20.0, 10), Rung(40.0, 5), Rung(55.0, 3), Rung(67.5, 1)),
            ladder(80.0, 20.0, 2.5, kg),
        )
    }

    @Test
    fun aRungRoundsDownToALoadableWeight() {
        // 59.5 → 57.5, where nearest would give 60.
        assertEquals(Rung(57.5, 3), ladder(85.0, 20.0, 2.5, kg)[2])
        // 56 → 50, the heaviest these plates load; rounding to the step would give 55.
        assertEquals(Rung(50.0, 3), ladder(80.0, 20.0, 2.5, listOf(25.0, 20.0, 10.0, 5.0))[2])
    }

    @Test
    fun aRungLighterThanTheBarIsDropped() {
        // 50 % = 15 is under the bar and dropped; 21 rounds down to 20; 25.5 to 25.
        assertEquals(listOf(Rung(20.0, 10), Rung(20.0, 3), Rung(25.0, 1)), ladder(30.0, 20.0, 2.5, kg))
        // A working 15 on a 20 bar: a 20 × 10 warm-up would outweigh it, so there is no ladder.
        assertEquals(emptyList(), ladder(15.0, 20.0, 2.5, kg))
    }

    @Test
    fun withoutABarTheBarRungIsDroppedAndRungsRoundDownToTheStep() {
        // 42.5 stays; 59.5 → 57.5; 72.25 → 70.
        assertEquals(listOf(Rung(42.5, 5), Rung(57.5, 3), Rung(70.0, 1)), ladder(85.0, null, 2.5, kg))
    }

    @Test
    fun aZeroStepFallsBackToTwoAndAHalf() {
        val atTwoAndAHalf = listOf(Rung(42.5, 5), Rung(57.5, 3), Rung(70.0, 1))
        assertEquals(atTwoAndAHalf, ladder(85.0, null, 0.0, kg))
        // 0.001 rounds to 0 hundredths.
        assertEquals(atTwoAndAHalf, ladder(85.0, null, 0.001, kg))
    }

    @Test
    fun sparsePlatesRoundEachRungDownToTheHeaviestTheyLoad() {
        // Side amounts 65, 95 and 117.5 kg fall to 50, 75 and 100 on 25s alone.
        assertEquals(
            listOf(Rung(20.0, 10), Rung(120.0, 5), Rung(170.0, 3), Rung(220.0, 1)),
            ladder(300.0, 20.0, 2.5, listOf(25.0)),
        )
    }

    @Test
    fun anAbsurdWeightCannotBeLoaded() {
        assertEquals(Load.CannotLoad, load(99999999.0, 20.0, kg))
    }

    @Test
    fun nextRungSkipsWhatIsDone() {
        val rungs = ladder(80.0, 20.0, 2.5, kg)
        assertEquals(Rung(55.0, 3), nextRung(rungs, listOf(40.0)))
        assertEquals(Rung(20.0, 10), nextRung(rungs, emptyList()))
        assertNull(nextRung(rungs, listOf(67.5)))
    }
}
