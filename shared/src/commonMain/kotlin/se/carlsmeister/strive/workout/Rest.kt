package se.carlsmeister.strive.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.custom_time
import strive.shared.generated.resources.free_rest
import strive.shared.generated.resources.rest
import strive.shared.generated.resources.rest_longer
import strive.shared.generated.resources.rest_shorter
import strive.shared.generated.resources.skip

/**
 * The T5 rest card in glass (Q7, Q17; 16-components "Rest timer", K2): a ring that drains in amber, the time left, − and + by 15 s and
 * Hoppa över. It starts when a set is logged. The lock-screen notification and the Live Activity are #43's.
 */
@Composable
internal fun RestCard(rest: Rest, now: Long, haze: HazeState, onChange: (Rest?) -> Unit, modifier: Modifier = Modifier) {
    val left = rest.left(now)
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(left == 0) {
        if (left == 0) {
            // A buzz only if it ran out just now, not for a rest that ended while the workout was minimised.
            if (now - rest.startedAt - rest.seconds * 1_000L < 2_000) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onChange(null)
        }
    }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxWidth().padding(horizontal = StriveTheme.spacing.s16).clip(MaterialTheme.shapes.large)
            .glass(haze, StriveTheme.glass, thick = true).padding(StriveTheme.spacing.s16).testTag("rest-card"),
        verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s16)) {
            val track = colors.surfaceContainerHighest
            val fill = colors.tertiary
            val share = if (rest.seconds == 0) 0f else left / rest.seconds.toFloat()
            Canvas(Modifier.size(StriveTheme.sizes.ringSmall)) {
                val w = size.width * 12 / 120
                val inset = w / 2 + size.width * 10 / 120
                val box = Size(size.width - 2 * inset, size.height - 2 * inset)
                drawArc(track, 0f, 360f, false, Offset(inset, inset), box, style = Stroke(w))
                drawArc(fill, -90f, 360f * share, false, Offset(inset, inset), box, style = Stroke(w, cap = StrokeCap.Round))
            }
            Column {
                Text(stringResource(Res.string.rest), style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant)
                Text(clock(left.toLong()), style = MaterialTheme.typography.displaySmall)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
            RoundKey("−", stringResource(Res.string.rest_shorter)) { onChange(rest.copy(seconds = (rest.seconds - 15).coerceAtLeast(0))) }
            Text("15 s", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            RoundKey("+", stringResource(Res.string.rest_longer)) { onChange(rest.copy(seconds = rest.seconds + 15)) }
            Spacer(Modifier.weight(1f))
            Btn({ onChange(null) }, Modifier.height(StriveTheme.sizes.touch), container = colors.surfaceContainerHighest, content = colors.primary, shape = ShapeFull) {
                Text(stringResource(Res.string.skip), Modifier.padding(horizontal = StriveTheme.spacing.s16), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun RoundKey(glyph: String, label: String, onClick: () -> Unit) = Box(
    Modifier.size(StriveTheme.sizes.touch).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .clickable(onClickLabel = label, onClick = onClick).semantics { contentDescription = label },
    contentAlignment = Alignment.Center,
) { Text(glyph, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }

/** F1's ⏱ in the title bar: a free rest of 0:30, 1:00, 2:00 or 3:00, or a time of your own (Q17). */
@Composable
internal fun RestButton(onStart: (Int) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    var custom by remember { mutableStateOf(false) }
    val label = stringResource(Res.string.free_rest)
    Box {
        Box(
            Modifier.size(StriveTheme.sizes.touch).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClickLabel = label) { menu = true }.semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) { Glyph(Glyph.Timer, 20.dp, color = MaterialTheme.colorScheme.onSurface) }
        DropdownMenu(menu, { menu = false }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            for (s in listOf(30, 60, 120, 180)) DropdownMenuItem({ Text(clock(s.toLong())) }, onClick = { menu = false; onStart(s) })
            DropdownMenuItem({ Text(stringResource(Res.string.custom_time)) }, onClick = { menu = false; custom = true })
        }
    }
    if (custom) {
        TextPrompt(stringResource(Res.string.custom_time), "", onSave = { t ->
            custom = false
            restSeconds(t.filter { it.isDigit() || it == ':' })?.takeIf { it > 0 }?.let(onStart)
        }, onDismiss = { custom = false })
    }
}