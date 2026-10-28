package se.carlsmeister.strive.library

import androidx.compose.runtime.saveable.listSaver
import se.carlsmeister.strive.data.Category
import se.carlsmeister.strive.data.Exercise
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.data.Muscle

/** One chip in the library's row: one of Carl's movement patterns, or mobility or conditioning (decision 36). */
internal sealed interface Chip {
    data class Pattern(val name: String) : Chip
    data class Of(val category: Category) : Chip
}

/** Nej starts unticked, which is how NO exercises stay hidden until asked for (#17 choice 12b). */
internal val DefaultMarks: Set<Mark?> = setOf(Mark.Yes, Mark.Sub, Mark.Test, null)

internal data class LibraryFilter(
    val query: String = "",
    val chip: Chip? = null,
    val equipment: Set<String> = emptySet(),
    val marks: Set<Mark?> = DefaultMarks,
) {
    /** The filter button fills while a filter is on (#17 R1). */
    val filtering get() = equipment.isNotEmpty() || marks != DefaultMarks
}

/** Keeps the filter while an exercise is open on top of the library; Android needs plain strings and lists to save it. */
@Suppress("UNCHECKED_CAST")
internal val LibraryFilterSaver = listSaver<LibraryFilter, Any>(
    save = { f ->
        val chip = when (val c = f.chip) {
            is Chip.Pattern -> "p:" + c.name
            is Chip.Of -> "c:" + c.category.key
            null -> ""
        }
        listOf(f.query, chip, ArrayList(f.equipment), ArrayList(f.marks.map { it?.name ?: "" }))
    },
    restore = { saved ->
        val chip = (saved[1] as String).let {
            when {
                it.startsWith("p:") -> Chip.Pattern(it.drop(2))
                it.startsWith("c:") -> Chip.Of(Category.of(it.drop(2)))
                else -> null
            }
        }
        LibraryFilter(saved[0] as String, chip, (saved[2] as List<String>).toSet(),
            (saved[3] as List<String>).map { if (it.isEmpty()) null else Mark.valueOf(it) }.toSet())
    },
)

/**
 * The library's sections: exercises grouped by primary muscle in [Muscle] order, A–Ö within each. Without a search or a
 * category chip it lists strength only; a search reaches every category.
 */
internal fun filter(exercises: List<Exercise>, f: LibraryFilter): Map<Muscle, List<Exercise>> {
    val query = f.query.trim()
    return exercises.asSequence()
        .filter { e ->
            when (val chip = f.chip) {
                is Chip.Pattern -> e.category == Category.Strength && e.pattern == chip.name
                is Chip.Of -> e.category == chip.category
                null -> query.isNotEmpty() || e.category == Category.Strength
            }
        }
        .filter { query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.aliases.any { a -> a.contains(query, ignoreCase = true) } }
        .filter { f.equipment.isEmpty() || it.equipment in f.equipment }
        .filter { it.mark in f.marks }
        .sortedBy { it.name.lowercase() }
        .groupBy { it.primary }
        .let { groups -> Muscle.entries.mapNotNull { m -> groups[m]?.let { m to it } }.toMap() }
}