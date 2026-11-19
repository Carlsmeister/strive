package se.carlsmeister.strive.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.Load
import se.carlsmeister.strive.data.plateText
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.plates_bar_only
import strive.shared.generated.resources.plates_cannot_load
import strive.shared.generated.resources.plates_per_side

/** A plate's width, height and corner radius in the sleeve's 40-high viewBox, by rank: 25, 20, 15, 10, 5, 2.5 and 1.25 kg (README ### Plates). */
private class PlateSize(val w: Float, val h: Float, val r: Float)

private val PlateSizes = listOf(
    PlateSize(7f, 36f, 1.5f), PlateSize(6f, 32f, 1.5f), PlateSize(5f, 28f, 1.5f), PlateSize(5f, 24f, 1.5f),
    PlateSize(4f, 22f, 1f), PlateSize(3f, 16f, 1f), PlateSize(3f, 14f, 1f),
)

/**
 * One side of the bar as the boards draw it (`.sleeve`): the shaft, the collar, then [perSide] innermost first, 1 apart and centred, each
 * at the size of its rank in [keys] (heaviest first), so an lb 45 draws as a kg 25 (ruling 10). The plate at [newest] is `primary`.
 */
@Composable
internal fun Sleeve(perSide: List<Double>, keys: List<Double>, height: Dp, tag: String, newest: Int? = null, modifier: Modifier = Modifier) {
    val swedish = Locale.current.language == "sv"
    // A plate lighter than every key draws as the smallest, one heavier than every key as the largest.
    val sizes = perSide.map { p -> PlateSizes[(keys.indexOfFirst { it <= p }.takeIf { it >= 0 } ?: keys.lastIndex).coerceIn(0, PlateSizes.lastIndex)] }
    val xs = sizes.runningFold(10f) { x, s -> x + s.w + 1 }
    // Past 64 the viewBox widens to the last plate's end + 2, and the shaft with it.
    val width = maxOf(64f, xs.last() + 1)
    val colors = MaterialTheme.colorScheme
    Canvas(modifier.size(height * (width / 40f), height).semantics { contentDescription = perSide.joinToString(", ") { kilos(it, swedish) } }.testTag(tag)) {
        val unit = size.height / 40f
        fun rect(color: Color, x: Float, y: Float, w: Float, h: Float, r: Float) =
            drawRoundRect(color, Offset(x * unit, y * unit), Size(w * unit, h * unit), CornerRadius(r * unit))
        rect(colors.outlineVariant, 0f, 18f, width, 4f, 1f)
        rect(colors.outline, 4f, 12f, 4f, 16f, 1f)
        sizes.forEachIndexed { i, s -> rect(if (i == newest) colors.primary else colors.onSurface, xs[i], 20 - s.h / 2, s.w, s.h, s.r) }
    }
}

/** `.pline` under the next set (#48): the sleeve and its plates per side, the bar alone, or that the gym's plates cannot make the weight. */
@Composable
internal fun PlatesLine(load: Load, keys: List<Double>) {
    val swedish = Locale.current.language == "sv"
    Row(
        Modifier.testTag("plates-line").padding(start = 44.dp, top = 2.dp, bottom = 4.dp),
        Arrangement.spacedBy(StriveTheme.spacing.s8), Alignment.CenterVertically,
    ) {
        val text = when (load) {
            Load.CannotLoad -> stringResource(Res.string.plates_cannot_load)
            is Load.Plates -> {
                Sleeve(load.perSide, keys, 26.dp, "sleeve")
                if (load.perSide.isEmpty()) stringResource(Res.string.plates_bar_only)
                else stringResource(Res.string.plates_per_side, plateText(load.perSide, swedish))
            }
        }
        Text(text, style = MaterialTheme.typography.bodySmall.board(), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
