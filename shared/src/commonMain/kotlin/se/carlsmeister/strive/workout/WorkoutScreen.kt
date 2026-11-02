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
import strive.shared.generated.resources.collapse
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import strive.shared.generated.resources.no_exercises_yet
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
    val nowMs = rememberNow()
    var editField by remember { mutableStateOf<Int?>(null) }
    val list = rememberLazyListState()
    // Only the exercise being done stands open; the rest fold to C's overview rows, as in E, until their chevron opens them
    // (decision 39). When the focus moves on, the next one opens and the finished one folds.
    val folds = remember { mutableStateMapOf<Long, Boolean>() }
    var finishing by remember { mutableStateOf(false) }
    var discarding by remember { mutableStateOf(false) }
    // The next set to do is outlined (#17 S3): the first one not logged, in the order the list is lifted.
    val current = w.exercises.firstNotNullOfOrNull { e -> e.sets.firstOrNull { !it.done } }?.id
    // The focus card follows the next set, into the next exercise once one is done (decision 39).
    val focus = w.exercises.firstNotNullOfOrNull { e -> e.sets.indexOfFirst { !it.done }.takeIf { it >= 0 }?.let { e to it } }
    var focusRir by remember(current) { mutableStateOf<Int?>(null) }
    var focusMore by remember(current) { mutableStateOf(false) }
    val picks = quickPicks()

    fun log(set: WorkoutSet) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm) // Q23: a short haptic confirms ✓
        scope.launch { sessions.saveSet(set) }
    }

    fun logFocus() {
        val (e, i) = focus ?: return
        val set = e.sets[i]
        if (set.loggable(e.type)) log(set.copy(rir = focusRir, done = true)) else { editing = set.id; editField = null }
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
            // The name rides in the bar only while the large header is out of sight (decision 40).
            val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }
            TitleBar(w, nowMs, showTitle = w.exercises.isEmpty() || scrolled, onMinimise, ::finish)
            Box(Modifier.weight(1f)) {
                LazyColumn(
                    Modifier.fillMaxSize().hazeSource(haze), list,
                    contentPadding = PaddingValues(start = StriveTheme.spacing.s16, end = StriveTheme.spacing.s16, bottom = 320.dp),
                ) {
                    if (w.exercises.isEmpty()) {
                        item(key = "empty") {
                            QuickStart(picks.first, picks.second, onAddExercises) { id -> scope.launch { sessions.add(w.id, listOf(id)) } }
                        }
                    } else {
                        item(key = "header") { LargeHeader(w, nowMs) }
                        focus?.let { (e, i) ->
                            item(key = "focus") {
                                FocusCard(e, e.sets[i], i + 1, focusRir, focusMore, { focusRir = it }, { focusMore = it },
                                    onEdit = { editing = e.sets[i].id; editField = null }, onLog = { logFocus() })
                            }
                        }
                    }
                    for (e in w.exercises) {
                        val folded = folds[e.id] ?: (focus != null && e.id != focus.first.id)
                        if (folded) {
                            item(key = "o${e.id}") {
                                Box(Modifier.padding(top = StriveTheme.spacing.s12).animateItem(placementSpec = place)) { ExerciseOverview(e) { folds[e.id] = false } }
                            }
                            continue
                        }
                        item(key = "h${e.id}") { ExerciseHeader(e, { folds[e.id] = true }, Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = place)) }
                        item(key = "c${e.id}") { ColumnLabels(e.type) }
                        itemsIndexed(e.sets, key = { _, s -> s.id }) { i, s ->
                            Box(Modifier.padding(vertical = 1.dp).animateItem(placementSpec = place)) {
                                SwipeToDelete(MaterialTheme.shapes.medium, onDelete = { scope.launch { sessions.deleteSet(s.id) } }) {
                                    SetRow(
                                        "set ${e.name} ${i + 1}", i + 1, s, e.type, current = s.id == current,
                                        onOpen = { f -> tap { editing = s.id; editField = f } },
                                        onCheck = {
                                            tap {
                                                when {
                                                    s.done -> scope.launch { sessions.saveSet(s.copy(done = false)) }
                                                    s.loggable(e.type) -> log(s.copy(done = true))
                                                    else -> { editing = s.id; editField = null }
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                        item(key = "a${e.id}") {
                            FilledTonalButton(
                                { scope.launch { sessions.addSet(e.id) } },
                                Modifier.fillMaxWidth().padding(top = StriveTheme.spacing.s4, bottom = StriveTheme.spacing.s8).animateItem(placementSpec = place),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text("+", Modifier.padding(end = StriveTheme.spacing.s8), color = MaterialTheme.colorScheme.primary)
                                Text(stringResource(Res.string.add_set))
                            }
                        }
                    }
                    item(key = "end") {
                        Column(Modifier.fillMaxWidth().padding(top = StriveTheme.spacing.s16), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
                            if (w.exercises.isNotEmpty()) {
                                FilledTonalButton(onAddExercises, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch), shape = MaterialTheme.shapes.large) {
                                    Text(stringResource(Res.string.add_exercises))
                                }
                            }
                            TextButton({ discarding = true }, Modifier.fillMaxWidth()) {
                                Text(stringResource(Res.string.discard_workout), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                // Once the focus card scrolls away it folds into a strip under the bar (D).
                val stripShown by remember { derivedStateOf { list.firstVisibleItemIndex > 1 } }
                StripOverlay(stripShown, focus, Modifier.align(Alignment.TopCenter)) { logFocus() }
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
            Dock(e, set, i + 1, haze, onSave = { log(it); editing = null }, onClose = { editing = null }, field = editField)
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
private fun TitleBar(w: Workout, nowMs: Long, showTitle: Boolean, onMinimise: () -> Unit, onFinish: () -> Unit) {
    val alpha by animateFloatAsState(if (showTitle) 1f else 0f, Motion.fade())
    Row(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = StriveTheme.spacing.s8, vertical = StriveTheme.spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            IconTarget(stringResource(Res.string.minimise), onMinimise) { Chevron(down = true, size = 20.dp) }
        }
        Column(Modifier.alpha(alpha), horizontalAlignment = Alignment.CenterHorizontally) {
            if (alpha > 0f) {
                WorkoutMenu(w, large = false)
                Text(progress(w, nowMs), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            val label = @Composable { Text(stringResource(Res.string.finish)) }
            if (w.sets > 0 && w.logged == w.sets) Button(onFinish, content = { label() }) else FilledTonalButton(onFinish, content = { label() })
        }
    }
}

/** The exercise's name with its ⋯ (#17 choice 4); today's note under it once one is opened. */
@Composable
private fun ExerciseHeader(e: SessionExercise, onFold: () -> Unit, modifier: Modifier) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    var menu by remember { mutableStateOf(false) }
    var noteOpen by remember { mutableStateOf(false) }
    var note by remember(e.id) { mutableStateOf(e.note) }
    Column(modifier.padding(top = StriveTheme.spacing.s16)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(e.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
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
            IconTarget(stringResource(Res.string.collapse, e.name), onFold) { Chevron(down = false, up = true, color = MaterialTheme.colorScheme.onSurfaceVariant, size = 14.dp) }
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


/** The focus strip sliding down under the bar while the focus card is out of sight (D). */
@Composable
private fun StripOverlay(shown: Boolean, focus: Pair<SessionExercise, Int>?, modifier: Modifier, onLog: () -> Unit) {
    val reduceMotion = StriveTheme.reduceMotion
    AnimatedVisibility(
        shown && focus != null, modifier.padding(horizontal = StriveTheme.spacing.s12, vertical = StriveTheme.spacing.s4),
        enter = if (reduceMotion) fadeIn(Motion.fade()) else fadeIn(Motion.fade()) + slideInVertically(Motion.settle(IntOffset.VisibilityThreshold)) { -it },
        exit = if (reduceMotion) fadeOut(Motion.fade()) else fadeOut(Motion.fade()) + slideOutVertically(Motion.settle(IntOffset.VisibilityThreshold)) { -it },
    ) {
        // Keeps showing the set it had while it slides away.
        val shownFocus = remember { mutableStateOf(focus) }
        if (focus != null) shownFocus.value = focus
        shownFocus.value?.let { (e, i) -> e.sets.getOrNull(i)?.let { FocusStrip(e, it, i + 1, onLog) } }
    }
}

/** Quick picks for an empty workout: exercises marked Ja, else the ones done most recently, at most six (decision 40). */
@Composable
private fun quickPicks(): Pair<List<Pair<Long, String>>, Boolean> {
    val graph = LocalAppGraph.current
    val all by remember { graph.exercises.all() }.collectAsState(emptyList())
    val history by remember { graph.sessions.history() }.collectAsState(emptyList())
    val marked = all.filter { it.mark == se.carlsmeister.strive.data.Mark.Yes }.map { it.id to it.name }
    if (marked.isNotEmpty()) return marked.take(6) to true
    return history.flatMap { w -> w.exercises.map { it.exerciseId to it.name } }.distinctBy { it.first }.take(6) to false
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
