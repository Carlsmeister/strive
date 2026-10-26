package se.carlsmeister.strive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.back
import strive.shared.generated.resources.close
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
private data class Session(val title: StringResource) : Route
private data class Screen(val tab: Tab, val depth: Int)

// shortcut: back stacks are plain lists, so Android loses them when it recreates the activity (rotation,
// a language change) or kills the process; switch to rememberNavBackStack with serializable keys when a
// feature has state worth restoring.
@Composable
fun App() {
    MaterialTheme {
        val root = remember { mutableStateListOf<Route>(Tabs) }
        // Held here, not in TabsScreen, which leaves the composition while a session is open.
        val stacks = remember { Tab.entries.associateWith { mutableStateListOf(Screen(it, 1)) } }
        val selected = remember { mutableStateOf(Tab.Home) }
        NavDisplay(
            backStack = root,
            onBack = { root.removeLastOrNull() },
            entryProvider = { route ->
                when (route) {
                    Tabs -> NavEntry(route) { TabsScreen(stacks, selected, onStart = { root += Session(it) }) }
                    is Session -> NavEntry(route) {
                        Placeholder(stringResource(route.title), Modifier.safeContentPadding(), close = { root.removeLastOrNull() })
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsScreen(
    stacks: Map<Tab, SnapshotStateList<Screen>>,
    selectedState: MutableState<Tab>,
    onStart: (StringResource) -> Unit,
) {
    var selected by selectedState
    var sheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                @Composable
                fun item(tab: Tab) = NavigationBarItem(
                    selected = tab == selected,
                    onClick = { selected = tab },
                    icon = {},
                    label = { Text(stringResource(tab.title)) },
                )
                item(Tab.Home)
                item(Tab.Food)
                NavigationBarItem(
                    selected = false,
                    onClick = { sheet = true },
                    icon = { Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) },
                    label = { Text(stringResource(Res.string.tab_start)) },
                )
                item(Tab.Plan)
                item(Tab.You)
            }
        },
    ) { padding ->
        val stack: SnapshotStateList<Screen> = stacks.getValue(selected)
        // A fresh display per tab, so switching tabs swaps stacks instead of animating as a push.
        key(selected) {
            NavDisplay(
                backStack = stack,
                modifier = Modifier.padding(padding),
                onBack = { if (stack.size > 1) stack.removeLast() },
                entryProvider = { screen ->
                    NavEntry(screen) {
                        val title = stringResource(screen.tab.title)
                        Placeholder(
                            if (screen.depth == 1) title else "$title ${screen.depth}",
                            back = if (screen.depth > 1) ({ stack.removeLast() }) else null,
                            next = { stack += Screen(screen.tab, screen.depth + 1) },
                        )
                    }
                },
            )
        }
    }

    if (sheet) {
        ModalBottomSheet(onDismissRequest = { sheet = false }) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (title in listOf(Res.string.start_workout, Res.string.start_run)) {
                    Button(onClick = { sheet = false; onStart(title) }, Modifier.fillMaxWidth()) { Text(stringResource(title)) }
                }
            }
        }
    }
}

@Composable
private fun Placeholder(
    title: String,
    modifier: Modifier = Modifier,
    back: (() -> Unit)? = null,
    close: (() -> Unit)? = null,
    next: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        back?.let { TextButton(it) { Text(stringResource(Res.string.back)) } }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        next?.let { Button(it) { Text(stringResource(Res.string.next_screen)) } }
        close?.let { TextButton(it) { Text(stringResource(Res.string.close)) } }
    }
}
