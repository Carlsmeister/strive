package se.carlsmeister.strive.data

import se.carlsmeister.strive.library.kilos
import kotlin.math.roundToLong

/** How a total loads on the bar (#48): the plates for one side, heaviest first, or that it cannot be made. */
internal sealed interface Load {
    /** Empty is the bar alone. */
    data class Plates(val perSide: List<Double>) : Load
    data object CannotLoad : Load
}

// Weights are worked in hundredths so sums and percentages never drift.
private fun Double.hundredths() = (this * 100).roundToLong()

// No gym loads 1000 kg a side: above it a typed total is a typo, and its table would not fit in memory.
private const val MAX_SIDE = 100_000L
private const val NONE = Int.MAX_VALUE

/** Min-count coin change up to [side] hundredths: fewest[a] plates make a (NONE if none do), pick[a] is the plate taken first. */
private class Sides(side: Int, available: List<Double>) {
    val fewest = IntArray(side + 1) { NONE }.also { it[0] = 0 }
    val pick = IntArray(side + 1)

    init {
        val sizes = available.map { it.hundredths().toInt() }.filter { it > 0 }.distinct().sortedDescending()
        for (a in 1..side) for (p in sizes) {
            if (p <= a && fewest[a - p] != NONE && fewest[a - p] + 1 < fewest[a]) {
                fewest[a] = fewest[a - p] + 1
                pick[a] = p
            }
        }
    }

    /** The heaviest side these plates make at or under [amount]; 0, the bare bar, always is. */
    fun heaviestUpTo(amount: Long) = (amount.coerceAtMost(fewest.lastIndex.toLong()).toInt() downTo 0).first { fewest[it] != NONE }
}

/** The fewest plates per side that make [total] on [bar]; ties go to the heavier plate first. */
internal fun load(total: Double, bar: Double, available: List<Double>): Load {
    // shortcut: unlimited plates of each size; track counts when #63's editor stores them.
    val both = total.hundredths() - bar.hundredths()
    if (both < 0 || both % 2 != 0L || both / 2 > MAX_SIDE) return Load.CannotLoad
    val side = (both / 2).toInt()
    val sides = Sides(side, available)
    if (sides.fewest[side] == NONE) return Load.CannotLoad
    val plates = generateSequence(side) { it - sides.pick[it] }.takeWhile { it > 0 }.map { sides.pick[it] / 100.0 }
    return Load.Plates(plates.toList().sortedDescending())
}

/** The total the bar and these plates on each side make, exact to the hundredth. */
internal fun weightOf(bar: Double, perSide: List<Double>): Double =
    (bar.hundredths() + 2 * perSide.sumOf { it.hundredths() }) / 100.0

/** One side's plates as "4 × 25 + 1.25": a repeated size grouped, in order of first appearance. */
internal fun plateText(perSide: List<Double>, swedish: Boolean): String =
    perSide.groupingBy { it }.eachCount().entries.joinToString(" + ") { (plate, count) ->
        if (count == 1) kilos(plate, swedish) else "$count × ${kilos(plate, swedish)}"
    }

internal data class Rung(val weight: Double, val reps: Int)

/**
 * Q22's warm-up ladder to [firstWorking]: the bar × 10, then 50 % × 5, 70 % × 3 and 85 % × 1, each rounded
 * down to what [plates] load on [bar], or to a multiple of [step] when there is no bar. A rung under the bar is dropped.
 */
internal fun ladder(firstWorking: Double, bar: Double?, step: Double, plates: List<Double>): List<Rung> {
    val first = firstWorking.hundredths()
    val barH = bar?.hundredths()
    // One table up to the top rung's side serves every rung.
    val sides = barH?.let { Sides(((first * 85 / 100 - it) / 2).coerceIn(0, MAX_SIDE).toInt(), plates) }
    // A step that rounds to nothing falls back to the 2.5 default.
    val stepH = step.hundredths().takeIf { it > 0 } ?: 250
    val rungs = listOf(50 to 5, 70 to 3, 85 to 1).mapNotNull { (percent, reps) ->
        val raw = first * percent / 100
        val weight = when {
            barH == null || sides == null -> raw - raw % stepH
            raw < barH -> null
            else -> barH + 2 * sides.heaviestUpTo((raw - barH) / 2)
        }
        weight?.takeIf { it > 0 }?.let { Rung(it / 100.0, reps) }
    }
    return if (bar == null) rungs else listOf(Rung(bar, 10)) + rungs
}

/** The first rung above the heaviest warm-up done so far, or null when none is left. */
internal fun nextRung(rungs: List<Rung>, warmups: List<Double>): Rung? {
    val done = warmups.maxOfOrNull { it.hundredths() } ?: 0
    return rungs.firstOrNull { it.weight.hundredths() > done }
}
