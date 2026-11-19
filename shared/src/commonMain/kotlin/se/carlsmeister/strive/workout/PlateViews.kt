package se.carlsmeister.strive.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.Load
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.WeightUnit
import se.carlsmeister.strive.data.bars
import se.carlsmeister.strive.data.defaultBars
import se.carlsmeister.strive.data.namedBar
import se.carlsmeister.strive.data.plateText
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.GlassMenu
import se.carlsmeister.strive.ui.MenuRow
import se.carlsmeister.strive.ui.touchTall
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.bar_pill
import strive.shared.generated.resources.bar_pill_description
import strive.shared.generated.resources.bar_pill_description_unnamed
import strive.shared.generated.resources.per_side
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
    // [height] tall, or narrower and shorter at the same shape where the room is narrower (`.barline svg`'s `meet`).
    Canvas(modifier.heightIn(max = height).aspectRatio(width / 40f).semantics { contentDescription = perSide.joinToString(", ") { kilos(it, swedish) } }.testTag(tag)) {
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

/** A bar as WO-12 names it, "Olympic 20 kg" or "None 0 kg", and one without a name by its weight, "10 kg". */
@Composable
internal fun barText(bar: Double?, unit: WeightUnit): String {
    val amount = "${kilos(bar ?: 0.0, Locale.current.language == "sv")} ${stringResource(unit.label)}"
    return namedBar(bar, unit)?.let { stringResource(it.name, amount) } ?: amount
}

/**
 * `.barline` in the dock (WO-04-new): the sleeve, the plates per side for [perSide] on [bar], and the "Bar 20" pill whose menu picks the
 * exercise's bar from the gym's bars or None, calling [onBar] (None is null). [newest] is the sleeve's `primary` plate.
 */
@Composable
internal fun BarLine(perSide: Load, keys: List<Double>, bar: Double, unit: WeightUnit, newest: Int?, onBar: (Double?) -> Unit) {
    val swedish = Locale.current.language == "sv"
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val graph = LocalAppGraph.current
    val bars by remember(unit) { graph.settings.bars(unit) }.collectAsState(unit.defaultBars)
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(StriveTheme.spacing.s12), Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(44.dp), contentAlignment = Alignment.CenterStart) {
            if (perSide is Load.Plates) Sleeve(perSide.perSide, keys, 44.dp, "bar-sleeve", newest)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            val muted = type.labelMedium.board()
            if (perSide is Load.Plates && perSide.perSide.isNotEmpty()) {
                Text(plateText(perSide.perSide, swedish), style = type.titleSmall.board(), color = colors.onSurface, textAlign = TextAlign.End)
                Text(stringResource(Res.string.per_side).lowercase(), style = muted, color = colors.onSurfaceVariant)
            } else {
                Text(
                    stringResource(if (perSide is Load.Plates) Res.string.plates_bar_only else Res.string.plates_cannot_load),
                    style = muted, color = colors.onSurfaceVariant, textAlign = TextAlign.End,
                )
            }
        }
        val amount = "${kilos(bar, swedish)} ${stringResource(unit.label)}"
        val description = namedBar(bar, unit)?.short?.let { stringResource(Res.string.bar_pill_description, stringResource(it), amount) }
            ?: stringResource(Res.string.bar_pill_description_unnamed, amount)
        Box {
            // `.barmenu` is drawn 36 tall and tapped a touch tall.
            Box(
                Modifier.touchTall().clickable { open = true }.semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    Modifier.heightIn(min = 36.dp).background(colors.surfaceContainerHighest, ShapeFull).padding(horizontal = StriveTheme.spacing.s12),
                    Arrangement.spacedBy(2.dp), Alignment.CenterVertically,
                ) {
                    Text(stringResource(Res.string.bar_pill, kilos(bar, swedish)), style = type.titleSmall.board(), color = colors.primary, maxLines = 1, softWrap = false)
                    Icon(StriveIcons.Down, null, Modifier.size(14.dp), tint = colors.primary)
                }
            }
            GlassMenu(open, { open = false }) {
                // A bar the gym's list lacks (10 kg on some seeded barbells) leads, so it shows checked, as WO-12's pull-down does.
                for (b in (listOfNotNull(bar.takeIf { it !in bars }) + bars + null).distinct()) {
                    MenuRow(barText(b, unit), { open = false; onBar(b) }, checked = b == bar)
                }
            }
        }
    }
}
