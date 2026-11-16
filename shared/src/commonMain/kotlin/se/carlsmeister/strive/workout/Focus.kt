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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import se.carlsmeister.strive.theme.CssLineBox
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.wrapContentHeight
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.ui.touchTall
import se.carlsmeister.strive.ui.GlassMenu
import se.carlsmeister.strive.ui.HeaderSheet
import se.carlsmeister.strive.ui.MenuRow
import se.carlsmeister.strive.ui.MenuSeparator
import se.carlsmeister.strive.ui.MenuTitle
import se.carlsmeister.strive.ui.MorePill
import se.carlsmeister.strive.ui.TextPrompt
import se.carlsmeister.strive.ui.Accent
import se.carlsmeister.strive.ui.text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import se.carlsmeister.strive.data.loggable
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.ShapeLargeIncreased
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
import strive.shared.generated.resources.col_seconds_side
import strive.shared.generated.resources.col_added
import strive.shared.generated.resources.per_side
import strive.shared.generated.resources.type_a_time
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
import strive.shared.generated.resources.now_set_kind
import strive.shared.generated.resources.now_strip
import strive.shared.generated.resources.now_strip_kind
import strive.shared.generated.resources.quick_marked
import strive.shared.generated.resources.quick_picks
import strive.shared.generated.resources.quick_recent
import strive.shared.generated.resources.rename
import strive.shared.generated.resources.rest_after
import strive.shared.generated.resources.rest_after_round
import strive.shared.generated.resources.last_set
import strive.shared.generated.resources.hold_last_set
import strive.shared.generated.resources.log_warmup
import strive.shared.generated.resources.log_drop
import strive.shared.generated.resources.rest_row
import strive.shared.generated.resources.save
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.set_failure
import strive.shared.generated.resources.set_stays
import strive.shared.generated.resources.set_type
import strive.shared.generated.resources.sets_progress
import strive.shared.generated.resources.skipped
import strive.shared.generated.resources.superset_rest
import strive.shared.generated.resources.up_by
import strive.shared.generated.resources.weekdays_short
import strive.shared.generated.resources.what_today
import strive.shared.generated.resources.what_today_body
import strive.shared.generated.resources.workout_options

/** The columns a set row shows after Set and Förra, by what the exercise records. */
internal fun columns(type: ExerciseType, perSide: Boolean = false): List<StringResource> = when (type) {
    ExerciseType.WeightReps -> listOf(Res.string.col_kg, Res.string.col_reps)
    ExerciseType.BodyweightReps -> listOf(Res.string.col_added, Res.string.col_reps)
    ExerciseType.Reps -> listOf(Res.string.col_reps)
    ExerciseType.Time -> listOf(if (perSide) Res.string.col_seconds_side else Res.string.col_seconds)
    ExerciseType.Distance -> listOf(Res.string.col_metres)
}

/** The units after the focus card's values (WO-02-s9f, s10td, s15): a time has none unless it is per side. */
private fun units(type: ExerciseType, perSide: Boolean): List<StringResource?> = when (type) {
    ExerciseType.WeightReps, ExerciseType.BodyweightReps -> listOf(Res.string.col_kg, Res.string.col_reps)
    ExerciseType.Reps -> listOf(Res.string.col_reps)
    ExerciseType.Time -> listOf(if (perSide) Res.string.per_side else null)
    ExerciseType.Distance -> listOf(Res.string.col_metres)
}

/** The values for those columns; null before one is entered. A bodyweight exercise's added weight reads "+10". */
internal fun cells(s: WorkoutSet, type: ExerciseType, swedish: Boolean): List<String?> = when (type) {
    ExerciseType.WeightReps -> listOf(s.weight?.let { kilos(it, swedish) }, s.reps?.toString())
    ExerciseType.BodyweightReps -> listOf(s.weight?.let { "+" + kilos(it, swedish) }, s.reps?.toString())
    ExerciseType.Reps -> listOf(s.reps?.toString())
    ExerciseType.Time -> listOf(s.seconds?.let { clock(it.toLong()) })
    ExerciseType.Distance -> listOf(s.metres?.let { kilos(it, swedish) })
}

/** The focus card's values with their column: added weight is optional, so a bodyweight set without it reads "8 reps". */
internal fun cardCells(s: WorkoutSet, type: ExerciseType, swedish: Boolean): List<IndexedValue<String?>> =
    cells(s, type, swedish).withIndex().filterNot { type == ExerciseType.BodyweightReps && it.index == 0 && it.value == null }

internal val SetKind.label get() = when (this) {
    SetKind.Normal -> Res.string.kind_normal
    SetKind.Warmup -> Res.string.kind_warmup
    SetKind.Drop -> Res.string.kind_drop
    SetKind.Failure -> Res.string.kind_failure
}

/**
 * The colour of a set's type, in its badge, its word in the focus label and its dot, so they cannot drift: warm-up amber, failure coral,
 * drop violet (Carl, 2026-11-03).
 */
internal val SetKind.tint: Color @Composable get() = when (this) {
    SetKind.Normal -> MaterialTheme.colorScheme.onSurface
    SetKind.Warmup -> MaterialTheme.colorScheme.tertiary
    SetKind.Drop -> StriveTheme.accents.drop
    SetKind.Failure -> StriveTheme.accents.failure
}

/** A superset's letter (choice 6's tag): A or B in the superset's blue, before the exercise's name and on each of its sets. */
@Composable
internal fun Tag(letter: String, modifier: Modifier = Modifier) = Box(
    modifier.size(20.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraSmall),
    contentAlignment = Alignment.Center,
) { Text(letter, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = StriveTheme.accents.superset) }

/**
 * G1's line down the superset's left edge (choice 6), drawn on each of its rows so they join into one; nothing unless [on]. As `.ssgroup`:
 * 2 in, the line, then 10 to the content.
 */
@Composable
internal fun Modifier.edge(on: Boolean = true): Modifier {
    if (!on) return this
    val color = StriveTheme.accents.superset
    val width = StriveTheme.borders.focus
    return drawBehind { drawRect(color, Offset(2.dp.toPx(), 0f), Size(width.toPx(), size.height)) }.padding(start = width + 12.dp)
}

/** "Superset · vila 1:30 efter varvet" over the superset's headers (choice 6), "Superset" in the superset's blue. */
@Composable
internal fun SupersetLabel(rest: Int, modifier: Modifier = Modifier) {
    val text = stringResource(Res.string.superset_rest, clock(rest.toLong()))
    val accent = StriveTheme.accents.superset
    val word = SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)
    Text(buildAnnotatedString { append(text); addStyle(word, 0, text.indexOf(" · ").coerceAtLeast(0)) }, modifier,
        style = MaterialTheme.typography.labelMedium.board(), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private val SetColumn = 36.dp
private val CheckColumn = 44.dp
internal val RowShape = RoundedCornerShape(12.dp)

/** D's grid: weight 64 pt, reps 56; a single value gets the two together. */
private fun cellWidth(count: Int, i: Int) = if (count == 1) 88.dp else if (i == 0) 64.dp else 56.dp

private val swedish @Composable get() = androidx.compose.ui.text.intl.Locale.current.language == "sv"

/** Text as a board's CSS `font:` shorthand sets it: the shorthand resets `.strive`'s tabular figures, and the line box is CSS's. */
internal fun TextStyle.board() = copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox)

/** The workout's name with ⋯: Byt namn, Passanteckning and Ändra starttid (Q19). */
@Composable
internal fun WorkoutMenu(w: Workout, large: Boolean, nowMs: Long) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var prompt by remember { mutableStateOf<String?>(null) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (large) 8.dp else 6.dp)) {
        Text(
            w.name, Modifier.weight(1f, fill = false).clickable(onClickLabel = stringResource(Res.string.workout_options)) { menu = true },
            // `.whead .name`: 700 28/34, no tracking.
            style = if (large) MaterialTheme.typography.headlineMedium.board() else MaterialTheme.typography.titleMedium,
            fontWeight = if (large) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Box {
            // `.pill` 40 × 32 in the header, the bar's 32 × 24, each tapped across 44 pt.
            MorePill(stringResource(Res.string.workout_options), { menu = true }, small = !large)
            GlassMenu(menu, { menu = false }) {
                MenuRow(stringResource(Res.string.rename), { menu = false; prompt = "name" })
                MenuRow(stringResource(Res.string.session_note), { menu = false; prompt = "note" })
                MenuRow(stringResource(Res.string.change_start), { menu = false; prompt = "start" })
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
    HeaderSheet(stringResource(Res.string.change_start), onDismiss, cancel = stringResource(Res.string.cancel), done = stringResource(Res.string.save),
        onDone = { onSave(state.hour, state.minute) }) {
        TimeInput(state, Modifier.align(Alignment.CenterHorizontally))
    }
}

/** B's large title (decision 40), as D draws it: the name with ⋯, then the day, the time and the sets done, each with its glyph. */
@Composable
internal fun LargeHeader(w: Workout, now: Long) {
    val start = local(w.startedAt)
    val day = "${stringArrayResource(Res.array.weekdays_short)[start.dayOfWeek.isoDayNumber - 1]} ${start.day} ${
        stringArrayResource(Res.array.months_short)[start.month.number - 1]}" // "Mån 2 nov", as D draws it
    val grey = MaterialTheme.colorScheme.onSurfaceVariant
    val style = MaterialTheme.typography.bodyMedium.board()
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
            val count = stringResource(Res.string.sets_progress, w.countedLogged, w.countedSets)
            if (w.complete) {
                // P11: every working set logged turns the count lime with a check (WO-02-s13's `.m-lime`).
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Glyph(Glyph.Check, 16.dp, color = StriveTheme.moments.lime)
                    Text(count, style = style, color = StriveTheme.moments.text(Accent.Lime))
                }
            } else Text(count, style = style, color = grey)
        }
    }
}

/**
 * The label over the next set: "NU · BENCH PRESS BB · SET 2 AV 4"; the type for a warm-up or drop set, which has no number; the number
 * and then "TILL FAILURE" for a failure set. The type word takes the type's colour. In a superset the exercise's letter comes first:
 * "NU · A · INCLINE PRESS DB · SET 1 AV 3".
 */
@Composable
private fun nowLabel(e: SessionExercise, set: WorkoutSet, badge: String, strip: Boolean, tag: String?): AnnotatedString {
    val name = tag?.let { "$it · ${e.name}" } ?: e.name
    val kind = stringResource(set.kind.label).uppercase()
    val failure = set.kind == SetKind.Failure
    val text = when {
        !set.kind.numbered -> stringResource(Res.string.now_kind, name, kind)
        strip && failure -> stringResource(Res.string.now_strip_kind, name, badge, kind)
        strip -> stringResource(Res.string.now_strip, name, badge)
        failure -> stringResource(Res.string.now_set_kind, name, badge, working(e.sets), kind)
        else -> stringResource(Res.string.now_set, name, badge, working(e.sets))
    }.uppercase()
    val tint = set.kind.tint
    return buildAnnotatedString {
        append(text)
        if (set.kind != SetKind.Normal) text.lastIndexOf(kind).takeIf { it >= 0 }?.let { addStyle(SpanStyle(color = tint), it, it + kind.length) }
    }
}

/** "upp 2,5 kg" against last time, or nothing (Q21's ↑, until #45's rule). */
@Composable
internal fun changeText(set: WorkoutSet, prev: WorkoutSet?, swedish: Boolean): String? = change(set.weight, prev?.weight)?.let {
    stringResource(if (it > 0) Res.string.up_by else Res.string.down_by, kilos(kotlin.math.abs(it), swedish))
}

/**
 * C's focus card as WO-02-s2 draws it: the label, the next set large with "upp 2,5 kg" under it, [meta] (the rest after it, or
 * "last set") and last time's set, RIR, Ändra for the dock, and one tap to log it.
 */
@Composable
internal fun FocusCard(e: SessionExercise, set: WorkoutSet, badge: String, prev: WorkoutSet?, meta: FocusMeta, rir: Int?, more: Boolean,
                       onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit, onEdit: () -> Unit, onLog: () -> Unit, tag: String? = null) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val sv = swedish
    Column(
        Modifier.fillMaxWidth().background(colors.surface, ShapeLargeIncreased).padding(StriveTheme.spacing.s16).testTag("focus"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // The board's CSS line boxes: `.now` 13/18, `.fv` 38/44, `.fchange` 13/18 pulled up 8 under the values, `.fmeta` 4 below.
        Text(nowLabel(e, set, badge, strip = false, tag), style = type.bodySmall.copy(letterSpacing = 0.3.sp).board(), fontWeight = FontWeight.SemiBold,
            color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val big = type.displaySmall.copy(fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-1).sp, fontWeight = FontWeight.Bold).board()
                val units = units(e.type, e.perSide)
                cardCells(set, e.type, sv).forEachIndexed { n, (i, v) ->
                    if (n > 0) Text("×", Modifier.alignByBaseline(), style = type.headlineMedium.copy(fontSize = 26.sp, lineHeight = 32.sp).board(), color = colors.onSurfaceVariant)
                    Text(v ?: "–", Modifier.alignByBaseline(), style = big)
                    units[i]?.let { Text(stringResource(it).lowercase(), Modifier.alignByBaseline(), style = type.bodyMedium.board(), color = colors.onSurfaceVariant) }
                }
            }
            // `.cap`: a time not yet typed says how to log it (WO-02-s15).
            if (e.type == ExerciseType.Time && set.seconds == null) {
                Text(stringResource(Res.string.type_a_time), style = type.labelMedium.board(), color = colors.onSurfaceVariant)
            }
            changeText(set, prev, sv)?.let {
                Text(it, Modifier.layout { m, c ->
                    val p = m.measure(c)
                    val up = 8.dp.roundToPx()
                    layout(p.width, p.height - up) { p.place(0, -up) }
                }, style = type.bodySmall.board(), fontWeight = FontWeight.SemiBold, color = colors.primary)
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                val words = when (meta) {
                    is FocusMeta.RestAfter -> stringResource(Res.string.rest_after, clock(meta.seconds.toLong()))
                    is FocusMeta.RestAfterRound -> stringResource(Res.string.rest_after_round, clock(meta.seconds.toLong()))
                    FocusMeta.LastSet -> stringResource(Res.string.last_set)
                    FocusMeta.HoldLastSet -> stringResource(Res.string.hold_last_set)
                }
                Text(words, Modifier.alignByBaseline(), style = type.bodySmall.board(), color = colors.onSurfaceVariant)
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = colors.onSurfaceVariant)) { append(stringResource(Res.string.col_previous) + " ") }
                        withStyle(SpanStyle(color = colors.onSurface, fontSize = 15.sp)) { append(prev?.let { setText(it, e.type, sv) } ?: "–") }
                    },
                    // `.fmeta b`'s 15/20 sets the row's line box.
                    Modifier.alignByBaseline(), style = type.bodySmall.copy(lineHeight = 20.sp).board(),
                )
            }
        }
        if (e.type.hasRir()) RirRow(rir, more, onRir, onMore, label = false, compact = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Btn(onEdit, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(14.dp)) {
                Text(stringResource(Res.string.edit), style = type.titleMedium)
            }
            // `.fbtn.wait`: until the set has its values, Log waits on a grey fill; a tap opens the dock to type them.
            val ready = set.loggable(e.type)
            Btn(onLog, Modifier.weight(2f).height(50.dp), container = if (ready) colors.primary else colors.surfaceContainerHigh,
                content = if (ready) colors.onPrimary else colors.onSurfaceVariant, shape = RoundedCornerShape(14.dp)) {
                Glyph(Glyph.Check, 20.dp, stroke = 2.8f)
                val log = when (set.kind) {
                    SetKind.Warmup -> stringResource(Res.string.log_warmup)
                    SetKind.Drop -> stringResource(Res.string.log_drop)
                    else -> stringResource(Res.string.log_set, badge)
                }
                Text(log, Modifier.padding(start = 8.dp), style = type.titleMedium, fontWeight = if (ready) FontWeight.Bold else null)
            }
        }
    }
}

/**
 * The focus card folded into a strip under the bar once it scrolls away (D). The whole strip is one button back to the card; logging
 * stays on the card and the rows, so a tap meant for the bar never logs a set (Carl, point 7).
 */
@Composable
internal fun FocusStrip(e: SessionExercise, set: WorkoutSet, badge: String, rir: Int?, onBack: () -> Unit, tag: String? = null) {
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
        Text(nowLabel(e, set, badge, strip = true, tag), style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.3.sp).board(),
            fontWeight = FontWeight.SemiBold, color = colors.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        // No RIR drawn on the strip (design-1 item 7), but VoiceOver still hears the one picked on the card, as it did before.
        val spoken = rir?.let { "$main RIR $it" }
        Text(main, if (spoken != null) Modifier.semantics { contentDescription = spoken } else Modifier,
            style = MaterialTheme.typography.titleMedium.board())
    }
}

/** A's column labels: Set · Förra · the values · ✓. */
@Composable
internal fun ColumnLabels(type: ExerciseType, perSide: Boolean = false) {
    val cols = columns(type, perSide)
    val style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold).board()
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
    tag: String, badge: String, set: WorkoutSet, type: ExerciseType, current: Boolean, prev: WorkoutSet?, number: Int,
    onKind: (SetKind) -> Unit, onOpen: (Int?) -> Unit, onCheck: () -> Unit, guard: (() -> Unit) -> Unit = { it() }, letter: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val sv = swedish
    val values = cells(set, type, sv)
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().heightIn(min = if (current) 42.dp else 38.dp)
            .background(colors.background, RowShape)
            .then(if (current) Modifier.background(StriveTheme.roles.stateNextRow, RowShape) else Modifier)
            .clickable { onOpen(null) }.testTag(tag).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
    ) {
        Box {
            val label = stringResource(Res.string.set_type, badge)
            // VoiceOver reads a failure set's number with its type, since the colour alone says nothing: "Set 2, till failure".
            val desc = if (set.kind == SetKind.Failure) stringResource(Res.string.set_failure, badge) else label
            // The next set's badge is filled: teal, or a warm-up, failure or drop set's own colour (WO-01-picked, WO-02-s9f, s9d).
            val tinted = set.kind != SetKind.Normal
            val fill = when { !current -> colors.surfaceContainerHigh; tinted -> set.kind.tint; else -> colors.primary }
            Box(
                Modifier.width(SetColumn).height(28.dp).clip(MaterialTheme.shapes.small).background(fill)
                    .clickable(onClickLabel = label) { guard { menu = true } }.semantics { contentDescription = desc },
                contentAlignment = Alignment.Center,
            ) {
                Text(badge, style = MaterialTheme.typography.bodyMedium.board(), fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold,
                    color = when { !current -> set.kind.tint; tinted -> colors.onTertiary; else -> colors.onPrimary })
            }
            GlassMenu(menu, { menu = false }) {
                // P8: a logged set keeps its place, whatever its type becomes (WO-13-logged).
                if (set.done) {
                    MenuTitle(stringResource(Res.string.set_stays), Modifier.padding(bottom = StriveTheme.spacing.s8))
                    MenuSeparator()
                }
                // Each type with the badge the set would take: W, the number it has or would have as a working set, D, that number.
                for (kind in listOf(SetKind.Warmup, SetKind.Normal, SetKind.Drop, SetKind.Failure)) {
                    MenuRow(stringResource(kind.label), { menu = false; onKind(kind) }, checked = kind == set.kind, tail = kind.letter ?: "$number",
                        tailColor = if (kind == SetKind.Normal) colors.primary else kind.tint, tailBadge = true)
                }
            }
        }
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            letter?.let { Tag(it, Modifier.padding(end = 8.dp)) }
            Text(prev?.let { setText(it, type, sv) } ?: "–", style = MaterialTheme.typography.bodyMedium.board(),
                color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        values.forEachIndexed { i, v ->
            Box(
                Modifier.width(cellWidth(values.size, i)).height(34.dp).clip(RoundedCornerShape(10.dp))
                    .background(when { set.done -> Color.Transparent; current -> colors.surfaceContainerHigh; else -> colors.surface })
                    // The dock has no added-weight field yet (deviations-42.md), so a bodyweight row's +kg opens it on reps.
                    .clickable { onOpen(if (type == ExerciseType.BodyweightReps) (i - 1).coerceAtLeast(0) else i) }.testTag("$tag cell $i"),
                contentAlignment = Alignment.Center,
            ) {
                val reps = type.hasRir() && i == values.lastIndex
                Text(
                    buildAnnotatedString {
                        // An optional added weight not given is left blank, not a dash waiting for a value.
                        append(v ?: if (type == ExerciseType.BodyweightReps && i == 0) "" else "–")
                        if (reps) set.rir?.let {
                            // A 7 sp space is about 2 pt, `.cell small`'s margin; the text keeps its space so it still reads "8 @2".
                            withStyle(SpanStyle(fontSize = 7.sp)) { append(" ") }
                            withStyle(SpanStyle(fontSize = 12.sp, color = colors.onSurfaceVariant)) { append("@$it") }
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge.board(), maxLines = 1,
                    color = if (set.done || current) colors.onSurface else colors.onSurfaceVariant,
                )
            }
        }
        val check = if (set.done) stringResource(Res.string.logged) else stringResource(Res.string.log_set, badge)
        // `.ok.wait`: the next set's ✓ dims until the set has its values (WO-02-s15); a tap still opens the dock.
        val waiting = current && !set.done && !set.loggable(type)
        Box(
            Modifier.width(CheckColumn).height(34.dp).alpha(if (waiting) StriveTheme.states.disabledOpacity else 1f).clip(RoundedCornerShape(10.dp))
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

/**
 * The rest between two sets (Q17), "Vila 2:00" between two lines; teal after the last set logged, where the rest is now. It never counts
 * down, the T5 card does (Carl, 2026-11-03). A tap changes it, as a rest line does in T4: D's 20 pt line inside a 44 pt target.
 */
@Composable
internal fun RestLine(seconds: Int, now: Boolean, label: String, onClick: () -> Unit) {
    val line = if (now) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    Row(
        Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch).clickable(onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.weight(1f).height(2.dp).background(line, RoundedCornerShape(1.dp)))
        Text(stringResource(Res.string.rest_row, clock(seconds.toLong())), style = MaterialTheme.typography.bodySmall.board(), fontWeight = FontWeight.SemiBold,
            color = if (now) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Box(Modifier.weight(1f).height(2.dp).background(line, RoundedCornerShape(1.dp)))
    }
}

/**
 * C's overview row for a folded exercise: its name, a dot per set, and how many are logged (D). Folded exercises in a row share one
 * card, joined by hairlines; [first] and [last] round its ends.
 */
@Composable
internal fun ExerciseOverview(e: SessionExercise, first: Boolean, last: Boolean, letter: String? = null, complete: Boolean = false,
                               onExpand: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val logged = e.sets.count { it.done }
    val label = stringResource(Res.string.expand, e.name)
    val top = if (first) ShapeLargeIncreased.topStart else ZeroCornerSize
    val bottom = if (last) ShapeLargeIncreased.topStart else ZeroCornerSize
    // `.ovrow + .ovrow::before`: the hairline is drawn over the row from 14 in, so it takes no height.
    val hairline = colors.outlineVariant
    val hairlineWidth = StriveTheme.borders.hairline
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(top, top, bottom, bottom)).background(colors.surface).drawWithContent {
        drawContent()
        if (!first) drawRect(hairline, Offset(14.dp.toPx(), 0f), Size(size.width - 14.dp.toPx(), hairlineWidth.toPx()))
    }) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClickLabel = label, onClick = onExpand).padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12),
        ) {
            Column(Modifier.weight(1f).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    letter?.let { Tag(it, Modifier.padding(end = 8.dp)) }
                    // `.nm.skip`: "Tricep Dips · Skipped" in grey 400, with no count (WO-02-s14).
                    val skipped = stringResource(Res.string.skipped)
                    Text(if (e.skipped) "${e.name} · $skipped" else e.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp, lineHeight = 21.sp).board(),
                        color = if (e.skipped) colors.onSurfaceVariant else colors.onSurface, fontWeight = if (e.skipped) FontWeight.Normal else null)
                }
                if (!e.skipped) Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (s in e.sets) {
                        val dot = if (s.kind == SetKind.Normal) colors.primary else s.kind.tint
                        Box(Modifier.size(8.dp).then(
                            if (s.done) Modifier.background(dot, CircleShape) else Modifier.border(1.5.dp, colors.surfaceContainerHighest, CircleShape),
                        ))
                    }
                }
            }
            val all = logged == e.sets.size && logged > 0
            // P11: lime once the whole workout is logged (WO-02-s13); a finished exercise alone stays teal (WO-02-s4).
            val ink = if (complete) StriveTheme.moments.text(Accent.Lime) else colors.primary
            if (!e.skipped) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("$logged/${e.sets.size}",
                    style = MaterialTheme.typography.bodyMedium.board(), color = if (all) ink else colors.onSurfaceVariant,
                    fontWeight = if (all) FontWeight.SemiBold else FontWeight.Normal)
                if (all) {
                    val done = stringResource(Res.string.logged)
                    Glyph(Glyph.Check, 14.dp, Modifier.offset(y = 1.5.dp).semantics { contentDescription = done }, color = ink)
                }
            }
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
    // `.qs`'s margin-top 8 under `.body`'s own 8, which the list's top padding draws (WO-01-marks, WO-01-none).
    Column(verticalArrangement = Arrangement.spacedBy(space.s24), modifier = Modifier.padding(top = space.s8)) {
        Column(
            Modifier.fillMaxWidth().background(colors.surface, ShapeLargeIncreased).padding(horizontal = space.s20, vertical = space.s24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space.s12),
        ) {
            Box(Modifier.size(72.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(StriveIcons.Plus, null, Modifier.size(32.dp), tint = colors.onPrimaryContainer)
            }
            Text(stringResource(Res.string.what_today), style = MaterialTheme.typography.headlineSmall.board(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(stringResource(Res.string.what_today_body), style = MaterialTheme.typography.bodyMedium.board(), color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Button(onLibrary, Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = MaterialTheme.shapes.large) { Text(stringResource(Res.string.add_exercises)) }
        }
        if (picks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(space.s12)) {
                Row {
                    Text(stringResource(Res.string.quick_picks), Modifier.weight(1f).alignByBaseline(), style = MaterialTheme.typography.titleSmall.board())
                    Text(stringResource(if (fromMarks) Res.string.quick_marked else Res.string.quick_recent), Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.labelMedium.board(), color = colors.onSurfaceVariant)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(space.s8), verticalArrangement = Arrangement.spacedBy(space.s8)) {
                    for ((id, name) in picks) {
                        val label = stringResource(Res.string.add_named, name)
                        // `.chip.qp` is drawn 40 tall; it takes taps across 44, over the 8 between the rows.
                        Row(
                            Modifier.touchTall().clickable(onClickLabel = label) { onPick(id) }.wrapContentHeight().heightIn(min = 40.dp)
                                .background(colors.surfaceContainerHigh, RoundedCornerShape(50)).padding(start = space.s12, end = space.s16),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("+", style = MaterialTheme.typography.bodyMedium.board().copy(fontSize = 18.sp, lineHeight = 18.sp), color = colors.primary)
                            Text(name, style = MaterialTheme.typography.bodyMedium.board())
                        }
                    }
                }
            }
        }
    }
}