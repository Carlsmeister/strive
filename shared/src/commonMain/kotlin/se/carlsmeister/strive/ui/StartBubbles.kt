package se.carlsmeister.strive.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

internal enum class StartChoice { Continue, Empty, Run }

/** With a workout open, Start only continues it (SH-03-inprog), so nothing starts a second one. */
internal fun startChoices(workoutOpen: Boolean) =
    if (workoutOpen) listOf(StartChoice.Continue) else listOf(StartChoice.Empty, StartChoice.Run)

/** The bubbles' centres from Start's centre, raised by [lift] (P2: SH-03-today, SH-03-run, SH-03-inprog; two take the inner pair). */
internal fun bubbleCentres(count: Int, lift: Dp = 0.dp): List<DpOffset> {
    require(count in 1..4) { "Start has room for one to four bubbles, not $count" }
    val arc = when (count) {
        1 -> listOf(0 to -160)
        2 -> listOf(-47 to -157, 47 to -157)
        3 -> listOf(-112 to -106, 0 to -164, 112 to -106)
        else -> listOf(-112 to -106, -47 to -157, 47 to -157, 112 to -106)
    }
    return arc.map { (x, y) -> DpOffset(x.dp, y.dp - lift) }
}
