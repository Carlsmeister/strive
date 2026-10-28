package se.carlsmeister.strive.library

import se.carlsmeister.strive.data.Category
import se.carlsmeister.strive.data.Exercise
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.data.Muscle
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryFilterTest {
    private fun exercise(
        name: String, primary: Muscle = Muscle.Chest, pattern: String = "Horisontell press", equipment: String = "Skivstång",
        aliases: List<String> = emptyList(), category: Category = Category.Strength, mark: Mark? = null,
    ) = Exercise(
        id = name.hashCode().toLong(), name = name, aliases = aliases, primary = primary, secondary = emptyList(), pattern = pattern,
        equipment = equipment, type = ExerciseType.WeightReps, perSide = false, step = null, bar = null, category = category,
        stepsEn = "", tipsEn = "", stepsSv = "", tipsSv = "", mark = mark, note = null,
    )

    private val bench = exercise("Bench Press BB", aliases = listOf("Bänkpress"))
    private val fly = exercise("Fly DB", equipment = "Hantlar")
    private val curl = exercise("Biceps Curl DB", primary = Muscle.Biceps, pattern = "Armar", equipment = "Hantlar")
    private val stretch = exercise("Chest Stretch", category = Category.Mobility)
    private val bike = exercise("Stationary Bike", primary = Muscle.Quads, category = Category.Conditioning)
    private val pecDec = exercise("Pec Dec", mark = Mark.No)
    private val all = listOf(curl, fly, bench, stretch, bike, pecDec)

    private fun names(groups: Map<Muscle, List<Exercise>>) = groups.mapValues { (_, list) -> list.map { it.name } }

    @Test
    fun theDefaultListIsStrengthByMuscleInOrderWithoutNej() = assertEquals(
        mapOf(Muscle.Chest to listOf("Bench Press BB", "Fly DB"), Muscle.Biceps to listOf("Biceps Curl DB")),
        names(filter(all, LibraryFilter())),
    )

    @Test
    fun searchMatchesAnAliasRegardlessOfCase() =
        assertEquals(mapOf(Muscle.Chest to listOf("Bench Press BB")), names(filter(all, LibraryFilter(query = "bänk"))))

    @Test
    fun searchFindsMobilityToo() =
        assertEquals(mapOf(Muscle.Chest to listOf("Chest Stretch")), names(filter(all, LibraryFilter(query = "stretch"))))

    @Test
    fun aPatternChipKeepsThatPattern() =
        assertEquals(mapOf(Muscle.Biceps to listOf("Biceps Curl DB")), names(filter(all, LibraryFilter(chip = Chip.Pattern("Armar")))))

    @Test
    fun theMobilityChipShowsOnlyMobility() =
        assertEquals(mapOf(Muscle.Chest to listOf("Chest Stretch")), names(filter(all, LibraryFilter(chip = Chip.Of(Category.Mobility)))))

    @Test
    fun equipmentFiltersCombine() = assertEquals(
        mapOf(Muscle.Chest to listOf("Fly DB"), Muscle.Biceps to listOf("Biceps Curl DB")),
        names(filter(all, LibraryFilter(equipment = setOf("Hantlar")))),
    )

    @Test
    fun tickingNejShowsThoseToo() = assertEquals(
        listOf("Bench Press BB", "Fly DB", "Pec Dec"),
        names(filter(all, LibraryFilter(marks = Mark.entries.toSet() + null))).getValue(Muscle.Chest),
    )
}
