package se.carlsmeister.strive.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.SetKind
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.add_named
import strive.shared.generated.resources.back_to_focus
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.change_start
import strive.shared.generated.resources.col_kg
import strive.shared.generated.resources.col_metres
import strive.shared.generated.resources.col_previous
import strive.shared.generated.resources.col_reps
import strive.shared.generated.resources.col_seconds
import strive.shared.generated.resources.col_set
import strive.shared.generated.resources.down_by
import strive.shared.generated.resources.edit
import strive.shared.generated.resources.expand
import strive.shared.generated.resources.kind_drop
import strive.shared.generated.resources.kind_failure
import strive.shared.generated.resources.kind_normal
import strive.shared.generated.resources.kind_warmup
import strive.shared.generated.resources.log_set
import strive.shared.generated.resources.logged
import strive.shared.generated.resources.months_short
import strive.shared.generated.resources.now_kind
import strive.shared.generated.resources.now_set
import strive.shared.generated.resources.now_strip
import strive.shared.generated.resources.quick_marked
import strive.shared.generated.resources.quick_picks
import strive.shared.generated.resources.quick_recent
import strive.shared.generated.resources.rename
import strive.shared.generated.resources.rest_after
import strive.shared.generated.resources.rest_row
import strive.shared.generated.resources.save
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.set_type
import strive.shared.generated.resources.sets_progress
import strive.shared.generated.resources.skipped
import strive.shared.generated.resources.up_by
import strive.shared.generated.resources.weekdays_short
import strive.shared.generated.resources.what_today
import strive.shared.generated.resources.what_today_body
import strive.shared.generated.resources.workout_options

/** The columns a set row shows after Set and Förra, by what the exercise records. */
internal fun columns(type: ExerciseType): List<StringResource> = when (type) {
    ExerciseType.WeightReps -> listOf(Res.string.col_kg, Res.string.col_reps)
    ExerciseType.BodyweightReps, ExerciseType.Reps -> listOf(Res.string.col_reps)
    ExerciseType.Time -> listOf(Res.string.col_seconds)
    ExerciseType.Distance -> listOf(Res.string.col_metres)
}

/** The values for those columns; null before one is entered. */
internal fun cells(s: WorkoutSet, type: ExerciseType, swedish: Boolean): List<String?> = when (type) {
    ExerciseType.WeightReps -> listOf(s.weight?.let { kilos(it, swedish) }, s.reps?.toString())
    ExerciseType.BodyweightReps, ExerciseType.Reps -> listOf(s.reps?.toString())
    ExerciseType.Time -> listOf(s.seconds?.let { clock(it.toLong()) })
    ExerciseType.Distance -> listOf(s.metres?.let { kilos(it, swedish) })
}

internal val SetKind.label get() = when (this) {
    SetKind.Normal -> Res.string.kind_normal
    SetKind.Warmup -> Res.string.kind_warmup
    SetKind.Drop -> Res.string.kind_drop
    SetKind.Failure -> Res.string.kind_failure
}

private val SetColumn = 36.dp
private val CheckColumn = 44.dp
internal val RowShape = RoundedCornerShape(12.dp)

/** D's grid: weight 64 pt, reps 56; a single value gets the two together. */
private fun cellWidth(count: Int, i: Int) = if (count == 1) 88.dp else if (i == 0) 64.dp else 56.dp

private val swedish @Composable get() = androidx.compose.ui.text.intl.Locale.current.language == "sv"

/** The ⋯ pill D draws beside a name: teal container, the glyph bold. */
@Composable
internal fun MorePill(label: String, large: Boolean, onClick: () -> Unit) = Box(
    Modifier.height(if (large) 30.dp else 28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
        .clickable(onClickLabel = label, onClick = onClick).semantics { contentDescription = label }.padding(horizontal = if (large) 12.dp else 10.dp),
    contentAlignment = Alignment.Center,
) {
    Text("⋯", style = MaterialTheme.typography.titleMedium.copy(fontSize = if (large) 18.sp else 17.sp, lineHeight = 18.sp),
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
}

/** The workout's name with ⋯: Byt namn, Passanteckning and Ändra starttid (Q19). */
@Composable
internal fun WorkoutMenu(w: Workout, large: Boolean, nowMs: Long) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var prompt by remember { mutableStateOf<String?>(null) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (large) 10.dp else 6.dp)) {
        Text(
            w.name, Modifier.weight(1f, fill = false).clickable(onClickLabel = stringResource(Res.string.workout_options)) { menu = true },
            style = if (large) MaterialTheme.typography.headlineMedium.copy(letterSpacing = (-0.4).sp) else MaterialTheme.typography.titleMedium,
            fontWeight = if (large) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Box {
            MorePill(stringResource(Res.string.workout_options), large) { menu = true }
            DropdownMenu(menu, { menu = false }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                DropdownMenuItem({ Text(stringResource(Res.string.rename)) }, onClick = { menu = false; prompt = "name" })
                DropdownMenuItem({ Text(stringResource(Res.string.session_note)) }, onClick = { menu = false; prompt = "note" })
                DropdownMenuItem({ Text(stringResource(Res.string.change_start)) }, onClick = { menu = false; prompt = "start" })
            }
        }
    }
    when (prompt) {
        "name" -> TextPrompt(stringResource(Res.string.rename), w.name, onSave = { n ->
            prompt = null
            if (n.isNotEmpty()) scope.launch { sessions.rename(w.id, n) }
        }, onDismiss = { prompt = null })
        "note" -> TextPrompt(stringResource(Res.string.session_note), w.note, singleLine = false, onSave = { n ->
            prompt = null
            scope.launch { sessions.setNote(w.id, n) }
        }, onDismiss = { prompt = null })
        "start" -> StartPrompt(w.startedAt, onSave = { h, m ->
            prompt = null
            scope.launch { sessions.setStart(w.id, withTime(w.startedAt, h, m, TimeZone.currentSystemDefault(), nowMs)) }
        }, onDismiss = { prompt = null })
    }
}

/** Ändra starttid: the hour and minute on the day the workout started. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartPrompt(startedAt: Long, onSave: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val start = local(startedAt)
    val state = rememberTimePickerState(start.hour, start.minute, is24Hour = true)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s20),
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12), horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(Res.string.change_start), style = MaterialTheme.typography.titleMedium)
            TimeInput(state)
            Row {
                TextButton(onDismiss, Modifier.weight(1f)) { Text(stringResource(Res.string.cancel)) }
                TextButton({ onSave(state.hour, state.minute) }, Modifier.weight(1f)) { Text(stringResource(Res.string.save)) }
            }
        }
    }
}

/** B's large title (decision 40), as D draws it: the name with ⋯, then the day, the time and the sets done, each with its glyph. */
@Composable
internal fun LargeHeader(w: Workout, now: Long) {
    val start = local(w.startedAt)
    val day = "${stringArrayResource(Res.array.weekdays_short)[start.dayOfWeek.isoDayNumber - 1]} ${start.day} ${
        stringArrayResource(Res.array.months_short)[start.month.number - 1]}" // "Mån 2 nov", as D draws it
    val grey = MaterialTheme.colorScheme.onSurfaceVariant
    val style = MaterialTheme.typography.bodyMedium
    Column(Modifier.padding(top = StriveTheme.spacing.s4), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4)) {
        WorkoutMenu(w, large = true, nowMs = now)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Glyph(Glyph.Calendar, 16.dp, color = grey, stroke = 1.8f)
                Text(day, style = style, color = grey)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Glyph(Glyph.Clock, 16.dp, color = grey, stroke = 1.8f)
                Text(clock((now - w.startedAt) / 1_000), style = style)
            }
            Text(stringResource(Res.string.sets_progress, w.logged, w.sets), style = style, color = grey)
        }
    }
}

/** The label over the next set: "NU · BENCH PRESS BB · SET 2 AV 4", or the set's type for a warm-up, drop or failure set. */
@Composable
private fun nowLabel(e: SessionExercise, set: WorkoutSet, badge: String, strip: Boolean): String {
    val working = e.sets.count { it.kind == SetKind.Normal }
    return when {
        set.kind != SetKind.Normal -> stringResource(Res.string.now_kind, e.name, stringResource(set.kind.label))
        strip -> stringResource(Res.string.now_strip, e.name, badge)
        else -> stringResource(Res.string.now_set, e.name, badge, working)
    }.uppercase()
}

/** "upp 2,5 kg" against last time, or nothing (Q21's ↑, until #45's rule). */
@Composable
internal fun changeText(set: WorkoutSet, prev: WorkoutSet?, swedish: Boolean): String? = change(set.weight, prev?.weight)?.let {
    stringResource(if (it > 0) Res.string.up_by else Res.string.down_by, kilos(kotlin.math.abs(it), swedish))
}

/**
 * C's focus card as Carl drew it in D: the label, the next set large with "upp 2,5 kg" under it, the rest after it and last time's
 * set, RIR, Ändra for the dock, and one tap to log it.
 */
@Composable
internal fun FocusCard(e: SessionExercise, set: WorkoutSet, badge: String, prev: WorkoutSet?, rest: Int, rir: Int?, more: Boolean,
                       onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit, onEdit: () -> Unit, onLog: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val sv = swedish
    Column(
        Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(22.dp)).padding(StriveTheme.spacing.s16).testTag("focus"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(nowLabel(e, set, badge, strip = false), style = type.bodySmall.copy(letterSpacing = 0.3.sp), fontWeight = FontWeight.SemiBold,
            color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val big = type.displaySmall.copy(fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-1).sp, fontWeight = FontWeight.Bold)
                val units = columns(e.type)
                cells(set, e.type, sv).forEachIndexed { i, v ->
                    if (i > 0) Text("×", Modifier.alignByBaseline(), style = type.headlineMedium.copy(fontSize = 26.sp), color = colors.onSurfaceVariant)
                    Text(v ?: "–", Modifier.alignByBaseline(), style = big)
                    Text(stringResource(units[i]).lowercase(), Modifier.alignByBaseline(), style = type.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            changeText(set, prev, sv)?.let {
                Text(it, Modifier.offset(y = (-2).dp), style = type.bodySmall, fontWeight = FontWeight.SemiBold, color = colors.primary)
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.rest_after, clock(rest.toLong())), Modifier.alignByBaseline(), style = type.bodySmall, color = colors.onSurfaceVariant)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.onSurfaceVariant)) { append(stringResource(Res.string.col_previous) + " ") }
                        withStyle(SpanStyle(color = colors.onSurface, fontSize = 15.sp)) { append(prev?.let { setText(it, e.type, sv) } ?: "–") }
                    },
                    Modifier.alignByBaseline(), style = type.bodySmall,
                )
            }
        }
        if (e.type.hasRir()) RirRow(rir, more, onRir, onMore, label = false, compact = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn(onEdit, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) {
                Text(stringResource(Res.string.edit), style = type.titleMedium)
            }
            Btn(onLog, Modifier.weight(2f).height(50.dp), container = colors.primary, content = colors.onPrimary, shape = RoundedCornerShape(14.dp)) {
                Glyph(Glyph.Check, 20.dp, stroke = 2.8f)
                Text(stringResource(Res.string.log_set, badge), Modifier.padding(start = 8.dp), style = type.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * The focus card folded into a strip under the bar once it scrolls away (D). The whole strip is one button back to the card; logging
 * stays on the card and the rows, so a tap meant for the bar never logs a set (Carl, point 7).
 */
@Composable
internal fun FocusStrip(e: SessionExercise, set: WorkoutSet, badge: String, rir: Int?, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sv = swedish
    val main = when (e.type) {
        ExerciseType.WeightReps -> "${set.weight?.let { kilos(it, sv) } ?: "–"} kg × ${set.reps ?: "–"}"
        else -> setText(set.copy(rir = null), e.type, sv)
    }
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerHigh)
            .clickable(onClickLabel = stringResource(Res.string.back_to_focus), onClick = onBack)
            .padding(horizontal = 14.dp, vertical = 8.dp).testTag("focus-strip"),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(nowLabel(e, set, badge, strip = true), style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.3.sp),
            fontWeight = FontWeight.SemiBold, color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            buildAnnotatedString {
                append(main)
                rir?.let { withStyle(SpanStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, color = colors.onSurfaceVariant)) { append(" RIR $it") } }
            },
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** A's column labels: Set · Förra · the values · ✓. */
@Composable
internal fun ColumnLabels(type: ExerciseType) {
    val cols = columns(type)
    val style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
    val grey = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        Text(stringResource(Res.string.col_set), Modifier.width(SetColumn), style = style, color = grey, textAlign = TextAlign.Center)
        Text(stringResource(Res.string.col_previous), Modifier.weight(1f), style = style, color = grey)
        cols.forEachIndexed { i, c -> Text(stringResource(c), Modifier.width(cellWidth(cols.size, i)), style = style, color = grey, textAlign = TextAlign.Center) }
        Text("✓", Modifier.width(CheckColumn), style = style, color = grey, textAlign = TextAlign.Center)
    }
}

/**
 * A's set row as D draws it: the badge (a tap changes the set's type, Q21), Förra, the values in fields that open the dock on that
 * field, and the set's own ✓. The next set is tinted. Its background is opaque under the tint, so the red of a swipe never shows
 * through before the row moves.
 */
@Composable
internal fun SetRow(
    tag: String, badge: String, set: WorkoutSet, type: ExerciseType, current: Boolean, prev: WorkoutSet?,
    onKind: (SetKind) -> Unit, onOpen: (Int?) -> Unit, onCheck: () -> Unit, guard: (() -> Unit) -> Unit = { it() },
) {
    val colors = MaterialTheme.colorScheme
    val sv = swedish
    val values = cells(set, type, sv)
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().heightIn(min = if (current) 42.dp else 38.dp)
            .background(colors.background, RowShape)
            .then(if (current) Modifier.background(colors.primary.copy(alpha = 0.12f), RowShape) else Modifier)
            .clickable { onOpen(null) }.testTag(tag).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
    ) {
        Box {
            val label = stringResource(Res.string.set_type, badge)
            Box(
                Modifier.width(SetColumn).height(28.dp).clip(RoundedCornerShape(8.dp)).background(if (current) colors.primary else colors.surfaceContainerHigh)
                    .clickable(onClickLabel = label) { guard { menu = true } }.semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                Text(badge, style = MaterialTheme.typography.bodyMedium, fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold,
                    color = when { current -> colors.onPrimary; set.kind != SetKind.Normal -> colors.tertiary; else -> colors.onSurface })
            }
            DropdownMenu(menu, { menu = false }, containerColor = colors.surfaceContainerHigh) {
                for (kind in SetKind.entries) {
                    DropdownMenuItem(
                        { Text(stringResource(kind.label)) }, onClick = { menu = false; onKind(kind) },
                        leadingIcon = { Box(Modifier.size(18.dp)) { if (kind == set.kind) Glyph(Glyph.Check, 18.dp, color = colors.primary) } },
                        trailingIcon = { Text(kind.letter ?: "1", color = colors.onSurfaceVariant) },
                    )
                }
            }
        }
        Text(prev?.let { setText(it, type, sv) } ?: "–", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        values.forEachIndexed { i, v ->
            Box(
                Modifier.width(cellWidth(values.size, i)).height(34.dp).clip(RoundedCornerShape(10.dp))
                    .background(when { set.done -> Color.Transparent; current -> colors.surfaceContainerHigh; else -> colors.surface })
                    .clickable { onOpen(i) }.testTag("$tag cell $i"),
                contentAlignment = Alignment.Center,
            ) {
                val reps = type.hasRir() && i == values.lastIndex
                Text(
                    buildAnnotatedString {
                        append(v ?: "–")
                        if (reps) set.rir?.let { withStyle(SpanStyle(fontSize = 12.sp, color = colors.onSurfaceVariant)) { append(" @$it") } }
                    },
                    style = MaterialTheme.typography.bodyLarge, maxLines = 1,
                    color = if (set.done || current) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        }
        val check = if (set.done) stringResource(Res.string.logged) else stringResource(Res.string.log_set, badge)
        Box(
            Modifier.width(CheckColumn).height(34.dp).clip(RoundedCornerShape(10.dp))
                .background(if (set.done) colors.primary else if (current) Color.Transparent else colors.surfaceContainerHigh)
                .then(if (current && !set.done) Modifier.border(2.dp, colors.primary, RoundedCornerShape(10.dp)) else Modifier)
                .clickable(onClickLabel = check, onClick = onCheck)
                .semantics { contentDescription = check },
            contentAlignment = Alignment.Center,
        ) {
            Glyph(Glyph.Check, 17.dp, stroke = 2.6f,
                color = if (set.done) colors.onPrimary else if (current) colors.primary else colors.onSurfaceVariant)
        }
    }
}

/** The rest between two sets (Q17), "Vila 2:00" between two lines; teal after the last set logged, where the rest is now. */
@Composable
internal fun RestLine(seconds: Int, now: Boolean) {
    val line = if (now) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    Row(Modifier.fillMaxWidth().height(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f).height(2.dp).background(line, RoundedCornerShape(1.dp)))
        Text(stringResource(Res.string.rest_row, clock(seconds.toLong())), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
            color = if (now) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f).height(2.dp).background(line, RoundedCornerShape(1.dp)))
    }
}

/**
 * C's overview row for a folded exercise: its name, a dot per set, and how many are logged (D). Folded exercises in a row share one
 * card, joined by hairlines; [first] and [last] round its ends.
 */
@Composable
internal fun ExerciseOverview(e: SessionExercise, first: Boolean, last: Boolean, onExpand: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val logged = e.sets.count { it.done }
    val label = stringResource(Res.string.expand, e.name)
    val top = if (first) 18.dp else 0.dp
    val bottom = if (last) 18.dp else 0.dp
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(top, top, bottom, bottom)).background(colors.surface)) {
        if (!first) HorizontalDivider(thickness = 0.5.dp, color = colors.outlineVariant)
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClickLabel = label, onClick = onExpand).padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12),
        ) {
            Column(Modifier.weight(1f).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4)) {
                Text(e.name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp), color = if (e.skipped) colors.onSurfaceVariant else colors.onSurface)
                if (!e.skipped) Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (s in e.sets) {
                        val dot = if (s.kind == SetKind.Warmup) colors.tertiary else colors.primary
                        Box(Modifier.size(8.dp).then(
                            if (s.done) Modifier.background(dot, CircleShape) else Modifier.border(1.5.dp, colors.surfaceContainerHighest, CircleShape),
                        ))
                    }
                }
            }
            val all = logged == e.sets.size && logged > 0
            Text(if (e.skipped) stringResource(Res.string.skipped) else "$logged/${e.sets.size}" + if (all) " ✓" else "",
                style = MaterialTheme.typography.bodyMedium, color = if (all && !e.skipped) colors.primary else colors.onSurfaceVariant,
                fontWeight = if (all && !e.skipped) FontWeight.SemiBold else FontWeight.Normal)
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(colors.surfaceContainerHigh).clickable(onClickLabel = label, onClick = onExpand)
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) { Glyph(Glyph.Down, 14.dp, color = colors.onSurface, stroke = 2.6f) }
        }
    }
}

/**
 * C's empty workout (decision 40): what to do, the library, and quick picks that add an exercise in one tap: the ones marked Ja,
 * else the ones done most recently.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickStart(picks: List<Pair<Long, String>>, fromMarks: Boolean, onLibrary: () -> Unit, onPick: (Long) -> Unit) {
    val space = StriveTheme.spacing
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(space.s24), modifier = Modifier.padding(top = space.s16)) {
        Column(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(24.dp)).padding(horizontal = space.s20, vertical = space.s24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space.s12),
        ) {
            Box(Modifier.size(72.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text("+", style = MaterialTheme.typography.displaySmall, color = colors.onPrimaryContainer)
            }
            Text(stringResource(Res.string.what_today), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(stringResource(Res.string.what_today_body), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            Button(onLibrary, Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = MaterialTheme.shapes.large) { Text(stringResource(Res.string.add_exercises)) }
        }
        if (picks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(space.s12)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(stringResource(Res.string.quick_picks), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(if (fromMarks) Res.string.quick_marked else Res.string.quick_recent), style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(space.s8), verticalArrangement = Arrangement.spacedBy(space.s8)) {
                    for ((id, name) in picks) {
                        val label = stringResource(Res.string.add_named, name)
                        Row(
                            Modifier.heightIn(min = StriveTheme.sizes.touch - 4.dp).background(colors.surfaceContainerHigh, RoundedCornerShape(50))
                                .clickable(onClickLabel = label) { onPick(id) }.padding(start = space.s12, end = space.s16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("+", style = MaterialTheme.typography.titleMedium, color = colors.primary)
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}