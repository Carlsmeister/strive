package se.carlsmeister.strive.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.ExerciseType
import se.carlsmeister.strive.data.Finish
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.Rest
import se.carlsmeister.strive.data.SessionExercise
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.loggable
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass
import se.carlsmeister.strive.theme.settle
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.add_set
import strive.shared.generated.resources.add_set_rest
import strive.shared.generated.resources.back_to_workout
import strive.shared.generated.resources.bar
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.collapse
import strive.shared.generated.resources.discard_body
import strive.shared.generated.resources.discard_question
import strive.shared.generated.resources.discard_workout
import strive.shared.generated.resources.done
import strive.shared.generated.resources.exercise_options
import strive.shared.generated.resources.exercise_settings
import strive.shared.generated.resources.finish
import strive.shared.generated.resources.include
import strive.shared.generated.resources.keep_going
import strive.shared.generated.resources.log_and_save
import strive.shared.generated.resources.minimise
import strive.shared.generated.resources.move_down
import strive.shared.generated.resources.move_up
import strive.shared.generated.resources.no_bar
import strive.shared.generated.resources.note
import strive.shared.generated.resources.order_skip
import strive.shared.generated.resources.remove_exercise
import strive.shared.generated.resources.rest_all
import strive.shared.generated.resources.rest_change
import strive.shared.generated.resources.rest_row
import strive.shared.generated.resources.save_without
import strive.shared.generated.resources.skip
import strive.shared.generated.resources.step
import strive.shared.generated.resources.sticky_note
import strive.shared.generated.resources.superset_create
import strive.shared.generated.resources.superset_leave
import strive.shared.generated.resources.swap
import strive.shared.generated.resources.time_sets
import strive.shared.generated.resources.unlogged

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
 * The active workout as design D draws it (decision 39, #17 choices 1–6, F1): one bar on top, the large title, the focus card, then
 * the exercise in focus with its sets and the rest between them, the others folded. Everything is written as it happens, so a killed
 * app comes back to this screen's state.
 */
@Composable
internal fun WorkoutScreen(onMinimise: () -> Unit, onAddExercises: () -> Unit, onSwap: (SessionExercise) -> Unit, onFinished: (Long) -> Unit) {
    val graph = LocalAppGraph.current
    val sessions = graph.sessions
    val workout by remember { sessions.open() }.collectAsState(null)
    // After Avsluta the open workout is gone while this screen is still sliding away; keep showing the last one.
    val last = remember { mutableStateOf<Workout?>(null) }
    workout?.let { last.value = it }
    val w = last.value ?: return
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val haze = rememberHazeState()
    val reduceMotion = StriveTheme.reduceMotion
    val place = if (reduceMotion) null else Motion.settle(IntOffset.VisibilityThreshold)
    var editing by remember { mutableStateOf<Long?>(null) }
    val nowMs = rememberNow()
    var editField by remember { mutableStateOf<Int?>(null) }
    // Opened from a rest line, the dock changes the rest without logging the set.
    var editRest by remember { mutableStateOf(false) }
    fun edit(id: Long?, field: Int? = null, rest: Boolean = false) { editing = id; editField = field; editRest = rest }
    val list = rememberLazyListState()
    val history by remember { sessions.history() }.collectAsState(emptyList())
    val restSetting by remember { graph.settings.get(REST_KEY) }.collectAsState(null)
    val defaultRest = defaultRest(restSetting)
    val rest by graph.rest.collectAsState()
    // Only the exercise being done stands open; the rest fold to C's overview rows until opened (decision 39). When the focus moves
    // on, the next one opens and the finished one folds.
    val folds = remember { mutableStateMapOf<Long, Boolean>() }
    var finishing by remember { mutableStateOf(false) }
    var discarding by remember { mutableStateOf(false) }
    val active = w.exercises.filterNot { it.skipped }
    val tags = tags(active)
    // The next set to do is outlined (#17 S3): the first one not logged, in the order the list is lifted, a superset round by round (G2).
    val focusRound = blocks(active).flatMap(::rounds).firstOrNull { r -> r.any { (e, i) -> !e.sets[i].done } }
    val focus = focusRound?.first { (e, i) -> !e.sets[i].done }
    // The rest after the focus's round: its own for a set alone, the round's for a superset.
    val focusRest = focusRound?.last()?.let { (e, i) -> restOf(e.sets[i], defaultRest) } ?: defaultRest
    val current = focus?.let { (e, i) -> e.sets[i].id }
    var focusRir by remember(current) { mutableStateOf<Int?>(null) }
    var focusMore by remember(current) { mutableStateOf(false) }
    val picks = quickPicks()
    val previous = remember(history, w.exercises.map { it.exerciseId }) { w.exercises.associate { it.exerciseId to previous(history, it.exerciseId) } }

    fun log(set: WorkoutSet) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm) // Q23: a short haptic confirms ✓
        scope.launch { sessions.saveSet(set) }
        // The rest starts when a set is logged (Q10, Q17); in a superset, once every set in its round is, in either order (choice 6).
        val after = active.map { e -> e.copy(sets = e.sets.map { if (it.id == set.id) set else it }) }
        restAfterLogging(after, set.id)?.let { graph.rest.value = Rest(now(), restOf(it, defaultRest), it.id) }
    }

    /** A rest changed from its line; the T5 card, if it counts down this rest, follows the new length from when it started. */
    fun saveRest(set: WorkoutSet) {
        scope.launch { sessions.saveSet(set) }
        rest?.takeIf { it.setId == set.id }?.let { graph.rest.value = it.copy(seconds = restOf(set, defaultRest)) }
    }

    fun logFocus() {
        val (e, i) = focus ?: return
        val set = e.sets[i]
        if (set.loggable(e.type)) log(set.copy(rir = focusRir, done = true)) else edit(set.id)
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
            // Once the focus card scrolls away it folds into a strip under the bar (D-scroll).
            val stripShown by remember { derivedStateOf { list.firstVisibleItemIndex > 1 } }
            val strip = stripShown && focus != null
            TitleBar(w, nowMs, showTitle = w.exercises.isEmpty() || scrolled, glassed = scrolled, hairline = !strip, onMinimise, ::finish) {
                graph.rest.value = Rest(now(), it)
            }
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
                                Box(Modifier.padding(top = 14.dp)) {
                                    FocusCard(e, e.sets[i], badges(e.sets)[i], previous[e.exerciseId]?.getOrNull(i), focusRest,
                                        focusRir, focusMore, { focusRir = it }, { focusMore = it },
                                        onEdit = { edit(e.sets[i].id) }, onLog = { logFocus() }, tag = tags[e.id])
                                }
                            }
                        }
                    }
                    // Each exercise alone, or a superset's together in one list in the order you lift (#17 choice 6, G2 with G1's edge line).
                    val shown = blocks(w.exercises)
                    fun folded(b: List<SessionExercise>) = b.all { it.skipped } || (folds[b.first().id] ?: (focus != null && b.none { it.id == focus.first.id }))
                    shown.forEachIndexed { index, b ->
                        if (folded(b)) {
                            // Folded, a superset's exercises are rows in the card the folded exercises share.
                            val first = shown.getOrNull(index - 1)?.let(::folded) != true
                            val last = shown.getOrNull(index + 1)?.let(::folded) != true
                            b.forEachIndexed { n, e ->
                                item(key = "o${e.id}") {
                                    Box(Modifier.padding(top = if (first && n == 0) 14.dp else 0.dp).animateItem(placementSpec = place)) {
                                        ExerciseOverview(e, first && n == 0, last && n == b.lastIndex, tags[e.id]) {
                                            folds[b.first().id] = false
                                            if (e.skipped) scope.launch { sessions.skip(e.id, false) }
                                        }
                                    }
                                }
                            }
                            return@forEachIndexed
                        }
                        val members = b.filterNot { it.skipped }
                        val superset = members.size > 1
                        val rows = rounds(members)
                        val badges = members.associate { it.id to badges(it.sets) }
                        if (superset) {
                            item(key = "g${b.first().id}") {
                                val roundRest = rows.firstOrNull()?.last()?.let { (e, i) -> restOf(e.sets[i], defaultRest) } ?: defaultRest
                                SupersetLabel(roundRest, Modifier.animateItem(placementSpec = place).padding(top = StriveTheme.spacing.s16).edge())
                            }
                        }
                        for (e in members) {
                            item(key = "h${e.id}") {
                                ExerciseHeader(w, e, defaultRest, tags[e.id], onFold = { folds[b.first().id] = true }, onSwap = { onSwap(e) },
                                    Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = place).edge(superset))
                            }
                        }
                        // One row of column labels over a superset whose exercises record the same values.
                        if (members.map { it.type }.distinct().size == 1) item(key = "c${b.first().id}") { Box(Modifier.edge(superset)) { ColumnLabels(members[0].type) } }
                        rows.forEachIndexed { r, round ->
                            for ((e, i) in round) {
                                val s = e.sets[i]
                                val badge = badges.getValue(e.id)[i]
                                item(key = s.id) {
                                    Box(Modifier.edge(superset).bleed(6.dp).padding(vertical = 1.5.dp).animateItem(placementSpec = place)) {
                                        SwipeToDelete(RowShape, onDelete = { scope.launch { sessions.deleteSet(s.id) } }) {
                                            SetRow(
                                                "set ${e.name} ${i + 1}", badge, s, e.type, current = s.id == current, prev = previous[e.exerciseId]?.getOrNull(i),
                                                onKind = { kind -> scope.launch { sessions.setKind(s.id, kind) } },
                                                onOpen = { f -> tap { edit(s.id, f) } },
                                                onCheck = {
                                                    tap {
                                                        when {
                                                            s.done -> scope.launch { sessions.saveSet(s.copy(done = false)) }
                                                            s.loggable(e.type) -> log(s.copy(done = true))
                                                            else -> edit(s.id)
                                                        }
                                                    }
                                                },
                                                guard = { tap(it) },
                                                letter = tags[e.id],
                                            )
                                        }
                                    }
                                }
                            }
                            // The rest after a round is its last set's (Q3); a tap changes it in the dock, as T4's rest line does.
                            if (r < rows.lastIndex) {
                                val (le, li) = round.last()
                                val after = le.sets[li]
                                val now = round.all { (x, j) -> x.sets[j].done } && !rows[r + 1].all { (x, j) -> x.sets[j].done }
                                item(key = "r${after.id}") {
                                    val label = stringResource(Res.string.rest_change, tags[le.id].orEmpty() + badges.getValue(le.id)[li])
                                    Box(Modifier.edge(superset).animateItem(placementSpec = place)) {
                                        RestLine(restOf(after, defaultRest), now, label) { edit(after.id, restField(le.type), rest = true) }
                                    }
                                }
                            }
                        }
                        members.forEachIndexed { n, e ->
                            item(key = "a${e.id}") {
                                val restAfter = e.sets.lastOrNull()?.let { restOf(it, defaultRest) } ?: defaultRest
                                // In a superset only the last exercise's button names the rest, as T4 draws it.
                                val text = if (superset && n < members.lastIndex) stringResource(Res.string.add_set)
                                    else stringResource(Res.string.add_set_rest, clock(restAfter.toLong()))
                                AddButton(text, 40.dp, RoundedCornerShape(12.dp), Modifier.edge(superset).padding(top = 6.dp).animateItem(placementSpec = place),
                                    letter = tags[e.id]) { scope.launch { sessions.addSet(e.id) } }
                            }
                        }
                    }
                    item(key = "end") {
                        Column(Modifier.fillMaxWidth().padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
                            if (w.exercises.isNotEmpty()) AddButton(stringResource(Res.string.add_exercises), 50.dp, MaterialTheme.shapes.large, onClick = onAddExercises)
                            TextButton({ discarding = true }, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch)) {
                                Text(stringResource(Res.string.discard_workout), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                StripOverlay(strip, focus, focusRir, focus?.let { tags[it.first.id] }, Modifier.align(Alignment.TopCenter)) {
                    scope.launch { if (reduceMotion) list.scrollToItem(0) else list.animateScrollToItem(0) }
                }
            }
        }

        val open = editing?.let { id -> w.exercises.firstNotNullOfOrNull { e -> e.sets.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { e to it } } }
        val restNow = rest
        if (open == null && restNow != null) {
            RestCard(restNow, nowMs, haze, onChange = { graph.rest.value = it },
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = StriveTheme.spacing.s16))
        }
        if (open != null) {
            // A tap anywhere outside the dock closes it (#17 D3).
            Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { edit(null) })
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
            Dock(e, set, badges(e.sets)[i], defaultRest, haze, onSave = { if (editRest) saveRest(it) else log(it); edit(null) }, onClose = { edit(null) },
                field = editField, logs = !editRest)
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

/** D's add button: a dark surface with a teal +, for "Lägg till set (2:00)" and "Lägg till övningar"; in a superset, with its letter. */
@Composable
private fun AddButton(text: String, height: androidx.compose.ui.unit.Dp, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier = Modifier,
                      letter: String? = null, onClick: () -> Unit) =
    Btn(onClick, modifier.fillMaxWidth().heightIn(min = height), container = MaterialTheme.colorScheme.surface, shape = shape) {
        Text("+", Modifier.padding(end = 6.dp), style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.primary)
        letter?.let { Tag(it, Modifier.padding(end = 6.dp)) }
        Text(text, style = MaterialTheme.typography.titleSmall)
    }

/**
 * F1 as D draws it: ⌄ and ⏱ on the left, Avsluta on the right, filled once every set is logged. Scrolled, the name with ⋯ and the
 * progress join the middle and the bar turns to glass.
 */
@Composable
private fun TitleBar(w: Workout, nowMs: Long, showTitle: Boolean, glassed: Boolean, hairline: Boolean, onMinimise: () -> Unit, onFinish: () -> Unit,
                     onRest: (Int) -> Unit) {
    val alpha by animateFloatAsState(if (showTitle) 1f else 0f, Motion.fade())
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().background(if (glassed) StriveTheme.glass.thick else colors.background)) {
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(start = 12.dp, end = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconTarget(stringResource(Res.string.minimise), onMinimise) { Glyph(Glyph.Down, 22.dp, stroke = 2.2f, color = colors.onSurface) }
                RestButton(onRest)
            }
            Column(Modifier.alpha(alpha), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (alpha > 0f) {
                    WorkoutMenu(w, large = false, nowMs = nowMs)
                    Text(progress(w, nowMs), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                val all = w.sets > 0 && w.logged == w.sets
                Box(Modifier.height(StriveTheme.sizes.touch), contentAlignment = Alignment.Center) {
                    Btn(onFinish, Modifier.height(40.dp), container = if (all) colors.primary else colors.surfaceContainerHigh,
                        content = if (all) colors.onPrimary else colors.onSurface, shape = ShapeFull) {
                        Text(stringResource(Res.string.finish), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
        if (glassed && hairline) HorizontalDivider(thickness = 0.5.dp, color = colors.outlineVariant)
    }
}

/**
 * The exercise's name, which folds it (Carl, point 3), with its ⋯ (choice 4's M2 with two tiles) and a chevron; under them the sticky
 * note in grey and today's note once one is opened (choice 6).
 */
@Composable
private fun ExerciseHeader(w: Workout, e: SessionExercise, defaultRest: Int, letter: String?, onFold: () -> Unit, onSwap: () -> Unit, modifier: Modifier) {
    val graph = LocalAppGraph.current
    val sessions = graph.sessions
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    var restMenu by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<String?>(null) }
    var noteOpen by remember { mutableStateOf(false) }
    var note by remember(e.id) { mutableStateOf(e.note) }
    val fold = stringResource(Res.string.collapse, e.name)
    Column(modifier.padding(top = StriveTheme.spacing.s16), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
            letter?.let { Tag(it) }
            Text(e.name, Modifier.weight(1f).clickable(onClickLabel = fold, onClick = onFold).padding(vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium, color = colors.primary)
            Box {
                val label = stringResource(Res.string.exercise_options)
                Box(
                    Modifier.size(40.dp, 32.dp).clip(CircleShape).background(colors.primaryContainer).clickable(onClickLabel = label) { menu = true }
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) { Text("⋯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer) }
                DropdownMenu(menu, { menu = false; restMenu = false }, containerColor = colors.surfaceContainerHigh) {
                    if (!restMenu) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
                            MenuTile(Glyph.Note, stringResource(Res.string.note)) { menu = false; noteOpen = true }
                            MenuTile(Glyph.Swap, stringResource(Res.string.swap)) { menu = false; onSwap() }
                        }
                        DropdownMenuItem({ Text(stringResource(Res.string.sticky_note)) }, onClick = { menu = false; dialog = "pin" },
                            trailingIcon = { Glyph(Glyph.Pin, 18.dp, color = colors.onSurface) })
                        DropdownMenuItem({ Text(stringResource(Res.string.rest_all)) }, onClick = { restMenu = true },
                            trailingIcon = { Text("›", style = MaterialTheme.typography.titleMedium) })
                        // Choice 4's Skapa superset; in a superset, the way out of it.
                        if (letter != null) {
                            DropdownMenuItem({ Text(stringResource(Res.string.superset_leave)) }, onClick = { menu = false; scope.launch { sessions.leaveSuperset(e.id) } })
                        } else if (w.exercises.size > 1) {
                            DropdownMenuItem({ Text(stringResource(Res.string.superset_create)) }, onClick = { menu = false; dialog = "superset" })
                        }
                        DropdownMenuItem({ Text(stringResource(Res.string.order_skip)) }, onClick = { menu = false; dialog = "order" })
                        if (e.type == ExerciseType.WeightReps) {
                            DropdownMenuItem({ Text(stringResource(Res.string.exercise_settings)) }, onClick = { menu = false; dialog = "settings" })
                        }
                        HorizontalDivider(thickness = StriveTheme.borders.hairline, color = colors.outlineVariant)
                        DropdownMenuItem({ Text(stringResource(Res.string.remove_exercise), color = colors.error) },
                            onClick = { menu = false; scope.launch { sessions.removeExercise(e.id) } })
                    } else {
                        DropdownMenuItem({ Text(stringResource(Res.string.rest_all), fontWeight = FontWeight.SemiBold) }, onClick = { restMenu = false },
                            leadingIcon = { Glyph(Glyph.Back, 18.dp, color = colors.onSurface) })
                        val all = e.sets.map { restOf(it, defaultRest) }.distinct().singleOrNull()
                        for (r in listOf(60, 90, 120, 150, 180)) {
                            DropdownMenuItem({ Text(clock(r.toLong())) }, onClick = { menu = false; restMenu = false; scope.launch { sessions.setRestAll(e.id, r) } },
                                leadingIcon = { Box(Modifier.size(18.dp)) { if (all == r) Glyph(Glyph.Check, 18.dp, color = colors.primary) } })
                        }
                    }
                }
            }
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(colors.surfaceContainerHigh).clickable(onClickLabel = fold, onClick = onFold)
                    .semantics { contentDescription = fold },
                contentAlignment = Alignment.Center,
            ) { Glyph(Glyph.Up, 14.dp, color = colors.onSurface, stroke = 2.6f) }
        }
        if (e.pinned.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Glyph(Glyph.Pin, 14.dp, color = colors.onSurfaceVariant, stroke = 1.8f)
                Text(e.pinned, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
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
    when (dialog) {
        "pin" -> TextPrompt(stringResource(Res.string.sticky_note), e.pinned, singleLine = false, onSave = { n ->
            dialog = null
            scope.launch { graph.exercises.setNote(e.exerciseId, n) }
        }, onDismiss = { dialog = null })
        "order" -> OrderDialog(w) { dialog = null }
        "superset" -> SupersetDialog(w, e) { dialog = null }
        "settings" -> SettingsDialog(e) { dialog = null }
    }
}

/** One of M2's two palette tiles on top of the exercise menu: Anteckning and Byt. */
@Composable
private fun MenuTile(glyph: Glyph, label: String, onClick: () -> Unit) = Column(
    Modifier.size(128.dp, 64.dp).clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4),
) {
    Glyph(glyph, 20.dp, color = MaterialTheme.colorScheme.onSurface, stroke = 1.8f)
    Text(label, style = MaterialTheme.typography.titleSmall)
}

/** Ordning och hoppa över (Q8): move an exercise up or down, or skip it; a skipped exercise stays recorded. */
@Composable
private fun OrderDialog(w: Workout, onDone: () -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    val workout by remember { sessions.open() }.collectAsState(w)
    val exercises = (workout ?: w).exercises
    fun move(i: Int, by: Int) {
        val ids = exercises.map { it.id }.toMutableList()
        val j = i + by
        if (j !in ids.indices) return
        ids[i] = ids[j].also { ids[j] = ids[i] }
        scope.launch { sessions.reorder(ids) }
    }
    Dialog(onDismissRequest = onDone) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s16),
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4),
        ) {
            Text(stringResource(Res.string.order_skip), Modifier.fillMaxWidth().padding(bottom = StriveTheme.spacing.s8),
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            exercises.forEachIndexed { i, e ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
                        color = if (e.skipped) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    IconTarget(stringResource(Res.string.move_up, e.name), { move(i, -1) }) { Glyph(Glyph.Up, 16.dp, color = MaterialTheme.colorScheme.primary) }
                    IconTarget(stringResource(Res.string.move_down, e.name), { move(i, 1) }) { Glyph(Glyph.Down, 16.dp, color = MaterialTheme.colorScheme.primary) }
                    TextButton({ scope.launch { sessions.skip(e.id, !e.skipped) } }) {
                        Text(stringResource(if (e.skipped) Res.string.include else Res.string.skip))
                    }
                }
            }
            TextButton(onDone, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.done)) }
        }
    }
}

/** Skapa superset: the workout's other exercises; the one tapped joins this one in a superset, right after it. */
@Composable
private fun SupersetDialog(w: Workout, e: SessionExercise, onDone: () -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = onDone) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s16),
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4),
        ) {
            Text(stringResource(Res.string.superset_create), Modifier.fillMaxWidth().padding(bottom = StriveTheme.spacing.s8),
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            for (other in w.exercises.filter { it.id != e.id && !it.skipped }) {
                Text(other.name, Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                    .clickable { scope.launch { sessions.superset(e.id, other.id) }; onDone() }
                    .heightIn(min = StriveTheme.sizes.touch).padding(horizontal = StriveTheme.spacing.s8, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge)
            }
            TextButton(onDone, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.cancel)) }
        }
    }
}

/** Övningens inställningar (Q15, Q7, Q22): the step − and + use, and the bar. */
@Composable
private fun SettingsDialog(e: SessionExercise, onDone: () -> Unit) {
    val exercises = LocalAppGraph.current.exercises
    val scope = rememberCoroutineScope()
    val swedish = Locale.current.language == "sv"
    var step by remember { mutableStateOf(e.step ?: 2.5) }
    var bar by remember { mutableStateOf(e.bar) }
    val steps = listOf(1.0, 1.25, 2.0, 2.5, 4.0, 5.0)
    val bars = listOf(null, 7.5, 15.0, 20.0, 25.0)
    Dialog(onDismissRequest = onDone) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s20),
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12),
        ) {
            Text(stringResource(Res.string.exercise_settings), Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(stringResource(Res.string.step), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Segments(steps.map { kilos(it, swedish) }, steps.indexOf(step).takeIf { it >= 0 }, { i -> i?.let { step = steps[it] } }, clearable = false)
            Text(stringResource(Res.string.bar), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val none = stringResource(Res.string.no_bar)
            Segments(bars.map { it?.let { b -> kilos(b, swedish) } ?: none }, bars.indexOf(bar).takeIf { it >= 0 }, { i -> i?.let { bar = bars[it] } }, clearable = false)
            TextButton({ scope.launch { exercises.setSettings(e.exerciseId, step, bar) }; onDone() }, Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.done))
            }
        }
    }
}

/** The focus strip under the bar while the focus card is out of sight (D-scroll), on the bar's glass. */
@Composable
private fun StripOverlay(shown: Boolean, focus: Pair<SessionExercise, Int>?, rir: Int?, tag: String?, modifier: Modifier, onBack: () -> Unit) {
    val reduceMotion = StriveTheme.reduceMotion
    AnimatedVisibility(
        shown, modifier,
        enter = if (reduceMotion) fadeIn(Motion.fade()) else fadeIn(Motion.fade()) + slideInVertically(Motion.settle(IntOffset.VisibilityThreshold)) { -it },
        exit = if (reduceMotion) fadeOut(Motion.fade()) else fadeOut(Motion.fade()) + slideOutVertically(Motion.settle(IntOffset.VisibilityThreshold)) { -it },
    ) {
        // Keeps showing the set it had while it slides away.
        val shownFocus = remember { mutableStateOf(focus) }
        if (focus != null) shownFocus.value = focus
        Column(Modifier.fillMaxWidth().background(StriveTheme.glass.thick)) {
            shownFocus.value?.let { (e, i) ->
                e.sets.getOrNull(i)?.let {
                    Box(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) { FocusStrip(e, it, badges(e.sets)[i], rir, onBack, tag) }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
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

/** The bar a minimised workout sits in above the tabs: its name, progress and the rest left; a tap brings it back (Q19). */
@Composable
internal fun MiniBar(w: Workout, haze: HazeState, onClick: () -> Unit) {
    val space = StriveTheme.spacing
    val now = rememberNow()
    val rest by LocalAppGraph.current.rest.collectAsState()
    val left = rest?.left(now) ?: 0
    Row(
        Modifier.padding(horizontal = space.s20, vertical = space.s8).fillMaxWidth().clip(ShapeFull)
            .glass(haze, StriveTheme.glass)
            .clickable(onClickLabel = stringResource(Res.string.back_to_workout), onClick = onClick)
            .heightIn(min = StriveTheme.sizes.touch).padding(horizontal = space.s20, vertical = space.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(w.name, style = MaterialTheme.typography.titleSmall)
            Text(progress(w, now) + if (left > 0) " · " + stringResource(Res.string.rest_row, clock(left.toLong())) else "",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}