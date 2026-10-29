package se.carlsmeister.strive.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.back
import strive.shared.generated.resources.done
import strive.shared.generated.resources.ended
import strive.shared.generated.resources.min_unit
import strive.shared.generated.resources.months
import strive.shared.generated.resources.rir_of
import strive.shared.generated.resources.rpe_question
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.started
import strive.shared.generated.resources.tile_records
import strive.shared.generated.resources.tile_rir
import strive.shared.generated.resources.tile_sets
import strive.shared.generated.resources.tile_time
import strive.shared.generated.resources.week
import strive.shared.generated.resources.weekdays
import kotlin.math.roundToLong
import kotlin.time.Instant

internal fun local(ms: Long): LocalDateTime = Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault())

internal fun hhmm(t: LocalDateTime) = "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"

/** "Lördag 24 oktober". */
@Composable
internal fun dayHeading(t: LocalDateTime) =
    "${stringArrayResource(Res.array.weekdays)[t.dayOfWeek.isoDayNumber - 1]} ${t.day} ${stringArrayResource(Res.array.months)[t.month.number - 1]}"

/**
 * The summary after a workout and the session detail from History, one screen (Q29; #17 choice 7, U1's top with U3's exercises):
 * the date, four tiles, RPE and the note, then the exercises folded to their best set, then the times. After a workout it ends in
 * Klar. Redigera comes when there is something to edit a past workout with.
 */
@Composable
internal fun SummaryScreen(id: Long, padding: PaddingValues, after: Boolean, onClose: () -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val workout by remember(id) { sessions.workout(id) }.collectAsState(null)
    val space = StriveTheme.spacing
    Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        Row(Modifier.fillMaxWidth().padding(horizontal = space.s8).heightIn(min = StriveTheme.sizes.touch), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                if (!after) TextButton(onClose) { Text(stringResource(Res.string.back)) }
            }
            Text(workout?.name ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                if (after) Button(onClose) { Text(stringResource(Res.string.done)) }
            }
        }
        val w = workout ?: return@Column
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = space.s16)
                .padding(top = space.s8, bottom = padding.calculateBottomPadding() + space.s24),
            verticalArrangement = Arrangement.spacedBy(space.s24),
        ) { Summary(w) }
    }
}

@Composable
private fun Summary(w: Workout) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    val space = StriveTheme.spacing
    val swedish = Locale.current.language == "sv"
    val start = local(w.startedAt)
    val end = w.endedAt?.let(::local)
    val s = summary(w, now())
    val week = stringResource(Res.string.week, isoWeek(start.date).second)

    Column(verticalArrangement = Arrangement.spacedBy(space.s4)) {
        Text(dayHeading(start), style = MaterialTheme.typography.headlineSmall)
        Text("${hhmm(start)}–${end?.let(::hhmm) ?: ""} · $week", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    // Snitt-RIR replaces volume (decision 32); with RIR on only some sets it says how many (#17 choice 7).
    Row(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
        Tile(Res.string.tile_time, "${s.minutes}", unit = stringResource(Res.string.min_unit))
        Tile(Res.string.tile_rir, s.rir?.let { kilos((it * 10).roundToLong() / 10.0, swedish) } ?: "–",
            sub = if (s.rir != null && s.rirSets < s.sets) stringResource(Res.string.rir_of, s.rirSets, s.sets) else null)
        Tile(Res.string.tile_sets, "${s.sets}")
        Tile(Res.string.tile_records, "–") // records arrive with #46
    }

    Column(verticalArrangement = Arrangement.spacedBy(space.s8)) {
        Text(stringResource(Res.string.rpe_question), style = MaterialTheme.typography.titleSmall)
        Segments((1..10).map { "$it" }, w.rpe?.minus(1), onSelect = { i -> scope.launch { sessions.setRpe(w.id, i?.plus(1)) } })
        var note by remember(w.id) { mutableStateOf(w.note) }
        OutlinedTextField(
            note, { note = it; scope.launch { sessions.setNote(w.id, it) } }, Modifier.fillMaxWidth(), minLines = 2,
            placeholder = { Text(stringResource(Res.string.session_note)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }

    Block {
        w.exercises.forEachIndexed { i, e ->
            if (i > 0) Hairline()
            ExerciseRow(e, swedish)
        }
    }

    Block {
        InfoRow(stringResource(Res.string.started), hhmm(start))
        Hairline()
        InfoRow(stringResource(Res.string.ended), end?.let(::hhmm) ?: "–")
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Tile(label: StringResource, value: String, unit: String? = null, sub: String? = null) = Column(
    Modifier.weight(1f).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large).padding(StriveTheme.spacing.s8),
    verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4),
) {
    Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    // The value large, its unit small (#16 D1), so a long value wraps rather than losing the unit.
    Text(buildAnnotatedString {
        append(value)
        unit?.let { withStyle(SpanStyle(fontSize = MaterialTheme.typography.labelMedium.fontSize)) { append(" $it") } }
    }, style = MaterialTheme.typography.titleLarge, maxLines = 2)
    sub?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
internal fun Block(content: @Composable () -> Unit) =
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)) { content() }

@Composable
internal fun Hairline() = HorizontalDivider(Modifier.padding(start = StriveTheme.spacing.s16), StriveTheme.borders.hairline, MaterialTheme.colorScheme.outlineVariant)

@Composable
private fun InfoRow(label: String, value: String) = Row(
    Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch).padding(horizontal = StriveTheme.spacing.s16),
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** One row per exercise with its best set, folded; a tap shows every set as logged (U3). */
@Composable
private fun ExerciseRow(e: SessionExercise, swedish: Boolean) {
    var open by remember(e.id) { mutableStateOf(false) }
    val space = StriveTheme.spacing
    Column(Modifier.animateContentSize(if (StriveTheme.reduceMotion) Motion.fade() else Motion.settle())) {
        Row(
            Modifier.fillMaxWidth().clickable { open = !open }.heightIn(min = StriveTheme.sizes.touch)
                .padding(horizontal = space.s16, vertical = space.s8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space.s8),
        ) {
            Column(Modifier.weight(1f)) {
                Text(e.name, style = MaterialTheme.typography.bodyLarge)
                best(e)?.let { Text(setText(it, e.type, swedish), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Chevron(down = open, color = MaterialTheme.colorScheme.onSurfaceVariant, size = androidx.compose.ui.unit.Dp(12f))
        }
        if (open) {
            e.sets.forEachIndexed { i, set ->
                Row(
                    Modifier.fillMaxWidth().padding(start = space.s32, end = space.s16, bottom = space.s8),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space.s12),
                ) {
                    Box(Modifier.size(space.s24).background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape), contentAlignment = Alignment.Center) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                    }
                    Text(setText(set, e.type, swedish), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (e.note.isNotEmpty()) {
                Text(e.note, Modifier.padding(start = space.s32, end = space.s16, bottom = space.s8), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}