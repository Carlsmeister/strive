package se.carlsmeister.strive.library

import org.jetbrains.compose.resources.StringResource
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.data.Muscle
import strive.shared.generated.resources.*

/** Where Carl's illustration for an exercise lives: its name in lower case with hyphens, as the prompts file says. */
internal fun imagePath(name: String) =
    "files/exercises/" + name.lowercase().replace("'", "").replace(Regex("[^a-z0-9]+"), "-").trim('-') + ".png"

internal val Muscle.label: StringResource
    get() = when (this) {
        Muscle.Chest -> Res.string.muscle_chest; Muscle.Back -> Res.string.muscle_back; Muscle.Shoulders -> Res.string.muscle_shoulders
        Muscle.Biceps -> Res.string.muscle_biceps; Muscle.Triceps -> Res.string.muscle_triceps; Muscle.Forearms -> Res.string.muscle_forearms
        Muscle.Quads -> Res.string.muscle_quads; Muscle.Hamstrings -> Res.string.muscle_hamstrings; Muscle.Glutes -> Res.string.muscle_glutes
        Muscle.Calves -> Res.string.muscle_calves; Muscle.Core -> Res.string.muscle_core; Muscle.FullBody -> Res.string.muscle_full_body
    }

internal val ExerciseType.label: StringResource
    get() = when (this) {
        ExerciseType.WeightReps -> Res.string.type_weight_reps; ExerciseType.BodyweightReps -> Res.string.type_bodyweight_reps
        ExerciseType.Reps -> Res.string.type_reps; ExerciseType.Distance -> Res.string.type_distance; ExerciseType.Time -> Res.string.type_time
    }

internal val Mark?.label: StringResource
    get() = when (this) {
        Mark.Yes -> Res.string.mark_yes; Mark.Sub -> Res.string.mark_sub; Mark.Test -> Res.string.mark_test; Mark.No -> Res.string.mark_no
        null -> Res.string.mark_none
    }

/** The line under "I mina program" saying what the choice does (#17 choice 10). */
internal val Mark?.line: StringResource
    get() = when (this) {
        Mark.Yes -> Res.string.mark_yes_line; Mark.Sub -> Res.string.mark_sub_line; Mark.Test -> Res.string.mark_test_line
        Mark.No -> Res.string.mark_no_line; null -> Res.string.mark_none_line
    }

/** Carl's movement patterns, stored in Swedish as his exercise library names them, in the chip row's order. */
internal val Patterns = linkedMapOf(
    "Horisontell press" to Res.string.pattern_horizontal_press, "Vertikal press" to Res.string.pattern_vertical_press,
    "Horisontellt drag" to Res.string.pattern_horizontal_pull, "Vertikalt drag" to Res.string.pattern_vertical_pull,
    "Knädominant" to Res.string.pattern_knee, "Höftdominant" to Res.string.pattern_hip, "Bål" to Res.string.pattern_core,
    "Armar" to Res.string.pattern_arms, "Vader" to Res.string.pattern_calves, "Explosivt och bär" to Res.string.pattern_explosive,
    "Kettlebell" to Res.string.pattern_kettlebell,
)

/** Equipment as the seed stores it, in the filter sheet's order. */
internal val Equipment = linkedMapOf(
    "Skivstång" to Res.string.equipment_barbell, "Hantlar" to Res.string.equipment_dumbbells, "Kettlebell" to Res.string.equipment_kettlebell,
    "Kabel" to Res.string.equipment_cable, "Maskin" to Res.string.equipment_machine, "EZ-stång" to Res.string.equipment_ez,
    "Kroppsvikt" to Res.string.equipment_bodyweight, "Band" to Res.string.equipment_band, "Annat" to Res.string.equipment_other,
)
