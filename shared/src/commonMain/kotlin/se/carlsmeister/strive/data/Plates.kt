package se.carlsmeister.strive.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.StringResource
import se.carlsmeister.strive.library.kilos
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.bar_ez
import strive.shared.generated.resources.bar_hex
import strive.shared.generated.resources.bar_name_ez
import strive.shared.generated.resources.bar_name_hex
import strive.shared.generated.resources.bar_name_olympic
import strive.shared.generated.resources.bar_name_short
import strive.shared.generated.resources.bar_none
import strive.shared.generated.resources.bar_olympic
import strive.shared.generated.resources.bar_short
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
 * down to what [plates] load on [bar], or to a multiple of [step] when there is no bar. A rung under the bar is dropped, and a
 * [firstWorking] lighter than the bar has no ladder at all.
 */
internal fun ladder(firstWorking: Double, bar: Double?, step: Double, plates: List<Double>): List<Rung> {
    val first = firstWorking.hundredths()
    val barH = bar?.hundredths()
    // A working weight under the bar has no ladder: even the bar alone would outweigh it.
    if (barH != null && first < barH) return emptyList()
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

/** The gym's plates setting for this unit. #63's control must write these keys in the format ADR 0008 records. */
internal val WeightUnit.platesKey get() = "plates_$key"

/** Every kg plate but 15; EL-07-lb's lb plates. */
internal val WeightUnit.defaultPlates
    get() = if (this == WeightUnit.Lb) listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5) else listOf(25.0, 20.0, 10.0, 5.0, 2.5, 1.25)

/** The Plates key's plates per unit; one the gym lacks is greyed. */
internal val WeightUnit.plateKeys
    get() = if (this == WeightUnit.Lb) listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5) else listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

/** The gym's plates in [unit]: the stored list, or the default when nothing parsable is stored. */
internal fun Settings.plates(unit: WeightUnit): Flow<List<Double>> = weights(unit.platesKey, unit.defaultPlates)

/** The gym's bars setting for this unit, in the plates' format. None is always offered and never stored. */
internal val WeightUnit.barsKey get() = "bars_$key"

/** Carl's gym: Olympic, Short, EZ and Hex. */
internal val WeightUnit.defaultBars get() = Bars.mapNotNull { it.weight(this) }

/** The gym's bars in [unit], read as [plates] reads its plates. */
internal fun Settings.bars(unit: WeightUnit): Flow<List<Double>> = weights(unit.barsKey, unit.defaultBars)

// A bad entry is skipped and an unusable value falls back, so a corrupt setting never reaches the plate maths.
private fun Settings.weights(key: String, default: List<Double>) = get(key).map { value ->
    value?.split(',')?.mapNotNull { it.trim().toDoubleOrNull()?.takeIf { w -> w.isFinite() && w.hundredths() > 0 } }?.ifEmpty { null } ?: default
}

/**
 * A bar by name (Q22, WO-12) with its weight in kg and in lb (EL-07-lb); None weighs nothing and saves no bar. [name] reads "Olympic 20 kg",
 * [short] the bare "Olympic" the dock's bar pill describes; None has none, as no pill shows without a bar.
 */
internal class NamedBar(val name: StringResource, val short: StringResource?, val kg: Double?, val lb: Double?) {
    fun weight(unit: WeightUnit) = if (unit == WeightUnit.Lb) lb else kg
}

internal val Bars = listOf(
    NamedBar(Res.string.bar_olympic, Res.string.bar_name_olympic, 20.0, 45.0), NamedBar(Res.string.bar_short, Res.string.bar_name_short, 15.0, 35.0),
    NamedBar(Res.string.bar_ez, Res.string.bar_name_ez, 7.5, 15.0), NamedBar(Res.string.bar_hex, Res.string.bar_name_hex, 25.0, 55.0),
    NamedBar(Res.string.bar_none, null, null, null),
)

/** The named bar that weighs [weight] in [unit] (None for no bar), or null when none does and the bar reads by its weight ("10 kg"). */
internal fun namedBar(weight: Double?, unit: WeightUnit): NamedBar? = Bars.firstOrNull { it.weight(unit) == weight }
