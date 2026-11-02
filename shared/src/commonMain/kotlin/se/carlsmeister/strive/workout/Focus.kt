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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.add_named
import strive.shared.generated.resources.col_kg
import strive.shared.generated.resources.col_metres
import strive.shared.generated.resources.col_previous
import strive.shared.generated.resources.col_reps
import strive.shared.generated.resources.col_seconds
import strive.shared.generated.resources.col_set
import strive.shared.generated.resources.edit
import strive.shared.generated.resources.expand
import strive.shared.generated.resources.log_set
import strive.shared.generated.resources.logged
import strive.shared.generated.resources.months
import strive.shared.generated.resources.now_set
import strive.shared.generated.resources.quick_marked
import strive.shared.generated.resources.quick_picks
import strive.shared.generated.resources.quick_recent
import strive.shared.generated.resources.rename
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.sets_progress
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

private val SetColumn = 36.dp
private val CheckColumn = 44.dp

/** A cell's width: weight a little wider than reps, a single value wider still. */
private fun cellWidth(count: Int, i: Int) = if (count == 1) 88.dp else if (i == 0) 72.dp else 60.dp

/** The workout's name with ⋯: rename and the workout note (Q19). */
@Composable
internal fun WorkoutMenu(w: Workout, large: Boolean) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var prompt by remember { mutableStateOf<String?>(null) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        Text(
            w.name, Modifier.weight(1f, fill = false).clickable(onClickLabel = stringResource(Res.string.workout_options)) { menu = true },
            style = if (large) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleMedium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Box {
            IconTarget(stringResource(Res.string.workout_options), { menu = true }) {
                Box(Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50)).padding(horizontal = 10.dp)) {
                    Text("⋯", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            DropdownMenu(menu, { menu = false }) {
                DropdownMenuItem({ Text(stringResource(Res.string.rename)) }, onClick = { menu = false; prompt = "name" })
                DropdownMenuItem({ Text(stringResource(Res.string.session_note)) }, onClick = { menu = false; prompt = "note" })
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
    }
}

/** B's large title (decision 40): the name with ⋯, then the day, the time and the sets done. Shown once the workout has an exercise. */
@Composable
internal fun LargeHeader(w: Workout, now: Long) {
    val start = local(w.startedAt)
    val day = "${stringArrayResource(Res.array.weekdays_short)[start.dayOfWeek.isoDayNumber - 1]} ${start.day} ${
        stringArrayResource(Res.array.months)[start.month.number - 1]}"
    Column(Modifier.padding(top = StriveTheme.spacing.s4, bottom = StriveTheme.spacing.s12), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4)) {
        WorkoutMenu(w, large = true)
        Row(horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s16)) {
            val grey = MaterialTheme.colorScheme.onSurfaceVariant
            Text(day, style = MaterialTheme.typography.bodyMedium, color = grey)
            Text(clock((now - w.startedAt) / 1_000), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(Res.string.sets_progress, w.logged, w.sets), style = MaterialTheme.typography.bodyMedium, color = grey)
        }
    }
}

/** C's focus card (decision 39, D): the next set large, RIR, Ändra for the dock, and one tap to log it. */
@Composable
internal fun FocusCard(e: SessionExercise, set: WorkoutSet, number: Int, rir: Int?, more: Boolean, onRir: (Int?) -> Unit, onMore: (Boolean) -> Unit,
                       onEdit: () -> Unit, onLog: () -> Unit) {
    val space = StriveTheme.spacing
    val swedish = androidx.compose.ui.text.intl.Locale.current.language == "sv"
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(22.dp)).padding(space.s16).testTag("focus"),
        verticalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        Text(stringResource(Res.string.now_set, e.name, number, e.sets.size).uppercase(), style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space.s8)) {
            val values = cells(set, e.type, swedish)
            val units = columns(e.type)
            values.forEachIndexed { i, v ->
                if (i > 0) Text("×", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(v ?: "–", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text(stringResource(units[i]).lowercase(), Modifier.padding(bottom = 6.dp), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (e.type.hasRir()) RirRow(rir, more, onRir, onMore)
        Row(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
            FilledTonalButton(onEdit, Modifier.weight(1f).heightIn(min = 50.dp), shape = MaterialTheme.shapes.medium) { Text(stringResource(Res.string.edit)) }
            Button(onLog, Modifier.weight(2f).heightIn(min = 50.dp), shape = MaterialTheme.shapes.medium) {
                Text("✓  " + stringResource(Res.string.log_set, number), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** The focus card folded into a strip under the bar once it scrolls away, so the next set is one tap from anywhere (D). */
@Composable
internal fun FocusStrip(e: SessionExercise, set: WorkoutSet, number: Int, onLog: () -> Unit) {
    val space = StriveTheme.spacing
    val swedish = androidx.compose.ui.text.intl.Locale.current.language == "sv"
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(start = space.s16, end = space.s8, top = space.s8, bottom = space.s8).testTag("focus-strip"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.now_set, e.name, number, e.sets.size).uppercase(), style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(setText(set, e.type, swedish), style = MaterialTheme.typography.titleMedium)
        }
        Button(onLog, Modifier.heightIn(min = StriveTheme.sizes.touch)) { Text("✓  " + stringResource(Res.string.log_set, number)) }
    }
}

/** A's column labels: Set · Förra · the values · ✓. */
@Composable
internal fun ColumnLabels(type: ExerciseType) {
    val cols = columns(type)
    val style = MaterialTheme.typography.labelMedium
    val grey = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.fillMaxWidth().padding(vertical = StriveTheme.spacing.s4), horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        Text(stringResource(Res.string.col_set), Modifier.width(SetColumn), style = style, color = grey, textAlign = TextAlign.Center)
        Text(stringResource(Res.string.col_previous), Modifier.weight(1f), style = style, color = grey)
        cols.forEachIndexed { i, c -> Text(stringResource(c), Modifier.width(cellWidth(cols.size, i)), style = style, color = grey, textAlign = TextAlign.Center) }
        Text("✓", Modifier.width(CheckColumn), style = style, color = grey, textAlign = TextAlign.Center)
    }
}

/**
 * A's set row (decision 39, D): badge, Förra (filled by #45), the values in grey fields that open the dock on that field, and the
 * set's own ✓. The next set is tinted.
 */
@Composable
internal fun SetRow(tag: String, number: Int, set: WorkoutSet, type: ExerciseType, current: Boolean, onOpen: (Int?) -> Unit, onCheck: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val space = StriveTheme.spacing
    val swedish = androidx.compose.ui.text.intl.Locale.current.language == "sv"
    val values = cells(set, type, swedish)
    Row(
        Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch)
            .background(if (current) colors.primary.copy(alpha = 0.12f) else colors.background, MaterialTheme.shapes.medium)
            .clickable { onOpen(null) }.testTag(tag).padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space.s8),
    ) {
        Box(
            Modifier.width(SetColumn).height(28.dp).background(if (current) colors.primary else colors.surfaceContainerHigh, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) { Text("$number", style = MaterialTheme.typography.titleSmall, color = if (current) colors.onPrimary else colors.onSurface) }
        Text("–", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        values.forEachIndexed { i, v ->
            Box(
                Modifier.width(cellWidth(values.size, i)).height(36.dp)
                    .background(if (set.done) Color.Transparent else colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
                    .clickable { onOpen(i) }.testTag("$tag cell $i"),
                contentAlignment = Alignment.Center,
            ) {
                Text(v ?: "–", style = MaterialTheme.typography.bodyLarge,
                    color = if (set.done || current) colors.onSurface else colors.onSurfaceVariant)
            }
        }
        val check = stringResource(if (set.done) Res.string.logged else Res.string.log_set, number)
        Box(
            Modifier.width(CheckColumn).height(36.dp)
                .background(if (set.done) colors.primary else if (current) Color.Transparent else colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
                .border(if (current && !set.done) 2.dp else 0.dp, if (current && !set.done) colors.primary else Color.Transparent, MaterialTheme.shapes.medium)
                .clickable(onClickLabel = check, onClick = onCheck)
                .semantics { contentDescription = check },
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", style = MaterialTheme.typography.titleMedium,
                color = if (set.done) colors.onPrimary else if (current) colors.primary else colors.onSurfaceVariant)
        }
    }
}

/** C's overview row for a folded exercise: its name, a dot per set, and how many are logged (D). */
@Composable
internal fun ExerciseOverview(e: SessionExercise, onExpand: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val space = StriveTheme.spacing
    val logged = e.sets.count { it.done }
    val label = stringResource(Res.string.expand, e.name)
    Row(
        Modifier.fillMaxWidth().padding(vertical = space.s4).background(colors.surface, MaterialTheme.shapes.large)
            .clickable(onClickLabel = label, onClick = onExpand).padding(start = space.s16, end = space.s4, top = space.s8, bottom = space.s8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space.s8),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(space.s4)) {
            Text(e.name, style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                for (s in e.sets) {
                    Box(Modifier.size(8.dp).then(
                        if (s.done) Modifier.background(colors.primary, CircleShape) else Modifier.border(1.5.dp, colors.surfaceContainerHighest, CircleShape),
                    ))
                }
            }
        }
        val all = logged == e.sets.size && logged > 0
        Text("$logged/${e.sets.size}" + if (all) " ✓" else "", style = MaterialTheme.typography.bodyMedium,
            color = if (all) colors.primary else colors.onSurfaceVariant, fontWeight = if (all) FontWeight.SemiBold else FontWeight.Normal)
        IconTarget(label, onExpand) { Chevron(down = true, color = colors.onSurfaceVariant, size = 14.dp) }
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
