package se.carlsmeister.strive.data

/** The unit an exercise's weights are entered and stored in (#58); null on the exercise means the app default. */
internal enum class WeightUnit(val key: String) {
    Kg("kg"), Lb("lb");

    companion object {
        fun of(key: String?) = entries.firstOrNull { it.key == key }
    }
}
