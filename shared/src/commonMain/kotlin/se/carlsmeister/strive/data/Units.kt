package se.carlsmeister.strive.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The unit an exercise's weights are entered and stored in (#58); null on the exercise means the app default. */
internal enum class WeightUnit(val key: String) {
    Kg("kg"), Lb("lb");

    companion object {
        fun of(key: String?) = entries.firstOrNull { it.key == key }
    }
}

/** The app default unit's setting. #63's control must write this key. */
internal const val UNIT_KEY = "weight_unit"

/** The app default unit: kg until one is stored (#63). */
internal fun Settings.defaultUnit(): Flow<WeightUnit> = get(UNIT_KEY).map { WeightUnit.of(it) ?: WeightUnit.Kg }

/** The one conversion between units (#58): weights stay in their exercise's unit, and only totals across exercises convert. */
internal fun Double.convert(from: WeightUnit, to: WeightUnit): Double = this * from.kilos / to.kilos

// 1 lb is 0.45359237 kg exactly, by the international definition.
private val WeightUnit.kilos get() = if (this == WeightUnit.Lb) 0.45359237 else 1.0
