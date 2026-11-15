package se.carlsmeister.strive

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.carlsmeister.strive.ui.BackLabel
import se.carlsmeister.strive.ui.BubbleItem
import se.carlsmeister.strive.ui.StartBubbles
import se.carlsmeister.strive.ui.StartChoice
import se.carlsmeister.strive.ui.startChoices
import strive.shared.generated.resources.bubble_continue
import strive.shared.generated.resources.bubble_empty
import strive.shared.generated.resources.bubble_empty_desc
import strive.shared.generated.resources.name_time
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.workout.HistoryScreen
import se.carlsmeister.strive.workout.BarState
import se.carlsmeister.strive.workout.barState
import se.carlsmeister.strive.workout.progress
import se.carlsmeister.strive.workout.SummaryScreen
import se.carlsmeister.strive.workout.WorkoutScreen
import se.carlsmeister.strive.workout.clock
import se.carlsmeister.strive.workout.now
import se.carlsmeister.strive.workout.rememberNow
import strive.shared.generated.resources.all_activities
import strive.shared.generated.resources.continue_workout
import strive.shared.generated.resources.workout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.StringResource
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.library.ExerciseScreen
import se.carlsmeister.strive.library.LibraryScreen
import se.carlsmeister.strive.theme.StriveTheme
import org.jetbrains.compose.resources.stringResource
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.close
import strive.shared.generated.resources.library
import strive.shared.generated.resources.next_screen
import strive.shared.generated.resources.start_run
import strive.shared.generated.resources.tab_food
import strive.shared.generated.resources.tab_home
import strive.shared.generated.resources.tab_plan
import strive.shared.generated.resources.tab_start
import strive.shared.generated.resources.tab_you
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import se.carlsmeister.strive.theme.LocalGlassSource
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.ui.TabBar
import se.carlsmeister.strive.ui.RestRing
import se.carlsmeister.strive.ui.TabItem
import se.carlsmeister.strive.ui.WorkoutMiniBar
import se.carlsmeister.strive.ui.barBottom
import strive.shared.generated.resources.close_start
import strive.shared.generated.resources.back_to_workout
import strive.shared.generated.resources.bar_left_open
import strive.shared.generated.resources.tap_to_continue

// The navigation from #14: four tabs around a Start button. Every screen is a placeholder until its feature exists.
private enum class Tab(val title: StringResource) {
    Home(Res.string.tab_home), Food(Res.string.tab_food), Plan(Res.string.tab_plan), You(Res.string.tab_you)
}

private sealed interface Route
private data object Tabs : Route
/** A run, still a placeholder until #5. */
private data class Session(val title: StringResource) : Route
private data object ActiveWorkout : Route
private data object PickExercises : Route
private data class SwapExercise(val sessionExerciseId: Long, val exerciseId: Long) : Route
private data class Finished(val id: Long) : Route
private sealed interface Screen
private data class Stub(val tab: Tab, val depth: Int) : Screen
private data object Library : Screen
private data class ExerciseDetail(val id: Long) : Screen
private data object History : Screen
private data class SessionDetail(val id: Long) : Screen

/** The workout rises from the bottom and sinks back to the bar it minimises to, the way Music's player does (Q19). */
private fun rise(reduceMotion: Boolean): Map<String, Any> {
    val spec = Motion.settle(IntOffset.VisibilityThreshold)
    val enter = if (reduceMotion) fadeIn(Motion.fade()) else slideInVertically(spec) { it }
    val exit = if (reduceMotion) fadeOut(Motion.fade()) else slideOutVertically(spec) { it }
    return NavDisplay.transitionSpec { enter togetherWith ExitTransition.KeepUntilTransitionsFinished } +
        NavDisplay.popTransitionSpec { EnterTransition.None togetherWith exit } +
        NavDisplay.predictivePopTransitionSpec { EnterTransition.None togetherWith exit }
}

// removeAt, not removeLast: on Android below 15 removeLast binds to a Java method that does not exist.
internal fun <T> MutableList<T>.pop() {
    if (size > 1) removeAt(lastIndex)
}

// shortcut: back stacks are plain lists, so Android loses them when it recreates the activity (rotation,
// a language change) or kills the process; switch to rememberNavBackStack with serializable keys when a
// feature has state worth restoring.
@Composable
fun App(graph: AppGraph) = CompositionLocalProvider(LocalAppGraph provides graph) {
    LaunchedEffect(graph) { graph.prepare() }
    StriveTheme {
        val root = remember { mutableStateListOf<Route>(Tabs) }
        // Held here, not in TabsScreen, which leaves the composition while a session is open.
        val stacks = remember { Tab.entries.associateWith { mutableStateListOf<Screen>(Stub(it, 1)) } }
        var selected by remember { mutableStateOf(Tab.Home) }
        val sessions = graph.sessions
        // A workout already open as the app starts was left open by the app being killed (R5): its id, until it is opened once.
        var leftOpen by remember { mutableStateOf<Long?>(null) }
        // Whether the first read of the open workout has come in, or a workout was shown before it did.
        var started by remember { mutableStateOf(false) }
        // A workout left open, by ⌄ or by the app being killed, is offered in the bar above the tabs. The first read sets leftOpen
        // in the same step as open, so the bar never shows a frame of the open state before the left-open one.
        val open by remember {
            sessions.open().onEach {
                if (!started) {
                    started = true
                    leftOpen = it?.id
                }
            }
        }.collectAsState(null)
        // Pushed once, however many taps reach it (the bar, Continue, Start).
        fun showWorkout() {
            started = true
            leftOpen = null
            if (root.last() != ActiveWorkout) root += ActiveWorkout
        }
        val scope = rememberCoroutineScope()
        val name = stringResource(Res.string.workout)
        // Two workouts cannot run at once (Q46): Start continues the open one. Under the lock, so two quick taps start one workout
        // and push it once.
        val starting = remember { Mutex() }
        fun startWorkout() {
            scope.launch {
                starting.withLock {
                    if (sessions.open().first() == null) sessions.start(name, now())
                    showWorkout()
                }
            }
        }
        val safe = WindowInsets.safeDrawing.asPaddingValues()
        val reduceMotion = StriveTheme.reduceMotion
        NavDisplay(
            backStack = root,
            onBack = { root.pop() },
            entryProvider = { route ->
                when (route) {
                    Tabs -> NavEntry(route) {
                        TabsScreen(stacks, selected, open, leftOpen = leftOpen != null && leftOpen == open?.id, onSelect = { selected = it },
                            onStartWorkout = ::startWorkout, onStartRun = { root += Session(Res.string.start_run) }, onRestore = ::showWorkout)
                    }
                    is Session -> NavEntry(route) {
                        Page { Placeholder(stringResource(route.title), Modifier.safeContentPadding(), close = { root.pop() }) }
                    }
                    ActiveWorkout -> NavEntry(route, metadata = rise(reduceMotion)) {
                        Page {
                            WorkoutScreen(
                                onMinimise = { root.pop() },
                                onAddExercises = { root += PickExercises },
                                onSwap = { e -> root += SwapExercise(e.id, e.exerciseId) },
                                onFinished = { id -> root[root.lastIndex] = Finished(id) },
                            )
                        }
                    }
                    PickExercises -> NavEntry(route, metadata = rise(reduceMotion)) {
                        Page {
                            fun add(ids: List<Long>, superset: Boolean) = scope.launch {
                                open?.let { sessions.add(it.id, ids, superset) }
                                root.pop()
                            }
                            LibraryScreen(safe, null, onOpen = {}, onBack = { root.pop() }, pick = { add(it, false) }, onSuperset = { add(it, true) })
                        }
                    }
                    is SwapExercise -> NavEntry(route, metadata = rise(reduceMotion)) {
                        Page {
                            LibraryScreen(safe, null, onOpen = {}, onBack = { root.pop() }, swapFor = route.exerciseId, pick = { ids ->
                                scope.launch {
                                    sessions.swap(route.sessionExerciseId, ids.single())
                                    root.pop()
                                }
                            })
                        }
                    }
                    is Finished -> NavEntry(route) { Page { SummaryScreen(route.id, safe, after = true, parent = null, onClose = { root.pop() }) } }
                }
            },
        )
    }
}

@Composable
private fun TabsScreen(
    stacks: Map<Tab, SnapshotStateList<Screen>>,
    selected: Tab,
    open: Workout?,
    leftOpen: Boolean,
    onSelect: (Tab) -> Unit,
    onStartWorkout: () -> Unit,
    onStartRun: () -> Unit,
    onRestore: () -> Unit,
) {
    // Open, Start shows its bubbles over the screen.
    var startOpen by remember { mutableStateOf(false) }
    val tabState = rememberSaveableStateHolder()
    val haze = rememberHazeState()
    val density = LocalDensity.current
    val gap = barBottom(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    var stackHeight by remember { mutableStateOf(0.dp) }
    var tabBarHeight by remember { mutableStateOf(0.dp) }
    // Screens run on under the floating bottom stack; each pads its own content clear of it.
    val padding = PaddingValues(top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding(), bottom = stackHeight + gap)
    // The shell's one clock, ticking only while a workout is open; a second one kept UI tests from going idle.
    val now = if (open != null) rememberNow() else 0L
    val rest by LocalAppGraph.current.rest.collectAsState()

    Box(Modifier.fillMaxSize()) { CompositionLocalProvider(LocalGlassSource provides haze) {
        val stack: SnapshotStateList<Screen> = stacks.getValue(selected)
        // A fresh display per tab, so switching tabs swaps stacks instead of animating as a push; the holder
        // keeps each tab's saved screen state (scroll, drafts) while its display is gone.
        key(selected) {
            tabState.SaveableStateProvider(selected.name) {
                NavDisplay(
                    backStack = stack,
                    modifier = Modifier.fillMaxSize().hazeSource(haze),
                    onBack = { stack.pop() },
                    entryProvider = { screen ->
                        NavEntry(screen) { Page {
                            when (screen) {
                                Stub(Tab.Food, 1) -> FoodScreen(padding)
                                is Stub -> {
                                    val title = stringResource(screen.tab.title)
                                    fun named(depth: Int) = if (depth == 1) title else "$title $depth"
                                    Placeholder(
                                        named(screen.depth),
                                        Modifier.padding(padding),
                                        back = if (screen.depth > 1) named(screen.depth - 1) else null,
                                        onBack = { stack.pop() },
                                        next = { stack += Stub(screen.tab, screen.depth + 1) },
                                        library = if (screen.tab == Tab.Plan && screen.depth == 1) ({ stack += Library }) else null,
                                        activities = if (screen.tab == Tab.You && screen.depth == 1) ({ stack += History }) else null,
                                    )
                                }
                                Library -> LibraryScreen(padding, stringResource(Res.string.tab_plan), onOpen = { stack += ExerciseDetail(it) }, onBack = { stack.pop() })
                                is ExerciseDetail -> ExerciseScreen(screen.id, padding, stringResource(Res.string.library), onBack = { stack.pop() })
                                History -> HistoryScreen(padding, stringResource(Res.string.tab_you), onOpen = { stack += SessionDetail(it) }, onBack = { stack.pop() }, onStart = onStartWorkout)
                                is SessionDetail -> SummaryScreen(screen.id, padding, after = false, parent = stringResource(Res.string.all_activities), onClose = { stack.pop() })
                            }
                        } }
                    },
                )
            }
        }
        // Back closes Start before it reaches the tab's stack; added after the tab's display, so it is asked first.
        NavigationBackHandler(rememberNavigationEventState(NavigationEventInfo.None), isBackEnabled = startOpen, onBackCompleted = { startOpen = false })
        val items = startChoices(open != null).map { choice ->
            when (choice) {
                StartChoice.Continue -> {
                    val w = open!!
                    val time = clock((now - w.startedAt) / 1_000)
                    BubbleItem(StriveIcons.Lift, stringResource(Res.string.bubble_continue), stringResource(Res.string.name_time, w.name, time), filled = true,
                        description = stringResource(Res.string.continue_workout, w.name, time)) { startOpen = false; onRestore() }
                }
                StartChoice.Empty -> BubbleItem(StriveIcons.Plus, stringResource(Res.string.bubble_empty), description = stringResource(Res.string.bubble_empty_desc)) {
                    startOpen = false
                    onStartWorkout()
                }
                StartChoice.Run -> BubbleItem(StriveIcons.Run, stringResource(Res.string.start_run)) { startOpen = false; onStartRun() }
            }
        }
        // Under the bottom stack, as the board's panel (z 4) is under the tab bar (z 5); the arc rises over the workout bar.
        StartBubbles(startOpen, items, startAbove = gap + tabBarHeight / 2, lift = if (open != null) stackHeight - tabBarHeight else 0.dp,
            closeLabel = stringResource(Res.string.close_start), onDismiss = { startOpen = false })
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = gap)
                .onSizeChanged { stackHeight = with(density) { it.height.toDp() } },
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
        ) {
            open?.let { w ->
                val back = stringResource(Res.string.back_to_workout)
                val r = rest
                val left = r?.left(now) ?: 0
                when (barState(leftOpen, left)) {
                    BarState.LeftOpen -> WorkoutMiniBar(stringResource(Res.string.bar_left_open, w.name, clock((now - w.startedAt) / 1_000)),
                        stringResource(Res.string.tap_to_continue), back, onRestore)
                    BarState.Resting -> WorkoutMiniBar(w.name, progress(w, now), back, onRestore, rest = RestRing(left, r!!.seconds))
                    BarState.Open -> WorkoutMiniBar(w.name, progress(w, now), back, onRestore)
                }
            }
            @Composable
            fun item(tab: Tab, icon: ImageVector) = TabItem(icon, stringResource(tab.title), tab == selected) { onSelect(tab) }
            TabBar(
                before = listOf(item(Tab.Home, StriveIcons.Home), item(Tab.Food, StriveIcons.Food)),
                after = listOf(item(Tab.Plan, StriveIcons.Plan), item(Tab.You, StriveIcons.You)),
                startLabel = stringResource(if (startOpen) Res.string.close_start else Res.string.tab_start),
                startOpen = startOpen,
                onStart = { startOpen = !startOpen },
                modifier = Modifier.onSizeChanged { tabBarHeight = with(density) { it.height.toDp() } },
            )
        }
    } }
}

// Opaque, so the screen sliding in covers the one it leaves instead of showing both at once.
@Composable
private fun Page(content: @Composable () -> Unit) {
    // A back swipe that starts mid-push seeks a transition whose start state never settled, and the screens jump.
    // Like UIKit, swallow back until the slide ends; this handler, added after NavDisplay's, is asked first.
    NavigationBackHandler(
        rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = LocalNavAnimatedContentScope.current.transition.isRunning,
        onBackCompleted = {},
    )
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() }
}

@Composable
private fun Placeholder(
    title: String,
    modifier: Modifier = Modifier,
    back: String? = null,
    onBack: () -> Unit = {},
    close: (() -> Unit)? = null,
    next: (() -> Unit)? = null,
    library: (() -> Unit)? = null,
    activities: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxSize().padding(StriveTheme.spacing.s16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s16, Alignment.CenterVertically),
    ) {
        back?.let { BackLabel(it, onBack) }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        next?.let { Button(it) { Text(stringResource(Res.string.next_screen)) } }
        library?.let { Button(it) { Text(stringResource(Res.string.library)) } }
        activities?.let { Button(it) { Text(stringResource(Res.string.all_activities)) } }
        close?.let { TextButton(it) { Text(stringResource(Res.string.close)) } }
    }
}
