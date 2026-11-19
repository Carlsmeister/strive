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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.WeightUnit
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.Load
import se.carlsmeister.strive.data.load
import se.carlsmeister.strive.data.weightOf
import se.carlsmeister.strive.data.loggable
import se.carlsmeister.strive.data.plateKeys
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.Segments
import se.carlsmeister.strive.theme.glass
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.close
import strive.shared.generated.resources.digits_key
import strive.shared.generated.resources.plate_add
import strive.shared.generated.resources.plate_missing
import strive.shared.generated.resources.plate_remove
import strive.shared.generated.resources.plates_empty
import strive.shared.generated.resources.plates_key
import strive.shared.generated.resources.field_metres
import strive.shared.generated.resources.field_reps
import strive.shared.generated.resources.field_seconds
import strive.shared.generated.resources.field_weight
import strive.shared.generated.resources.field_weight_step
import strive.shared.generated.resources.hide
import strive.shared.generated.resources.log
import strive.shared.generated.resources.log_next
import strive.shared.generated.resources.next
import strive.shared.generated.resources.rir
import strive.shared.generated.resources.rest
import strive.shared.generated.resources.save
import strive.shared.generated.resources.save_change
import strive.shared.generated.resources.dock_drop
import strive.shared.generated.resources.dock_rest_after
import strive.shared.generated.resources.dock_set
import strive.shared.generated.resources.dock_warmup

/** The values a set of each type records, in the order Nästa walks them (Q16). Weight's label depends on the exercise (#58). */
private enum class Value(val label: StringResource?, val decimals: Boolean) {
    Weight(null, true), Reps(Res.string.field_reps, false),
    Seconds(Res.string.field_seconds, false), Metres(Res.string.field_metres, true),
    /** The rest after the set, last in Nästa's order (Q16, Q17). */
    Rest(Res.string.rest, false),
}

private fun values(type: ExerciseType) = when (type) {
    ExerciseType.WeightReps -> listOf(Value.Weight, Value.Reps)
    ExerciseType.BodyweightReps, ExerciseType.Reps -> listOf(Value.Reps)
    ExerciseType.Time -> listOf(Value.Seconds)
    ExerciseType.Distance -> listOf(Value.Metres)
} + Value.Rest

/** The rest field's place in the dock, last after the values (Q16, Q17). */
internal fun restField(type: ExerciseType) = values(type).lastIndex

internal fun ExerciseType.hasRir() = this == ExerciseType.WeightReps || this == ExerciseType.BodyweightReps || this == ExerciseType.Reps

private fun WorkoutSet.read(v: Value) = when (v) {
    Value.Weight -> weight
    Value.Reps -> reps?.toDouble()
    Value.Seconds -> seconds?.toDouble()
    Value.Metres -> metres
    Value.Rest -> rest?.toDouble()
}

private fun WorkoutSet.write(v: Value, d: Double?) = when (v) {
    Value.Weight -> copy(weight = d)
    Value.Reps -> copy(reps = d?.toInt())
    Value.Seconds -> copy(seconds = d?.toInt())
    Value.Metres -> copy(metres = d)
    Value.Rest -> copy(rest = d?.toInt())
}

private fun Value.parse(f: Field) = if (this == Value.Rest) restSeconds(f.text)?.toDouble() else f.value

/**
 * The logging dock (#17 S3, D3, K3): closed until a set is tapped, closed again by its grabber or a tap outside. The fields show
 * − and + until one is tapped; then the keypad takes over and Nästa walks the fields, becoming Logga › on the last (Q16). Opened
 * from a rest line it changes without logging: [logs] false, and its button says Spara. With a bar, the bar line loads the weight field
 * from the gym's [plates] and its menu changes the bar through [onBar] (#48, WO-04-new).
 */
@Composable
internal fun Dock(exercise: SessionExercise, unit: WeightUnit, set: WorkoutSet, defaultRest: Int, haze: HazeState, onSave: (WorkoutSet) -> Unit, onClose: () -> Unit,
                  plates: List<Double>, onBar: (Double?) -> Unit, field: Int? = null, logs: Boolean = true) {
    val space = StriveTheme.spacing
    val swedish = Locale.current.language == "sv"
    val separator = if (swedish) ',' else '.'
    val fields = values(exercise.type)
    val close = stringResource(Res.string.close)
    val restShown = Field(clock(restOf(set, defaultRest).toLong()))
    var texts by remember(set.id) {
        mutableStateOf(fields.map { v -> if (v == Value.Rest) restShown else Field(set.read(v)?.let { kilos(it, swedish) } ?: "") })
    }
    var focus by remember(set.id) { mutableStateOf(field) }
    var rir by remember(set.id) { mutableStateOf(set.rir) }
    var more by remember(set.id) { mutableStateOf((set.rir ?: 0) >= 4) }
    // Ruling 8 (#48): the plates keyed per side in plate mode, innermost first; null outside it. Another field or another bar leaves plate mode.
    var keyed by remember(set.id, focus, exercise.bar) { mutableStateOf<List<Double>?>(null) }

    fun stepOf(v: Value) = when (v) {
        Value.Weight -> exercise.step ?: 2.5
        Value.Reps -> 1.0
        Value.Seconds -> 5.0
        Value.Metres -> 50.0
        Value.Rest -> 15.0
    }

    // Ruling 2 (#58): the weight always names its unit, and names the step too when no bar line shows it.
    val unitText = stringResource(unit.label)
    val weight = if (exercise.bar == null) stringResource(Res.string.field_weight_step, unitText, kilos(stepOf(Value.Weight), swedish))
        else stringResource(Res.string.field_weight, unitText)

    fun edit(i: Int, f: Field) {
        texts = texts.toMutableList().also { it[i] = f }
    }

    fun step(i: Int, by: Int) {
        val v = fields[i]
        edit(i, if (v == Value.Rest) restStep(texts[i], by * 15) else texts[i].step(by * stepOf(v), separator))
    }

    fun log() {
        val filled = fields.foldIndexed(set) { i, s, v -> s.write(v, v.parse(texts[i])) }
            // An untouched rest stays the default, so a new default in Settings still reaches it.
            .let { if (texts.last().text == restShown.text) it.copy(rest = set.rest) else it }
            .copy(rir = rir, done = true)
        // Something missing: go to the first empty field instead of logging half a set.
        if (!logs) onSave(filled.copy(done = set.done)) else if (filled.loggable(exercise.type)) onSave(filled) else focus = fields.indices.first { fields[it] != Value.Rest && texts[it].value == null }
    }

    @Composable
    fun field(i: Int, modifier: Modifier, steppers: Boolean) = Column(modifier, verticalArrangement = Arrangement.spacedBy(space.s4)) {
        val v = fields[i]
        val label = v.label?.let { stringResource(it) } ?: weight
        Text(label, style = MaterialTheme.typography.labelMedium.board(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        NumberField(
            label, texts[i].text, focused = focus == i, fresh = texts[i].fresh, steppers = steppers, plain = keyed != null,
            onTap = { focus = i; edit(i, texts[i].copy(fresh = true)) },
            onStep = { by -> step(i, by) },
        )
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
            .glass(haze, StriveTheme.glass, thick = true, shape = DockShape)
            // Taps on the dock itself stay in it; only a tap outside closes it. Not clickable, which would merge the dock into one
            // element for VoiceOver and hide the grabber's Stäng behind an empty action.
            .pointerInput(Unit) { detectTapGestures { } }
            .navigationBarsPadding()
            .padding(horizontal = space.s16).padding(bottom = space.s16)
            .animateContentSize(if (StriveTheme.reduceMotion) Motion.fade() else Motion.settle()),
        verticalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        // `.dk-title`'s margin-top: -6px sits the title 6 under the grabber, inside the dock's 12 gap.
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(space.s12 - 6.dp)) {
            Box(
                Modifier.size(StriveTheme.sizes.touch, space.s24)
                    .draggable(dragState, Orientation.Vertical, onDragStopped = { velocity ->
                        if (drag.value > height / 3 || velocity > 1000f) onClose() else drag.animateTo(0f, Motion.settle())
                    })
                    // VoiceOver cannot drag a grabber; it gets Stäng as the grabber's action instead.
                    .semantics { contentDescription = close; onClick { onClose(); true } },
                contentAlignment = Alignment.Center,
            ) { Box(Modifier.size(36.dp, 5.dp).background(MaterialTheme.colorScheme.outline, ShapeFull)) }

            // P7. A warm-up or drop set has no number, so the dock names its kind, also from the rest line after it.
            val n = numberAsWorking(exercise.sets, exercise.sets.indexOfFirst { it.id == set.id })
            val title = when {
                set.kind == SetKind.Warmup -> stringResource(Res.string.dock_warmup, exercise.name)
                set.kind == SetKind.Drop -> stringResource(Res.string.dock_drop, exercise.name)
                !logs -> stringResource(Res.string.dock_rest_after, exercise.name, n)
                else -> stringResource(Res.string.dock_set, exercise.name, n)
            }
            Text(
                title, Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium.board(), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            )
        }

        val f = focus
        val rest = fields.lastIndex
        // The keypad open: rest is the third field, where Nästa goes. Closed: RIR and rest share a row (#17, addition to choice 2).
        Row(horizontalArrangement = Arrangement.spacedBy(space.s12)) {
            for (i in fields.indices) if (f != null || i != rest) field(i, Modifier.weight(1f), steppers = f == null)
        }

        val bar = exercise.bar
        if (bar != null && exercise.type == ExerciseType.WeightReps) {
            // An empty or unparsable weight loads as the bar alone; a partly typed one loads as the number it reads so far.
            val weight = texts[fields.indexOf(Value.Weight)].value ?: bar
            val fewest = remember(weight, bar, plates) { load(weight, bar, plates) }
            // In plate mode the sleeve draws the plates as keyed, the last one keyed `primary`.
            BarLine(keyed?.let { Load.Plates(it) } ?: fewest, unit.plateKeys, bar, unit, newest = keyed?.lastIndex, onBar = onBar)
        }

        if (f == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(space.s8), verticalAlignment = Alignment.Bottom) {
                if (exercise.type.hasRir()) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(space.s4)) {
                        Text(stringResource(Res.string.rir), style = MaterialTheme.typography.labelMedium.board(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // The board's inline `height: 52px`, level with the rest field beside it.
                        RirSegments(rir, more, onRir = { rir = it }, onMore = { more = it }, Modifier.height(52.dp))
                    }
                }
                field(rest, Modifier.weight(1f), steppers = true)
            }
            // The 4 to 10 row stays open on the value picked (WO-04-rir4); 4+ closes it.
            if (more) RirMore(rir, onRir = { rir = it }, onMore = {})
            Button(::log, Modifier.fillMaxWidth().heightIn(min = ButtonHeight), shape = MaterialTheme.shapes.large) {
                Text(stringResource(if (!logs) Res.string.save else if (set.done) Res.string.save_change else Res.string.log))
            }
        } else {
            val v = fields[f]
            // Plates are keyed on the weight of an exercise with a bar.
            val plateBar = exercise.bar?.takeIf { v == Value.Weight && exercise.type == ExerciseType.WeightReps }
            fun seed(bar: Double) = (load(texts[f].value ?: bar, bar, plates) as? Load.Plates)?.perSide ?: emptyList()
            fun keyPlates(list: List<Double>) {
                val bar = plateBar ?: return
                keyed = list
                edit(f, Field(kilos(weightOf(bar, list), swedish), fresh = false))
            }
            Keypad(
                separator = separator.takeIf { v.decimals },
                plateKeys = unit.plateKeys.takeIf { plateBar != null },
                gym = plates,
                unit = unitText,
                plateMode = keyed != null,
                onPlateMode = { on -> keyed = if (on && plateBar != null) seed(plateBar) else null },
                onPlate = { p -> keyPlates(keyed.orEmpty() + p) },
                onPlateBack = { keyPlates(keyed.orEmpty().dropLast(1)) },
                onEmpty = { keyPlates(emptyList()) },
                onKey = { k -> edit(f, if (v == Value.Rest) restKey(texts[f], k) else texts[f].key(k, v.decimals, separator)) },
                // A step in plate mode reloads the stepped weight with the fewest plates, so the sleeve keeps drawing the field.
                onStep = { by -> step(f, by); if (keyed != null && plateBar != null) keyed = seed(plateBar) },
                onHide = { focus = null },
            )
            val last = f == fields.lastIndex
            Button(
                { if (last) log() else focus = f + 1 }, Modifier.fillMaxWidth().heightIn(min = ButtonHeight),
                shape = MaterialTheme.shapes.large,
            ) { Text(stringResource(if (!last) Res.string.next else if (logs) Res.string.log_next else Res.string.save)) }
        }
    }
}

/**
 * `.nf`: a field 52 tall on surface-container-highest, ringed in primary while focused (#16 I3); − and + beside the number while the
 * keypad is closed. Focused and fresh, the number sits on a primary wash, as the first key replaces it (`.v.fresh`); focused and typed
 * into, a caret follows it. [plain] shows neither: the plate keys write the number whole (WO-05-plates).
 */
@Composable
private fun NumberField(
    label: String, text: String, focused: Boolean, fresh: Boolean, steppers: Boolean, onTap: () -> Unit, onStep: (Int) -> Unit, plain: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val space = StriveTheme.spacing
    Row(
        Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch + 8.dp)
            .background(colors.surfaceContainerHighest, MaterialTheme.shapes.medium)
            .border(StriveTheme.borders.focus, if (focused) colors.primary else Color.Transparent, MaterialTheme.shapes.medium)
            // The whole 52 tall field takes the tap and carries the label; − and + inside it keep their own targets.
            .clickable(onClick = onTap).semantics { contentDescription = label }
            // CSS's 2 border sits outside the 4 padding, transparent while not focused.
            .padding(space.s4 + StriveTheme.borders.focus),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (steppers) StepKey("−", "$label −") { onStep(-1) }
        Row(
            Modifier.weight(1f)
                .then(if (focused && fresh && !plain) Modifier.padding(horizontal = 6.dp).background(colors.primary.copy(alpha = .28f), RoundedCornerShape(6.dp)) else Modifier)
                .padding(vertical = space.s4),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text.ifEmpty { "–" }, Modifier.alignByBaseline(), style = MaterialTheme.typography.titleLarge.board(),
                color = if (text.isEmpty()) colors.onSurfaceVariant else colors.onSurface,
            )
            // `.caret`: 2 × 20 in primary, 1 after the number, its foot 4 under the baseline (`vertical-align: -4px`).
            // Drawn still: the screen keeps one clock (#42).
            val foot = with(LocalDensity.current) { 4.dp.roundToPx() }
            if (focused && !fresh && !plain) Box(
                Modifier.alignBy { it.measuredHeight - foot }.padding(start = 1.dp).size(2.dp, 20.dp).background(colors.primary),
            )
        }
        if (steppers) StepKey("+", "$label +") { onStep(1) }
    }
}

/** `.stepk`: drawn 36 round, tapped across 44 around it without moving anything drawn. */
@Composable
private fun StepKey(label: String, description: String, onClick: () -> Unit) {
    val touch = StriveTheme.sizes.touch
    Box(
        Modifier.layout { m, _ ->
            val tap = m.measure(Constraints.fixed(touch.roundToPx(), touch.roundToPx()))
            val drawn = 36.dp.roundToPx()
            layout(drawn, drawn) { tap.place((drawn - tap.width) / 2, (drawn - tap.height) / 2) }
        }.clickable(onClick = onClick).semantics { contentDescription = description }
            .padding((touch - 36.dp) / 2).background(MaterialTheme.colorScheme.surface, ShapeFull),
        contentAlignment = Alignment.Center,
    ) { Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
}

/** RIR 0 · 1 · 2 · 3 · 4+; 4+ opens 4 to 10, and then shows the value picked (#17, change to Q13.3). */
@Composable
internal fun RirRow(rir: Int?, more: Boolean, onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit, label: Boolean = true, compact: Boolean = false) {
    val space = StriveTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(space.s4)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space.s8)) {
            if (label) Text(stringResource(Res.string.rir), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            RirSegments(rir, more, onRir, onMore, Modifier.weight(1f), compact)
        }
        if (more) RirMore(rir, onRir, onMore, compact)
    }
}

@Composable
internal fun RirSegments(rir: Int?, more: Boolean, onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
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
        modifier = modifier, compact = compact,
    )
}

@Composable
internal fun RirMore(rir: Int?, onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit, compact: Boolean = false) =
    Segments((4..10).map { "$it" }, selected = rir?.minus(4)?.takeIf { it >= 0 }, onSelect = { i -> onRir(i?.plus(4)); onMore(false) },
        modifier = Modifier.fillMaxWidth(), compact = compact)

/**
 * K3 (#17 choice 2): small actions on top, digits on glass keys. With [plateKeys] (the weight of an exercise with a bar), Plates swaps the
 * digits for the unit's plate keys, ⌫ and Empty, and 123 swaps them back (#48, WO-05-plates); a plate not in the [gym] is greyed.
 */
@Composable
private fun Keypad(
    separator: Char?, plateKeys: List<Double>?, gym: List<Double>, unit: String, plateMode: Boolean, onPlateMode: (Boolean) -> Unit,
    onPlate: (Double) -> Unit, onPlateBack: () -> Unit, onEmpty: () -> Unit, onKey: (Char) -> Unit, onStep: (Int) -> Unit, onHide: () -> Unit,
) {
    val space = StriveTheme.spacing
    val type = MaterialTheme.typography
    val swedish = Locale.current.language == "sv"
    Column(verticalArrangement = Arrangement.spacedBy(space.s8)) {
        Row(horizontalArrangement = Arrangement.spacedBy(space.s8), verticalAlignment = Alignment.CenterVertically) {
            // `.kk.small`: at least 88 wide.
            val small = Modifier.widthIn(min = StriveTheme.sizes.touch * 2)
            Key(stringResource(Res.string.hide), small, style = type.labelMedium, onClick = onHide)
            if (plateKeys != null) {
                Key(stringResource(if (plateMode) Res.string.digits_key else Res.string.plates_key), small, style = type.labelMedium) { onPlateMode(!plateMode) }
            }
            Spacer(Modifier.weight(1f))
            Key("−", Modifier.width(StriveTheme.sizes.touch + space.s16), tinted = true) { onStep(-1) }
            Key("+", Modifier.width(StriveTheme.sizes.touch + space.s16), tinted = true) { onStep(1) }
        }
        if (plateKeys != null && plateMode) {
            // `.kp .digits.plates`: four columns, the plates heaviest first then ⌫ (null), and Empty across all four.
            for (row in (plateKeys + null).chunked(4)) {
                Row(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
                    for (p in row) {
                        if (p == null) Key("⌫", Modifier.weight(1f), style = type.titleMedium, glass = true, description = stringResource(Res.string.plate_remove), onClick = onPlateBack)
                        else {
                            val text = kilos(p, swedish)
                            val has = p in gym
                            Key(
                                text, Modifier.weight(1f), style = type.titleMedium, glass = true, enabled = has,
                                description = stringResource(if (has) Res.string.plate_add else Res.string.plate_missing, text, unit),
                            ) { onPlate(p) }
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Key(stringResource(Res.string.plates_empty), Modifier.fillMaxWidth(), style = type.titleMedium, glass = true, onClick = onEmpty)
        } else {
            val rows = listOf("123", "456", "789", "${separator ?: ' '}0⌫")
            for (row in rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
                    for (k in row) {
                        if (k == ' ') Spacer(Modifier.weight(1f))
                        else Key(k.toString(), Modifier.weight(1f), glass = true) { onKey(k) }
                    }
                }
            }
        }
    }
}

/** A keypad key; [glass] is `.kp .digits .kk`: thick glass, edged by a hairline in the material's edge colour. A key not [enabled] is greyed. */
@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    tinted: Boolean = false,
    glass: Boolean = false,
    enabled: Boolean = true,
    description: String? = null,
    onClick: () -> Unit,
) = Box(
    modifier.height(StriveTheme.sizes.touch).then(if (enabled) Modifier else Modifier.alpha(StriveTheme.states.disabledOpacity))
        .then(
            if (glass) Modifier.border(StriveTheme.borders.hairline, StriveTheme.glass.edge, ShapeFull).background(StriveTheme.glass.thick, ShapeFull)
            else Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium),
        )
        .clickable(enabled = enabled, onClick = onClick)
        .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
        .testTag("key-$label"),
    contentAlignment = Alignment.Center,
) {
    Text(label, style = style, color = if (tinted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
}

/**
 * The dock's 28 dp top corners. `.dock` keeps only `.glass`'s top border, so the outline runs 1 dp past the sides and the foot:
 * glass draws its hairline inside the outline, and there it falls off screen everywhere but the top.
 */
internal val DockShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { 28.dp.toPx() }
        val e = with(density) { 1.dp.toPx() }
        return Outline.Generic(Path().apply {
            moveTo(-e, size.height + e)
            lineTo(-e, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            arcTo(Rect(size.width - 2 * r, 0f, size.width, 2 * r), 270f, 90f, false)
            lineTo(size.width + e, r)
            lineTo(size.width + e, size.height + e)
            close()
        })
    }
}

/** `.btn`'s min-height. */
private val ButtonHeight = 50.dp