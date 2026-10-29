package se.carlsmeister.strive.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.Finish
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.loggable
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.add_set
import strive.shared.generated.resources.back_to_workout
import strive.shared.generated.resources.discard_body
import strive.shared.generated.resources.discard_question
import strive.shared.generated.resources.discard_workout
import strive.shared.generated.resources.empty_workout
import strive.shared.generated.resources.exercise_options
import strive.shared.generated.resources.finish
import strive.shared.generated.resources.keep_going
import strive.shared.generated.resources.log_and_save
import strive.shared.generated.resources.log_set
import strive.shared.generated.resources.logged
import strive.shared.generated.resources.minimise
import strive.shared.generated.resources.note
import strive.shared.generated.resources.remove_exercise
import strive.shared.generated.resources.rename
import strive.shared.generated.resources.save_without
import strive.shared.generated.resources.session_note
import strive.shared.generated.resources.time_sets
import strive.shared.generated.resources.unlogged
import strive.shared.generated.resources.workout_options

/** The clock, ticking on the second. */
@Composable
internal fun rememberNow(): Long {
    val t by produceState(now()) {
        while (true) {
            delay(1_000 - now() % 1_000)
            value = now()
        }
    }
    return t
}

/** "12:34 · 3/9 set", the progress line under the workout's name in the title bar and the bar it minimises to. */
@Composable
internal fun progress(w: Workout, now: Long) = stringResource(Res.string.time_sets, clock((now - w.startedAt) / 1_000), w.logged, w.sets)

/**
 * The active workout (#17 choices 1–6, F1): one bar on top, then the exercises with their sets, each logged with its ✓ or opened in
 * the dock. Everything is written as it happens, so a killed app comes back to this screen's state.
 */
@Composable
internal fun WorkoutScreen(onMinimise: () -> Unit, onAddExercises: () -> Unit, onFinished: (Long) -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val workout by remember { sessions.open() }.collectAsState(null)
    // After Avsluta the open workout is gone while this screen is still sliding away; keep showing the last one.
    val last = remember { mutableStateOf<Workout?>(null) }
    workout?.let { last.value = it }
    val w = last.value ?: return
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val haze = rememberHazeState()
    val swedish = Locale.current.language == "sv"
    val reduceMotion = StriveTheme.reduceMotion
    val place = if (reduceMotion) null else Motion.settle(IntOffset.VisibilityThreshold)
    var editing by remember { mutableStateOf<Long?>(null) }
    var finishing by remember { mutableStateOf(false) }
    var discarding by remember { mutableStateOf(false) }
    // The next set to do is outlined (#17 S3): the first one not logged, in the order the list is lifted.
    val current = w.exercises.firstNotNullOfOrNull { e -> e.sets.firstOrNull { !it.done } }?.id

    fun log(set: WorkoutSet) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm) // Q23: a short haptic confirms ✓
        scope.launch { sessions.saveSet(set) }
    }

    fun finish() {
        when {
            w.sets == 0 -> discarding = true
            w.logged == w.sets -> scope.launch { sessions.finish(w.id, Finish.WithoutThem, now()); onFinished(w.id) }
            else -> finishing = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TitleBar(w, onMinimise, ::finish)
            LazyColumn(
                Modifier.weight(1f).hazeSource(haze),
                contentPadding = PaddingValues(start = StriveTheme.spacing.s16, end = StriveTheme.spacing.s16, top = StriveTheme.spacing.s8, bottom = 320.dp),
            ) {
                if (w.exercises.isEmpty()) {
                    item { Text(stringResource(Res.string.empty_workout), Modifier.padding(vertical = StriveTheme.spacing.s24), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                for (e in w.exercises) {
                    item(key = "h${e.id}") { ExerciseHeader(e, Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = place)) }
                    itemsIndexed(e.sets, key = { _, s -> s.id }) { i, s ->
                        val shape = tileShape(i, e.sets.size)
                        Box(Modifier.padding(top = if (i == 0) 0.dp else 2.dp).animateItem(placementSpec = place)) {
                            SwipeToDelete(shape, onDelete = { scope.launch { sessions.deleteSet(s.id) } }) {
                                SetLine(
                                    i + 1, setText(s, e.type, swedish), s, shape, current = s.id == current,
                                    onOpen = { editing = s.id },
                                    onCheck = {
                                        when {
                                            s.done -> scope.launch { sessions.saveSet(s.copy(done = false)) }
                                            s.loggable(e.type) -> log(s.copy(done = true))
                                            else -> editing = s.id
                                        }
                                    },
                                )
                            }
                        }
                    }
                    item(key = "a${e.id}") {
                        TextButton({ scope.launch { sessions.addSet(e.id) } }, Modifier.padding(bottom = StriveTheme.spacing.s16).animateItem(placementSpec = place)) {
                            Text(stringResource(Res.string.add_set))
                        }
                    }
                }
                item(key = "end") {
                    Column(Modifier.fillMaxWidth().padding(top = StriveTheme.spacing.s8), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
                        FilledTonalButton(onAddExercises, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch), shape = MaterialTheme.shapes.large) {
                            Text(stringResource(Res.string.add_exercises))
                        }
                        TextButton({ discarding = true }, Modifier.fillMaxWidth()) {
                            Text(stringResource(Res.string.discard_workout), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        val open = editing?.let { id -> w.exercises.firstNotNullOfOrNull { e -> e.sets.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { e to it } } }
        if (open != null) {
            // A tap anywhere outside the dock closes it (#17 D3).
            Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { editing = null })
        }
        AnimatedVisibility(
            open != null, Modifier.align(Alignment.BottomCenter),
            enter = if (reduceMotion) fadeIn(Motion.fade()) else slideInVertically(Motion.settle(IntOffset.VisibilityThreshold)) { it },
            exit = if (reduceMotion) fadeOut(Motion.fade()) else slideOutVertically(Motion.settle(IntOffset.VisibilityThreshold)) { it },
        ) {
            // Keeps showing the set it closed on while it slides away.
            val shown = remember { mutableStateOf(open) }
            if (open != null) shown.value = open
            val (e, i) = shown.value ?: return@AnimatedVisibility
            val set = e.sets.getOrNull(i) ?: return@AnimatedVisibility
            Dock(e, set, i + 1, haze, onSave = { log(it); editing = null }, onClose = { editing = null })
        }
    }

    if (finishing) {
        val left = w.sets - w.logged
        ActionSheet(
            pluralStringResource(Res.plurals.unlogged, left, left),
            listOf(
                SheetAction(stringResource(Res.string.log_and_save)) { finishing = false; scope.launch { sessions.finish(w.id, Finish.LogAsTheyStand, now()); onFinished(w.id) } },
                SheetAction(stringResource(Res.string.save_without)) { finishing = false; scope.launch { sessions.finish(w.id, Finish.WithoutThem, now()); onFinished(w.id) } },
                // Red, with no alert after it: Avsluta was the intentional action, not discarding (#16).
                SheetAction(stringResource(Res.string.discard_workout), destructive = true) { finishing = false; scope.launch { sessions.discard(w.id); onMinimise() } },
            ),
            stringResource(Res.string.keep_going), onDismiss = { finishing = false },
        )
    }
    if (discarding) {
        Alert(
            stringResource(Res.string.discard_question), stringResource(Res.string.discard_body), stringResource(Res.string.discard_workout),
            onConfirm = { discarding = false; scope.launch { sessions.discard(w.id); onMinimise() } }, onDismiss = { discarding = false },
        )
    }
}

/** F1: ⌄ on the left, the name with ⋯ and the progress in the middle, Avsluta on the right, filled once every set is logged. */
@Composable
private fun TitleBar(w: Workout, onMinimise: () -> Unit, onFinish: () -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var prompt by remember { mutableStateOf<String?>(null) }
    val nowMs = rememberNow()
    Row(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = StriveTheme.spacing.s8, vertical = StriveTheme.spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            IconTarget(stringResource(Res.string.minimise), onMinimise) { Chevron(down = true, size = 20.dp) }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Row(
                    Modifier.heightIn(min = StriveTheme.sizes.touch - 12.dp).clickable(onClickLabel = stringResource(Res.string.workout_options)) { menu = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(w.name, Modifier.weight(1f, fill = false), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(" ⋯", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text(stringResource(Res.string.rename)) }, onClick = { menu = false; prompt = "name" })
                    DropdownMenuItem({ Text(stringResource(Res.string.session_note)) }, onClick = { menu = false; prompt = "note" })
                }
            }
            Text(progress(w, nowMs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            val label = @Composable { Text(stringResource(Res.string.finish)) }
            if (w.sets > 0 && w.logged == w.sets) Button(onFinish, content = { label() }) else FilledTonalButton(onFinish, content = { label() })
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

/** The exercise's name with its ⋯ (#17 choice 4); today's note under it once one is opened. */
@Composable
private fun ExerciseHeader(e: SessionExercise, modifier: Modifier) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var noteOpen by remember { mutableStateOf(false) }
    var note by remember(e.id) { mutableStateOf(e.note) }
    Column(modifier.padding(top = StriveTheme.spacing.s8, bottom = StriveTheme.spacing.s4)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(e.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Box {
                IconTarget(stringResource(Res.string.exercise_options), { menu = true }) {
                    Text("⋯", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text(stringResource(Res.string.note)) }, onClick = { menu = false; noteOpen = true })
                    DropdownMenuItem({ Text(stringResource(Res.string.remove_exercise), color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; scope.launch { sessions.removeExercise(e.id) } })
                }
            }
        }
        if (noteOpen || note.isNotEmpty()) {
            OutlinedTextField(
                note, { note = it; scope.launch { sessions.setExerciseNote(e.id, it) } }, Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.note)) }, textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }
    }
}

/** Joined tiles (#16 rule A, M1): the group's outer corners round, the inner ones small. */
private fun tileShape(i: Int, size: Int): RoundedCornerShape {
    val outer = 16.dp
    val inner = 6.dp
    return RoundedCornerShape(
        topStart = if (i == 0) outer else inner, topEnd = if (i == 0) outer else inner,
        bottomStart = if (i == size - 1) outer else inner, bottomEnd = if (i == size - 1) outer else inner,
    )
}

/** A set line (#17 S3, D3): badge, the values (grey until logged) and its own ✓. A tap on the line opens the dock. */
@Composable
private fun SetLine(number: Int, text: String, set: WorkoutSet, shape: RoundedCornerShape, current: Boolean, onOpen: () -> Unit, onCheck: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val space = StriveTheme.spacing
    Row(
        Modifier.fillMaxWidth().background(colors.surface, shape)
            .border(StriveTheme.borders.focus, if (current) colors.primary else Color.Transparent, shape)
            .clickable(onClick = onOpen).padding(start = space.s12, end = space.s4, top = space.s4, bottom = space.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        Box(Modifier.size(space.s32).background(colors.surfaceContainerHighest, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
            Text("$number", style = MaterialTheme.typography.titleSmall)
        }
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = if (set.done) colors.onSurface else colors.onSurfaceVariant)
        val check = stringResource(if (set.done) Res.string.logged else Res.string.log_set, number)
        Box(
            Modifier.size(StriveTheme.sizes.touch)
                .background(if (set.done) colors.primary else colors.surfaceContainerHighest, MaterialTheme.shapes.medium)
                .clickable(onClickLabel = check, onClick = onCheck),
            contentAlignment = Alignment.Center,
        ) {
            Text("✓", style = MaterialTheme.typography.titleMedium, color = if (set.done) colors.onPrimary else colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
/** The bar a minimised workout sits in above the tabs: its name and progress; a tap brings it back (Q19). */
@Composable
internal fun MiniBar(w: Workout, haze: HazeState, onClick: () -> Unit) {
    val space = StriveTheme.spacing
    Row(
        Modifier.padding(horizontal = space.s20, vertical = space.s8).fillMaxWidth().clip(ShapeFull)
            .glass(haze, StriveTheme.glass)
            .clickable(onClickLabel = stringResource(Res.string.back_to_workout), onClick = onClick)
            .heightIn(min = StriveTheme.sizes.touch).padding(horizontal = space.s20, vertical = space.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(w.name, style = MaterialTheme.typography.titleSmall)
            Text(progress(w, rememberNow()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
