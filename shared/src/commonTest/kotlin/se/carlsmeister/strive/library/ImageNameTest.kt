package se.carlsmeister.strive.library

import kotlin.test.Test
import kotlin.test.assertEquals

class ImageNameTest {
    @Test
    fun anExerciseNameBecomesItsPromptFileName() = assertEquals("files/exercises/bench-press-bb.png", imagePath("Bench Press BB"))

    @Test
    fun punctuationCollapsesToOneHyphen() =
        assertEquals("files/exercises/turkish-get-up-squat-style.png", imagePath("Turkish Get-Up (Squat Style)"))

    @Test
    fun anApostropheDisappears() = assertEquals("files/exercises/farmers-carry-db.png", imagePath("Farmer's Carry DB"))
}
