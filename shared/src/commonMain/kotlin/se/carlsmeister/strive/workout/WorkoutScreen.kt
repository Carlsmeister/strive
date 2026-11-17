package se.carlsmeister.strive.workout

import androidx.compose.animation.core.Animatable
import se.carlsmeister.strive.theme.calm
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import se.carlsmeister.strive.ui.InputField
import se.carlsmeister.strive.ui.MorePill
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import strive.shared.generated.resources.note_plan
import strive.shared.generated.resources.note_previous
import strive.shared.generated.resources.note_today
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.runtime.CompositionLocalProvider
import se.carlsmeister.strive.theme.LocalGlassSource
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.ui.ActionSheet
import se.carlsmeister.strive.ui.CentredBar
import se.carlsmeister.strive.ui.Segments
import se.carlsmeister.strive.ui.Alert
import se.carlsmeister.strive.ui.AlertBox
import se.carlsmeister.strive.ui.AlertButton
import se.carlsmeister.strive.ui.GlassMenu
import se.carlsmeister.strive.ui.Overlay
import se.carlsmeister.strive.ui.HeaderSheet
import se.carlsmeister.strive.ui.SheetAction
import se.carlsmeister.strive.ui.TextPrompt
import se.carlsmeister.strive.ui.MenuRow
import se.carlsmeister.strive.ui.MenuTile
import se.carlsmeister.strive.ui.MenuTiles
import se.carlsmeister.strive.ui.Toast
import strive.shared.generated.resources.exercise_removed
import strive.shared.generated.resources.restored
import strive.shared.generated.resources.swapped_for
import strive.shared.generated.resources.undo
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
import se.carlsmeister.strive.data.WeightUnit
import se.carlsmeister.strive.data.WorkoutSet
import se.carlsmeister.strive.data.defaultUnit
import se.carlsmeister.strive.data.unitIn
import se.carlsmeister.strive.data.loggable
import se.carlsmeister.strive.library.kilos
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.settle
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.add_set
import strive.shared.generated.resources.add_set_rest
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

/**
 * "12:34 · 3/9 sets", the progress line under the workout's name in the title bar and the bar it minimises to; the time alone while the
 * workout has no working sets (P10, WO-01: "0:12").
 */
@Composable
internal fun progress(w: Workout, now: Long): String {
    val time = clock((now - w.startedAt) / 1_000)
    return if (w.countedSets == 0) time else stringResource(Res.string.time_sets, time, w.countedLogged, w.countedSets)
}

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
    // The graph's toasts, not the screen's: a removal still lands after the workout is minimised (P4, P5).
    val toasts = graph.toasts
    val hidden by toasts.hidden.collectAsState()
    // After Avsluta the open workout is gone while this screen is still sliding away; keep showing the last one.
    val last = remember { mutableStateOf<Workout?>(null) }
    workout?.let { last.value = it }
    // An exercise waiting on Undo is gone from everything the screen shows and counts.
    val w = (last.value ?: return).without(hidden)
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
    val defaultUnit by remember { graph.settings.defaultUnit() }.collectAsState(WeightUnit.Kg)
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
        // Q23 and MO-log: a light haptic confirms a log; success is a record's. Compose 1.12.1 sends ToggleOn to iOS's light impact
        // and Confirm to its success notification. Recheck on a Compose bump.
        haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
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

    // A removal waiting on Undo lands before the workout is finished or discarded, so neither sees the exercise (P4).
    fun finishAs(how: Finish) = scope.launch { toasts.settle(); sessions.finish(w.id, how, now()); onFinished(w.id) }
    fun discard() = scope.launch { toasts.settle(); sessions.discard(w.id); onMinimise() }

    fun finish() {
        when {
            w.sets == 0 -> discarding = true
            w.logged == w.sets -> finishAs(Finish.WithoutThem)
            else -> finishing = true
        }
    }

    // P9: a drag down the title bar, in px, as draggable reports it. The screen follows it 1:1 and eases toward the bar (to .9, corners
    // to 28) as it nears a third of the screen; let go, it minimises or springs back. Read only in layout and draw, so a drag recomposes
    // nothing.
    val density = LocalDensity.current
    var pulled by remember { mutableFloatStateOf(0f) }
    var leaving by remember { mutableStateOf(false) }
    var screenHeight by remember { mutableIntStateOf(0) }
    val pull = rememberDraggableState { delta -> pulled = (pulled + delta).coerceAtLeast(0f) }
    // P9's flick, WO-04's 1000 pt/s, in px/s like the release velocity; pt are dp here.
    val flick = with(density) { 1000.dp.toPx() }
    // The finger's speed, tracked on the box that does not move. Draggable's own velocity is taken in the title bar's coordinates, and
    // the bar moves with the finger, so it read about 0 for a flick (measured: 0.0 px/s for 405 px in 80 ms).
    val finger = remember { VelocityTracker() }
    val dragToMinimise = Modifier.draggable(pull, Orientation.Vertical, onDragStopped = {
        // Once, on release: velocity is px/s and positive downward, as are pulled and the height.
        val velocity = finger.calculateVelocity().y
        if (minimises(pulled, screenHeight.toFloat(), velocity, flick)) {
            leaving = true
            onMinimise()
        } else pull.drag {
            // Inside the drag's lock, so a finger that lands mid-spring takes the screen over from it.
            animate(pulled, 0f, velocity, Motion.settle()) { value, _ -> dragBy(value - pulled) }
        }
    })

    // Behind the screen as it is pulled down, black, as behind an iOS card; gone once it leaves, so the tabs show as it sinks.
    Box(
        Modifier.fillMaxSize().drawBehind { if (pulled > 0f && !leaving) drawRect(Color.Black) }.pointerInput(Unit) {
            // Watches every touch on its way in and consumes none, so the screen under it works as before.
            awaitPointerEventScope { while (true) awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull()?.let(finger::addPointerInputChange) }
        },
    ) {
    Box(
        Modifier.fillMaxSize().onSizeChanged { screenHeight = it.height }.graphicsLayer {
            val toward = if (screenHeight == 0) 0f else (pulled / (screenHeight / 3f)).coerceAtMost(1f)
            translationY = pulled
            // From the top edge, so the bar stays under the finger. Under Reduce Motion it does not scale (P9).
            transformOrigin = TransformOrigin(0.5f, 0f)
            if (!reduceMotion) { scaleX = 1f - 0.1f * toward; scaleY = scaleX }
            shape = RoundedCornerShape(28.dp.toPx() * toward)
            clip = toward > 0f
        }.background(MaterialTheme.colorScheme.background),
    ) { CompositionLocalProvider(LocalGlassSource provides haze) {
        Column(Modifier.fillMaxSize()) {
            // The name rides in the bar only while the large header is out of sight (decision 40).
            val scrolled by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }
            // Once the focus card scrolls away it folds into a strip under the bar (D-scroll): as soon as what is left of the card
            // fits under the strip, which covers it (WO-02-note, WO-03-strip). The list starts under the bar, as the strip does.
            val stripShown by remember {
                derivedStateOf {
                    val card = list.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "focus" }
                    list.firstVisibleItemIndex > 1 ||
                        card != null && card.offset + card.size - list.layoutInfo.viewportStartOffset <= with(density) { StripHeight.roundToPx() }
                }
            }
            val strip = stripShown && focus != null
            TitleBar(w, nowMs, showTitle = w.exercises.isEmpty() || scrolled, glassed = scrolled, hairline = !strip, onMinimise, ::finish, dragToMinimise) {
                graph.rest.value = Rest(now(), it)
            }
            Box(Modifier.weight(1f)) {
                LazyColumn(
                    // Its own background inside the source: haze blurs only what the source draws, and glass over a transparent
                    // source showed the list's text sharp through its tint.
                    Modifier.fillMaxSize().hazeSource(haze).background(MaterialTheme.colorScheme.background), list,
                    contentPadding = PaddingValues(StriveTheme.spacing.s16, StriveTheme.spacing.s8, StriveTheme.spacing.s16, 320.dp),
                ) {
                    if (w.exercises.isEmpty()) {
                        item(key = "empty") {
                            // A quick pick comes with last time's sets, so its first set logs in one tap (P14).
                            QuickStart(picks.first, picks.second, onAddExercises) { id -> scope.launch { sessions.add(w.id, listOf(id), lastTime = true) } }
                        }
                    } else {
                        item(key = "header") { LargeHeader(w, nowMs) }
                        focus?.let { (e, i) ->
                            item(key = "focus") {
                                Box(Modifier.padding(top = StriveTheme.spacing.s16)) {
                                    // Resting: a rest with time left began on one of this exercise's sets. The rest card clears an
                                    // ended rest only while it shows, so a rest under the dock can outlive its time.
                                    val resting = rest?.takeIf { it.left(nowMs) > 0 }?.setId?.let { id -> e.sets.any { it.id == id } } == true
                                    val meta = focusMeta(e, e.sets[i].id, inSuperset = tags[e.id] != null, resting, focusRest)
                                    FocusCard(e, e.unitIn(defaultUnit), e.sets[i], badges(e.sets)[i], previousOf(e.sets, i, previous[e.exerciseId]), meta,
                                        focusRir, focusMore, { focusRir = it }, { focusMore = it },
                                        onEdit = { edit(e.sets[i].id) }, onLog = { logFocus() }, tag = tags[e.id])
                                }
                            }
                        }
                    }
                    // Each exercise alone, or a superset's together in one list in the order you lift (#17 choice 6, G2 with G1's edge line).
                    val shown = blocks(w.exercises)
                    // With every set logged nothing is being done, so every exercise folds into the overview, a workout of one
                    // exercise too (WO-02-s13, WO-01-logged).
                    fun folded(b: List<SessionExercise>) = b.all { it.skipped } ||
                        (folds[b.first().id] ?: (focus == null || b.none { it.id == focus.first.id }))
                    shown.forEachIndexed { index, b ->
                        if (folded(b)) {
                            // Folded, a superset's exercises are rows in the card the folded exercises share.
                            val first = shown.getOrNull(index - 1)?.let(::folded) != true
                            val last = shown.getOrNull(index + 1)?.let(::folded) != true
                            b.forEachIndexed { n, e ->
                                item(key = "o${e.id}") {
                                    Box(Modifier.padding(top = if (first && n == 0) StriveTheme.spacing.s16 else 0.dp).animateItem(placementSpec = place)) {
                                        ExerciseOverview(e, first && n == 0, last && n == b.lastIndex, tags[e.id], complete = w.complete) {
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
                                SupersetLabel(roundRest, Modifier.animateItem(placementSpec = place).padding(top = StriveTheme.spacing.s16 + StriveTheme.spacing.s8).edge())
                            }
                        }
                        members.forEachIndexed { n, e ->
                            item(key = "h${e.id}") {
                                // A lone exercise is its own section: the `.body`'s 16 gap comes before its header's own 16. A superset's
                                // headers stack under its label: the first 6 below it, the others at once (WO-02-s4).
                                val top = if (!superset) StriveTheme.spacing.s16 * 2 else if (n == 0) 6.dp else 0.dp
                                ExerciseHeader(w, e, defaultRest, tags[e.id], onFold = { folds[b.first().id] = true }, onSwap = { onSwap(e) },
                                    Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null, placementSpec = place).edge(superset).padding(top = top))
                            }
                        }
                        // One row of column labels over a superset whose exercises record the same values, in the same unit.
                        if (members.map { it.type to it.unitIn(defaultUnit) }.distinct().size == 1) item(key = "c${b.first().id}") {
                            Box(Modifier.edge(superset)) { ColumnLabels(members[0].type, members[0].unitIn(defaultUnit), members[0].perSide) }
                        }
                        rows.forEachIndexed { r, round ->
                            round.forEachIndexed { k, (e, i) ->
                                val s = e.sets[i]
                                val badge = badges.getValue(e.id)[i]
                                item(key = s.id) {
                                    // `.srow`'s 1.5 margins collapse between a round's rows, so only the first takes one on top.
                                    val top = if (k == 0) 1.5.dp else 0.dp
                                    Box(Modifier.edge(superset).bleed(6.dp).padding(top = top, bottom = 1.5.dp).animateItem(placementSpec = place)) {
                                        SwipeToDelete(RowShape, onDelete = { scope.launch { sessions.deleteSet(s.id) } }) {
                                            SetRow(
                                                "set ${e.name} ${i + 1}", badge, s, e.type, current = s.id == current, prev = previousOf(e.sets, i, previous[e.exerciseId]),
                                                number = numberAsWorking(e.sets, i),
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
                                // `.addset`'s 6 collapses the last row's 1.5 margin, which that row already pads below.
                                val top = if (n == 0 && rows.isNotEmpty()) 4.5.dp else 6.dp
                                AddButton(text, 40.dp, RoundedCornerShape(12.dp), Modifier.edge(superset).padding(top = top).animateItem(placementSpec = place),
                                    letter = tags[e.id]) { scope.launch { sessions.addSet(e.id) } }
                            }
                        }
                    }
                    item(key = "end") {
                        // `.vstack`'s 12 over `.body`'s 16 gap under the exercises; the empty workout's Discard follows on the gap alone.
                        val top = if (w.exercises.isEmpty()) StriveTheme.spacing.s16 else 28.dp
                        Column(Modifier.fillMaxWidth().padding(top = top), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
                            if (w.exercises.isNotEmpty()) AddButton(stringResource(Res.string.add_exercises), 50.dp, MaterialTheme.shapes.large, onClick = onAddExercises)
                            TextButton({ discarding = true }, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch)) {
                                Text(stringResource(Res.string.discard_workout), style = MaterialTheme.typography.titleMedium.board(), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                StripOverlay(strip, focus, defaultUnit, focusRir, focus?.let { tags[it.first.id] }, Modifier.align(Alignment.TopCenter)) {
                    scope.launch { if (reduceMotion) list.scrollToItem(0) else list.animateScrollToItem(0) }
                }
            }
        }

        val open = editing?.let { id -> w.exercises.firstNotNullOfOrNull { e -> e.sets.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { e to it } } }
        val restNow = rest
        if (open == null && restNow != null) {
            // MO-log: the card rises 16 pt and fades in on the calm spring from 150 ms after the log; under Reduce Motion it fades in
            // where it sits. One delay that ends, on the frame clock, so the UI tests still go idle. Only a rest just started rises: back
            // after the dock or with the workout reopened from the bar, the card stands where it was.
            val rise = remember(restNow.startedAt) { Animatable(if (now() - restNow.startedAt < RISE_WINDOW_MS) 0f else 1f) }
            LaunchedEffect(rise) { if (rise.value < 1f) { delay(150); rise.animateTo(1f, if (reduceMotion) Motion.fade() else Motion.calm()) } }
            RestCard(restNow, nowMs, haze, onChange = { graph.rest.value = it },
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = StriveTheme.spacing.s16).graphicsLayer {
                    alpha = rise.value.coerceIn(0f, 1f)
                    if (!reduceMotion) translationY = (1f - rise.value) * 16.dp.toPx()
                })
        }
        if (open != null) {
            // A tap anywhere outside the dock closes it (#17 D3).
            Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { edit(null) })
        }
        Overlay(
            open != null, Modifier.align(Alignment.BottomCenter),
            enter = if (reduceMotion) fadeIn(Motion.fade()) else slideInVertically(Motion.settle(IntOffset.VisibilityThreshold)) { it },
            exit = if (reduceMotion) fadeOut(Motion.fade()) else slideOutVertically(Motion.settle(IntOffset.VisibilityThreshold)) { it },
        ) {
            // Keeps showing the set it closed on while it slides away.
            val shown = remember { mutableStateOf(open) }
            if (open != null) shown.value = open
            val (e, i) = shown.value ?: return@Overlay
            val set = e.sets.getOrNull(i) ?: return@Overlay
            Dock(e, e.unitIn(defaultUnit), set, defaultRest, haze, onSave = { if (editRest) saveRest(it) else log(it); edit(null) }, onClose = { edit(null) },
                field = editField, logs = !editRest)
        }
        val toast by toasts.shown.collectAsState()
        ToastOverlay(toast, toasts::undo)

        // In the screen's glass source, so the sheet and the alert blur the list behind them.
        if (finishing) {
            val left = w.sets - w.logged
            ActionSheet(
                pluralStringResource(Res.plurals.unlogged, left, left),
                listOf(
                    SheetAction(stringResource(Res.string.log_and_save)) { finishing = false; finishAs(Finish.LogAsTheyStand) },
                    SheetAction(stringResource(Res.string.save_without)) { finishing = false; finishAs(Finish.WithoutThem) },
                    // Red, with no alert after it: Avsluta was the intentional action, not discarding (#16).
                    SheetAction(stringResource(Res.string.discard_workout), destructive = true) { finishing = false; discard() },
                ),
                stringResource(Res.string.keep_going), onDismiss = { finishing = false },
            )
        }
        if (discarding) {
            Alert(
                stringResource(Res.string.discard_question), stringResource(Res.string.discard_body), stringResource(Res.string.discard_workout),
                onConfirm = { discarding = false; discard() }, onDismiss = { discarding = false },
            )
        }
    } } }
}

/**
 * The workout's one toast (P4): Restored under the bar (WO-02-s16), Removed with its Undo and Swapped 40 above the bottom (WO-02-removed,
 * WO-02-swap). Its hold runs on the graph, so this only draws it.
 */
@Composable
private fun BoxScope.ToastOverlay(toast: WorkoutToast?, onUndo: () -> Unit) {
    // Keeps showing the toast it had while it fades away.
    val shown = remember { mutableStateOf(toast) }
    if (toast != null) shown.value = toast
    val t = shown.value
    // Composed with no toast too, so the first one fades in.
    Overlay(
        toast != null,
        // Under the 52 pt bar, as the board's 106 is past its 54 pt status bar.
        if (t is WorkoutToast.Restored) Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(start = StriveTheme.spacing.s16, top = 52.dp, end = StriveTheme.spacing.s16).fillMaxWidth()
        else Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        fadeIn(Motion.fade()), fadeOut(Motion.fade()),
    ) {
        when (t) {
            null -> {}
            WorkoutToast.Restored -> Toast(stringResource(Res.string.restored), Modifier.fillMaxWidth(), StriveIcons.Check, iconSize = 18.dp)
            is WorkoutToast.Removed -> Toast(stringResource(Res.string.exercise_removed, t.name), icon = StriveIcons.Trash,
                action = stringResource(Res.string.undo), onAction = onUndo)
            is WorkoutToast.Swapped -> Toast(stringResource(Res.string.swapped_for, t.name), icon = StriveIcons.Swap)
        }
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
private fun TitleBar(w: Workout, nowMs: Long, showTitle: Boolean, glassed: Boolean, hairline: Boolean, onMinimise: () -> Unit, onFinish: () -> Unit, drag: Modifier,
                     onRest: (Int) -> Unit) {
    val alpha by animateFloatAsState(if (showTitle) 1f else 0f, Motion.fade())
    val colors = MaterialTheme.colorScheme
    val touch = StriveTheme.sizes.touch
    Column(drag.testTag("title-bar").fillMaxWidth().background(if (glassed) StriveTheme.glass.thick else colors.background)) {
        // `.nav`'s `1fr auto 1fr` with its 8 dp gap: the buttons keep their size and the name takes the room left between them.
        CentredBar(
            StriveTheme.spacing.s8,
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
                // `.wtb`'s middle is 46 pt (name 24, gap 2, sub line 20) in a 50 pt bar, so it runs 2 pt into the 6 pt foot, where the
                // boards put the strip (104 pt). The bar keeps 50 and the buttons centre on the 46. A taller middle (a larger text size)
                // grows the bar and still runs only those 2 pt in, so the foot keeps the board's visible 4 pt.
                .layout { m, c ->
                    val bar = m.measure(c)
                    val height = maxOf(touch.roundToPx(), bar.height - 2.dp.roundToPx())
                    layout(bar.width, height) { bar.place(0, 0) }
                },
            start = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconTarget(stringResource(Res.string.minimise), onMinimise) { Glyph(Glyph.Down, 22.dp, stroke = 2.2f, color = colors.onSurface) }
                    RestButton(onRest)
                }
            },
            middle = {
                Column(Modifier.alpha(alpha), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (alpha > 0f) {
                        WorkoutMenu(w, large = false, nowMs = nowMs)
                        // The board's `.wtb .mid small` resets `tabular-nums`: proportional figures, so the centred line shifts as the clock ticks.
                        Text(progress(w, nowMs), style = MaterialTheme.typography.bodyMedium.board(), color = colors.onSurfaceVariant)
                    }
                }
            },
            end = {
                val all = w.sets > 0 && w.logged == w.sets
                Box(Modifier.height(StriveTheme.sizes.touch), contentAlignment = Alignment.Center) {
                    Btn(onFinish, Modifier.height(40.dp), container = if (all) colors.primary else colors.surfaceContainerHigh,
                        content = if (all) colors.onPrimary else colors.onSurface, shape = ShapeFull) {
                        Text(stringResource(Res.string.finish), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleSmall)
                    }
                }
            },
        )
        if (glassed && hairline) HorizontalDivider(thickness = 0.5.dp, color = colors.outlineVariant)
    }
}

/**
 * The exercise's name, which folds it (Carl, point 3), with its ⋯ (choice 4's M2 with two tiles) and a chevron; under them the notes in
 * grey, pin, Plan and Previous (last time's note, #42 P2), and today's note once one is opened (choice 6). [plan] is the template's
 * cue, which nothing writes until #44 (P3).
 */
@Composable
private fun ExerciseHeader(
    w: Workout, e: SessionExercise, defaultRest: Int, letter: String?, onFold: () -> Unit, onSwap: () -> Unit, modifier: Modifier,
    plan: String? = null,
) {
    val graph = LocalAppGraph.current
    val sessions = graph.sessions
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    var restMenu by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<String?>(null) }
    var noteOpen by remember { mutableStateOf(false) }
    var note by remember(e.id) { mutableStateOf(e.note) }
    val previousNote by remember(e.exerciseId, w.id) { sessions.previousNote(e.exerciseId, w.id) }.collectAsState(null)
    val fold = stringResource(Res.string.collapse, e.name)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
            letter?.let { Tag(it) }
            Text(e.name, Modifier.weight(1f).clickable(onClickLabel = fold, onClick = onFold).padding(vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium.board(), color = colors.primary)
            Box {
                MorePill(stringResource(Res.string.exercise_options), { menu = true })
                GlassMenu(menu, { menu = false; restMenu = false }) {
                    if (!restMenu) {
                        MenuTiles(
                            MenuTile(StriveIcons.Note, stringResource(Res.string.note)) { menu = false; noteOpen = true },
                            MenuTile(StriveIcons.Swap, stringResource(Res.string.swap)) { menu = false; onSwap() },
                        )
                        MenuRow(stringResource(Res.string.sticky_note), { menu = false; dialog = "pin" }, icon = StriveIcons.Pin)
                        MenuRow(stringResource(Res.string.rest_all), { restMenu = true }, tail = "›")
                        // Choice 4's Skapa superset; in a superset, the way out of it.
                        if (letter != null) {
                            MenuRow(stringResource(Res.string.superset_leave), { menu = false; scope.launch { sessions.leaveSuperset(e.id) } })
                        } else if (w.exercises.size > 1) {
                            MenuRow(stringResource(Res.string.superset_create), { menu = false; dialog = "superset" })
                        }
                        MenuRow(stringResource(Res.string.order_skip), { menu = false; dialog = "order" })
                        if (e.type == ExerciseType.WeightReps) {
                            MenuRow(stringResource(Res.string.exercise_settings), { menu = false; dialog = "settings" })
                        }
                        // Hidden behind a toast with Undo; the rows go when the toast ends (P4).
                        MenuRow(stringResource(Res.string.remove_exercise), { menu = false; graph.toasts.show(WorkoutToast.Removed(e.name, e.id)) },
                            icon = StriveIcons.Trash, danger = true)
                    } else {
                        MenuRow(stringResource(Res.string.rest_all), { restMenu = false }, back = true)
                        val all = e.sets.map { restOf(it, defaultRest) }.distinct().singleOrNull()
                        for (r in listOf(60, 90, 120, 150, 180)) {
                            MenuRow(clock(r.toLong()), { menu = false; restMenu = false; scope.launch { sessions.setRestAll(e.id, r) } }, checked = all == r)
                        }
                    }
                }
            }
            // `.fold` draws 32 × 32; taps land across 44 pt centred on it, hanging over its neighbours as the house ⋯ pill's do.
            Box(
                Modifier.size(32.dp).requiredSize(StriveTheme.sizes.touch).clickable(onClickLabel = fold, onClick = onFold)
                    .semantics { contentDescription = fold }.wrapContentSize().size(32.dp).clip(CircleShape).background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) { Glyph(Glyph.Up, 14.dp, color = colors.onSurface, stroke = 2.6f) }
        }
        if (e.pinned.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Glyph(Glyph.Pin, 14.dp, color = colors.onSurfaceVariant, stroke = 1.8f)
                Text(e.pinned, style = MaterialTheme.typography.bodySmall.board(), color = colors.onSurfaceVariant)
            }
        }
        plan?.let { ExerciseNote(stringResource(Res.string.note_plan), it) }
        previousNote?.let { ExerciseNote(stringResource(Res.string.note_previous), it) }
        if (noteOpen || note.isNotEmpty()) {
            val focus = remember { FocusRequester() }
            InputField(
                note, { note = it; scope.launch { sessions.setExerciseNote(e.id, it) } },
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp).focusRequester(focus),
                placeholder = stringResource(Res.string.note_today),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            // Opened from the menu, the field takes the caret at once (WO-02-note).
            LaunchedEffect(noteOpen) { if (noteOpen) focus.requestFocus() }
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

/** `.exnote` with its bold label: "Plan Skulderbladen ihop.", "Previous Kändes stark idag." */
@Composable
private fun ExerciseNote(label: String, text: String) {
    val grey = MaterialTheme.colorScheme.onSurfaceVariant
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val style = MaterialTheme.typography.bodySmall.board()
        Text(label, Modifier.alignByBaseline(), style = style.copy(fontSize = 12.sp, lineHeight = 16.sp), fontWeight = FontWeight.SemiBold, color = grey)
        Text(text, Modifier.alignByBaseline(), style = style, color = grey)
    }
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
    HeaderSheet(stringResource(Res.string.order_skip), onDone, done = stringResource(Res.string.done)) {
        Column(verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4)) {
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
        }
    }
}

/** Skapa superset: the workout's other exercises; the one tapped joins this one in a superset, right after it. */
@Composable
private fun SupersetDialog(w: Workout, e: SessionExercise, onDone: () -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val scope = rememberCoroutineScope()
    AlertBox(onDone) {
        Text(stringResource(Res.string.superset_create), Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        for (other in w.exercises.filter { it.id != e.id && !it.skipped }) {
            Text(other.name, Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                .clickable { scope.launch { sessions.superset(e.id, other.id) }; onDone() }
                .heightIn(min = StriveTheme.sizes.touch).padding(horizontal = StriveTheme.spacing.s8, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge)
        }
        AlertButton(stringResource(Res.string.cancel), MaterialTheme.colorScheme.primary, onDone, Modifier.fillMaxWidth())
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
    // A tap on the scrim closes without saving, as dismissing the dialog did.
    HeaderSheet(stringResource(Res.string.exercise_settings), onDismiss = onDone, done = stringResource(Res.string.done),
        onDone = { scope.launch { exercises.setSettings(e.exerciseId, step, bar, e.unit) }; onDone() }) {
        Text(stringResource(Res.string.step), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Segments(steps.map { kilos(it, swedish) }, steps.indexOf(step).takeIf { it >= 0 }, { i -> i?.let { step = steps[it] } }, clearable = false)
        Text(stringResource(Res.string.bar), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val none = stringResource(Res.string.no_bar)
        Segments(bars.map { it?.let { b -> kilos(b, swedish) } ?: none }, bars.indexOf(bar).takeIf { it >= 0 }, { i -> i?.let { bar = bars[it] } }, clearable = false)
    }
}

/**
 * The strip's height under the bar, `.stripwrap` on WO-03-strip (104 to 169.7 pt). shortcut: a constant, not the strip measured; measure
 * it if the strip's type or padding change.
 */
private val StripHeight = 66.dp

/**
 * How new a rest must be for its card to rise. A log shows the card within a frame, so this only has to be shorter than reopening the
 * dock takes.
 */
private const val RISE_WINDOW_MS = 500L

/** The focus strip under the bar while the focus card is out of sight (D-scroll), on the bar's glass. */
@Composable
private fun StripOverlay(shown: Boolean, focus: Pair<SessionExercise, Int>?, defaultUnit: WeightUnit, rir: Int?, tag: String?, modifier: Modifier, onBack: () -> Unit) {
    val reduceMotion = StriveTheme.reduceMotion
    Overlay(
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
                    Box(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) { FocusStrip(e, e.unitIn(defaultUnit), it, badges(e.sets)[i], rir, onBack, tag) }
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
