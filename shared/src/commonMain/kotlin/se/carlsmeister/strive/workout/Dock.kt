package se.carlsmeister.strive.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.loggable
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.close
import strive.shared.generated.resources.field_metres
import strive.shared.generated.resources.field_reps
import strive.shared.generated.resources.field_seconds
import strive.shared.generated.resources.field_weight
import strive.shared.generated.resources.hide
import strive.shared.generated.resources.log
import strive.shared.generated.resources.log_next
import strive.shared.generated.resources.next
import strive.shared.generated.resources.rir
import strive.shared.generated.resources.save_change
import strive.shared.generated.resources.set_number

/** The values a set of each type records, in the order Nästa walks them (Q16). */
private enum class Value(val label: StringResource, val decimals: Boolean) {
    Weight(Res.string.field_weight, true), Reps(Res.string.field_reps, false),
    Seconds(Res.string.field_seconds, false), Metres(Res.string.field_metres, true),
}

private fun values(type: ExerciseType) = when (type) {
    ExerciseType.WeightReps -> listOf(Value.Weight, Value.Reps)
    ExerciseType.BodyweightReps, ExerciseType.Reps -> listOf(Value.Reps)
    ExerciseType.Time -> listOf(Value.Seconds)
    ExerciseType.Distance -> listOf(Value.Metres)
}

private fun ExerciseType.hasRir() = this == ExerciseType.WeightReps || this == ExerciseType.BodyweightReps || this == ExerciseType.Reps

private fun WorkoutSet.read(v: Value) = when (v) {
    Value.Weight -> weight
    Value.Reps -> reps?.toDouble()
    Value.Seconds -> seconds?.toDouble()
    Value.Metres -> metres
}

private fun WorkoutSet.write(v: Value, d: Double?) = when (v) {
    Value.Weight -> copy(weight = d)
    Value.Reps -> copy(reps = d?.toInt())
    Value.Seconds -> copy(seconds = d?.toInt())
    Value.Metres -> copy(metres = d)
}

/**
 * The logging dock (#17 S3, D3, K3): closed until a set is tapped, closed again by its grabber or a tap outside. The fields show
 * − and + until one is tapped; then the keypad takes over and Nästa walks the fields, becoming Logga › on the last (Q16).
 */
@Composable
internal fun Dock(exercise: SessionExercise, set: WorkoutSet, number: Int, haze: HazeState, onSave: (WorkoutSet) -> Unit, onClose: () -> Unit) {
    val space = StriveTheme.spacing
    val swedish = Locale.current.language == "sv"
    val separator = if (swedish) ',' else '.'
    val fields = values(exercise.type)
    val close = stringResource(Res.string.close)
    var texts by remember(set.id) { mutableStateOf(fields.map { v -> Field(set.read(v)?.let { kilos(it, swedish) } ?: "") }) }
    var focus by remember(set.id) { mutableStateOf<Int?>(null) }
    var rir by remember(set.id) { mutableStateOf(set.rir) }
    var more by remember(set.id) { mutableStateOf((set.rir ?: 0) >= 4) }

    fun stepOf(v: Value) = when (v) {
        Value.Weight -> exercise.step ?: 2.5
        Value.Reps -> 1.0
        Value.Seconds -> 5.0
        Value.Metres -> 50.0
    }

    fun edit(i: Int, f: Field) {
        texts = texts.toMutableList().also { it[i] = f }
    }

    fun log() {
        val filled = fields.foldIndexed(set) { i, s, v -> s.write(v, texts[i].value) }.copy(rir = rir, done = true)
        // Something missing: go to the first empty field instead of logging half a set.
        if (filled.loggable(exercise.type)) onSave(filled) else focus = fields.indices.first { texts[it].value == null }
    }

    // The grabber drags the dock down; let go past a third of the way, or flick, and it closes.
    val drag = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var height by remember { mutableStateOf(1f) }
    val dragState = rememberDraggableState { delta -> scope.launch { drag.snapTo((drag.value + delta).coerceAtLeast(0f)) } }

    Column(
        Modifier.fillMaxWidth()
            .onSizeChanged { height = it.height.toFloat() }
            .graphicsLayer { translationY = drag.value }
            .clip(DockShape)
            .glass(haze, StriveTheme.glass, thick = true)
            // Taps on the dock itself stay in it; only a tap outside closes it. Not clickable, which would merge the dock into one
            // element for VoiceOver and hide the grabber's Stäng behind an empty action.
            .pointerInput(Unit) { detectTapGestures { } }
            .navigationBarsPadding()
            .padding(horizontal = space.s16).padding(bottom = space.s16)
            .animateContentSize(if (StriveTheme.reduceMotion) Motion.fade() else Motion.settle()),
        verticalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        Box(
            Modifier.align(Alignment.CenterHorizontally).size(StriveTheme.sizes.touch, space.s24)
                .draggable(dragState, Orientation.Vertical, onDragStopped = { velocity ->
                    if (drag.value > height / 3 || velocity > 1000f) onClose() else drag.animateTo(0f, Motion.settle())
                })
                // VoiceOver cannot drag a grabber; it gets Stäng as the grabber's action instead.
                .semantics { contentDescription = close; onClick { onClose(); true } },
            contentAlignment = Alignment.Center,
        ) { Box(Modifier.size(36.dp, 5.dp).background(MaterialTheme.colorScheme.outline, ShapeFull)) }

        Text(
            "${exercise.name} · ${stringResource(Res.string.set_number, number)}", Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(space.s12)) {
            fields.forEachIndexed { i, v ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(space.s4)) {
                    Text(stringResource(v.label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    NumberField(
                        stringResource(v.label), texts[i].text, focused = focus == i, steppers = focus == null,
                        onTap = { focus = i; edit(i, texts[i].copy(fresh = true)) },
                        onStep = { by -> edit(i, texts[i].step(by * stepOf(v), separator)) },
                    )
                }
            }
        }

        val f = focus
        if (f == null) {
            if (exercise.type.hasRir()) RirRow(rir, more, onRir = { rir = it }, onMore = { more = it })
            Button(::log, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch), shape = MaterialTheme.shapes.large) {
                Text(stringResource(if (set.done) Res.string.save_change else Res.string.log))
            }
        } else {
            val v = fields[f]
            Keypad(
                separator = separator.takeIf { v.decimals },
                onKey = { k -> edit(f, texts[f].key(k, v.decimals, separator)) },
                onStep = { by -> edit(f, texts[f].step(by * stepOf(v), separator)) },
                onHide = { focus = null },
            )
            val last = f == fields.lastIndex
            Button(
                { if (last) log() else focus = f + 1 }, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch),
                shape = MaterialTheme.shapes.large,
            ) { Text(stringResource(if (last) Res.string.log_next else Res.string.next)) }
        }
    }
}

/** A grey field with the number large (#16 I3); − and + beside it while the keypad is closed. */
@Composable
private fun NumberField(label: String, text: String, focused: Boolean, steppers: Boolean, onTap: () -> Unit, onStep: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch + 8.dp)
            .background(colors.surfaceContainerHighest, MaterialTheme.shapes.medium)
            .border(StriveTheme.borders.focus, if (focused) colors.primary else Color.Transparent, MaterialTheme.shapes.medium)
            .padding(StriveTheme.spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (steppers) StepKey("−", "$label −") { onStep(-1) }
        Text(
            text.ifEmpty { "–" },
            Modifier.weight(1f).clickable(onClick = onTap).semantics { contentDescription = label }.padding(vertical = StriveTheme.spacing.s4),
            style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
            color = if (text.isEmpty()) colors.onSurfaceVariant else colors.onSurface,
        )
        if (steppers) StepKey("+", "$label +") { onStep(1) }
    }
}

@Composable
private fun StepKey(label: String, description: String, onClick: () -> Unit) = Box(
    Modifier.size(36.dp).background(MaterialTheme.colorScheme.surface, ShapeFull).clickable(onClick = onClick)
        .semantics { contentDescription = description },
    contentAlignment = Alignment.Center,
) { Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }

/** RIR 0 · 1 · 2 · 3 · 4+; 4+ opens 4 to 10, and then shows the value picked (#17, change to Q13.3). */
@Composable
private fun RirRow(rir: Int?, more: Boolean, onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit) {
    val space = StriveTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(space.s4)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space.s8)) {
            Text(stringResource(Res.string.rir), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val high = rir != null && rir >= 4
            Segments(
                listOf("0", "1", "2", "3", if (high) "$rir" else "4+"), selected = if (high) 4 else rir,
                onSelect = { i ->
                    when {
                        i == 4 -> onMore(!more)
                        i == null && high -> { onRir(null); onMore(false) }
                        else -> { onRir(i); onMore(false) }
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        if (more) {
            Segments((4..10).map { "$it" }, selected = rir?.minus(4)?.takeIf { it >= 0 }, onSelect = { i -> onRir(i?.plus(4)); onMore(false) })
        }
    }
}

/** K3 (#17 choice 2): small actions on top, digits on glass keys. Skivor joins with the plates (#48). */
@Composable
private fun Keypad(separator: Char?, onKey: (Char) -> Unit, onStep: (Int) -> Unit, onHide: () -> Unit) {
    val space = StriveTheme.spacing
    val glass = StriveTheme.glass.thick
    Column(verticalArrangement = Arrangement.spacedBy(space.s8)) {
        Row(horizontalArrangement = Arrangement.spacedBy(space.s8), verticalAlignment = Alignment.CenterVertically) {
            Key(stringResource(Res.string.hide), Modifier.widthIn(min = StriveTheme.sizes.touch * 2), small = true, onClick = onHide)
            Spacer(Modifier.weight(1f))
            Key("−", Modifier.width(StriveTheme.sizes.touch + space.s16), tinted = true) { onStep(-1) }
            Key("+", Modifier.width(StriveTheme.sizes.touch + space.s16), tinted = true) { onStep(1) }
        }
        val rows = listOf("123", "456", "789", "${separator ?: ' '}0⌫")
        for (row in rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
                for (k in row) {
                    if (k == ' ') Spacer(Modifier.weight(1f))
                    else Key(k.toString(), Modifier.weight(1f), background = glass, shape = ShapeFull) { onKey(k) }
                }
            }
        }
    }
}

@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    small: Boolean = false,
    tinted: Boolean = false,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    onClick: () -> Unit,
) = Box(
    modifier.height(StriveTheme.sizes.touch).background(background, shape).clickable(onClick = onClick).testTag("key-$label"),
    contentAlignment = Alignment.Center,
) {
    Text(
        label, style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleLarge,
        color = if (tinted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
    )
}

internal val DockShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)