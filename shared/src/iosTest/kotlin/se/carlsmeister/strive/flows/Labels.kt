package se.carlsmeister.strive.flows

import org.jetbrains.compose.resources.getString
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.field_weight
import strive.shared.generated.resources.field_weight_step

/** The dock's weight label: "Weight, kg", or "Weight, lb · step 5" for an exercise without a bar (#58, ruling 2). */
internal suspend fun weightLabel(unit: String, step: String? = null): String =
    if (step == null) getString(Res.string.field_weight, unit) else getString(Res.string.field_weight_step, unit, step)
