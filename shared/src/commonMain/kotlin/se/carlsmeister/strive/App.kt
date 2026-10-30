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
import kotlinx.coroutines.launch
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.workout.HistoryScreen
import se.carlsmeister.strive.workout.MiniBar
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
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
import se.carlsmeister.strive.theme.FixedTextSize
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.glass
import se.carlsmeister.strive.theme.StriveTheme
import org.jetbrains.compose.resources.stringResource
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.back
import strive.shared.generated.resources.close
import strive.shared.generated.resources.library
import strive.shared.generated.resources.next_screen
import strive.shared.generated.resources.start_run
import strive.shared.generated.resources.start_workout
import strive.shared.generated.resources.tab_food
import strive.shared.generated.resources.tab_home
import strive.shared.generated.resources.tab_plan
import strive.shared.generated.resources.tab_start
import strive.shared.generated.resources.tab_you

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
        // A workout left open, by ⌄ or by the app being killed, is offered in the bar above the tabs.
        val open by remember { sessions.open() }.collectAsState(null)
        val scope = rememberCoroutineScope()
        val name = stringResource(Res.string.workout)
        // Two workouts cannot run at once (Q46): Start continues the open one.
        fun startWorkout() {
            scope.launch {
                if (sessions.open().first() == null) sessions.start(name, now())
                root += ActiveWorkout
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
                        TabsScreen(stacks, selected, open, onSelect = { selected = it }, onStartWorkout = ::startWorkout,
                            onStartRun = { root += Session(Res.string.start_run) }, onRestore = { root += ActiveWorkout })
                    }
                    is Session -> NavEntry(route) {
                        Page { Placeholder(stringResource(route.title), Modifier.safeContentPadding(), close = { root.pop() }) }
                    }
                    ActiveWorkout -> NavEntry(route, metadata = rise(reduceMotion)) {
                        Page {
                            WorkoutScreen(
                                onMinimise = { root.pop() },
                                onAddExercises = { root += PickExercises },
                                onFinished = { id -> root[root.lastIndex] = Finished(id) },
                            )
                        }
                    }
                    PickExercises -> NavEntry(route, metadata = rise(reduceMotion)) {
                        Page {
                            LibraryScreen(safe, onOpen = {}, onBack = { root.pop() }, pick = { ids ->
                                scope.launch {
                                    open?.let { sessions.add(it.id, ids) }
                                    root.pop()
                                }
                            })
                        }
                    }
                    is Finished -> NavEntry(route) { Page { SummaryScreen(route.id, safe, after = true, onClose = { root.pop() }) } }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsScreen(
    stacks: Map<Tab, SnapshotStateList<Screen>>,
    selected: Tab,
    open: Workout?,
    onSelect: (Tab) -> Unit,
    onStartWorkout: () -> Unit,
    onStartRun: () -> Unit,
    onRestore: () -> Unit,
) {
    var sheet by remember { mutableStateOf(false) }
    val tabState = rememberSaveableStateHolder()
    val haze = rememberHazeState()

    Scaffold(
        bottomBar = { Column {
            open?.let { MiniBar(it, haze, onRestore) }
            FixedTextSize { NavigationBar(Modifier.glass(haze, StriveTheme.glass), containerColor = Color.Transparent) {
                @Composable
                fun item(tab: Tab) = NavigationBarItem(
                    selected = tab == selected,
                    onClick = { onSelect(tab) },
                    icon = {},
                    label = { Text(stringResource(tab.title), maxLines = 1) },
                )
                item(Tab.Home)
                item(Tab.Food)
                NavigationBarItem(
                    selected = false,
                    onClick = { sheet = true },
                    icon = { Box(Modifier.size(StriveTheme.sizes.touch).background(MaterialTheme.colorScheme.primary, ShapeFull)) },
                    label = { Text(stringResource(Res.string.tab_start), maxLines = 1) },
                )
                item(Tab.Plan)
                item(Tab.You)
            } }
        } },
    ) { padding ->
        val stack: SnapshotStateList<Screen> = stacks.getValue(selected)
        // A fresh display per tab, so switching tabs swaps stacks instead of animating as a push; the holder
        // keeps each tab's saved screen state (scroll, drafts) while its display is gone.
        key(selected) {
            tabState.SaveableStateProvider(selected.name) {
                NavDisplay(
                    backStack = stack,
                    // Screens run on under the glass bar; each pads its own content clear of it.
                    modifier = Modifier.hazeSource(haze),
                    onBack = { stack.pop() },
                    entryProvider = { screen ->
                        NavEntry(screen) { Page {
                            when (screen) {
                                is Stub -> {
                                    val title = stringResource(screen.tab.title)
                                    Placeholder(
                                        if (screen.depth == 1) title else "$title ${screen.depth}",
                                        Modifier.padding(padding),
                                        back = if (screen.depth > 1) ({ stack.pop() }) else null,
                                        next = { stack += Stub(screen.tab, screen.depth + 1) },
                                        library = if (screen.tab == Tab.Plan && screen.depth == 1) ({ stack += Library }) else null,
                                        activities = if (screen.tab == Tab.You && screen.depth == 1) ({ stack += History }) else null,
                                    )
                                }
                                Library -> LibraryScreen(padding, onOpen = { stack += ExerciseDetail(it) }, onBack = { stack.pop() })
                                is ExerciseDetail -> ExerciseScreen(screen.id, padding, onBack = { stack.pop() })
                                History -> HistoryScreen(padding, onOpen = { stack += SessionDetail(it) }, onBack = { stack.pop() }, onStart = onStartWorkout)
                                is SessionDetail -> SummaryScreen(screen.id, padding, after = false, onClose = { stack.pop() })
                            }
                        } }
                    },
                )
            }
        }
    }

    if (sheet) {
        ModalBottomSheet(onDismissRequest = { sheet = false }) {
            Column(Modifier.fillMaxWidth().padding(StriveTheme.spacing.s16), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
                if (open != null) {
                    // With a workout in progress Start offers only to continue it (Q46).
                    val label = stringResource(Res.string.continue_workout, open.name, clock((rememberNow() - open.startedAt) / 1_000))
                    Button(onClick = { sheet = false; onRestore() }, Modifier.fillMaxWidth()) { Text(label) }
                } else {
                    Button(onClick = { sheet = false; onStartWorkout() }, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.start_workout)) }
                    Button(onClick = { sheet = false; onStartRun() }, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.start_run)) }
                }
            }
        }
    }
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
    back: (() -> Unit)? = null,
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
        back?.let { TextButton(it) { Text(stringResource(Res.string.back)) } }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        next?.let { Button(it) { Text(stringResource(Res.string.next_screen)) } }
        library?.let { Button(it) { Text(stringResource(Res.string.library)) } }
        activities?.let { Button(it) { Text(stringResource(Res.string.all_activities)) } }
        close?.let { TextButton(it) { Text(stringResource(Res.string.close)) } }
    }
}
