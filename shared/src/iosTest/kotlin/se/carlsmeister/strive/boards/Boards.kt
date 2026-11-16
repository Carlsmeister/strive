package se.carlsmeister.strive.boards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.memoryDriver
import se.carlsmeister.strive.fixture.SampleData
import se.carlsmeister.strive.theme.LocalDarkTheme
import se.carlsmeister.strive.theme.LocalHighContrast
import se.carlsmeister.strive.theme.LocalReduceTransparency
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.workout.wallClock
import kotlin.time.Clock
import platform.Foundation.NSUserDefaults

/** The four modes a board is drawn in, named as the boards' `data-theme` names them. */
internal enum class Mode(val id: String, val dark: Boolean, val highContrast: Boolean) {
    Dark("dark", true, false), DarkHc("dark-hc", true, true), Light("light", false, false), LightHc("light-hc", false, true)
}

/** Carl's phone runs dark-HC: the modes checked first. */
internal val DarkModes = listOf(Mode.Dark, Mode.DarkHc)

// shortcut: shots go to a fixed folder outside the repo, for a person to read; pass a path from Gradle if CI should keep them.
internal const val BOARD_SHOTS = "/tmp/strive-board-shots"

/**
 * Shoots [content] on Carl's sample data at 390 × 844 pt and density 3 (R9), once per mode, as `[BOARD_SHOTS]/[id]-<mode>.png`.
 * [arrange] runs once after the first idle, before the first shot: a tap or a write that brings the board's state on screen.
 * [waitFor] is a text the board shows once arranged and its data has arrived, in every mode: reads run on Dispatchers.IO, which
 * waitForIdle does not wait for. In the first mode it is awaited after [arrange], so it may name what only [arrange] brings.
 * Shots are drawn in English, as the boards are, whatever the simulator's language; the flow tests keep the simulator's own.
 */
@OptIn(ExperimentalTestApi::class)
internal fun boardShots(
    id: String,
    modes: List<Mode>,
    waitFor: String? = null,
    reduceTransparency: Boolean = false,
    arrange: suspend ComposeUiTest.(AppGraph) -> Unit = {},
    content: @Composable () -> Unit,
) = inEnglish {
    try {
        runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
            val graph = AppGraph(memoryDriver())
            SampleData.load(graph)
            var mode by mutableStateOf(modes.first())
            setContent {
                CompositionLocalProvider(
                    LocalAppGraph provides graph, LocalDarkTheme provides mode.dark, LocalHighContrast provides mode.highContrast,
                    LocalReduceTransparency provides reduceTransparency,
                ) {
                    StriveTheme { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { content() } }
                }
            }
            for ((i, m) in modes.withIndex()) {
                mode = m
                if (i == 0) {
                    waitForIdle()
                    arrange(graph)
                }
                if (waitFor != null) waitUntil("see '$waitFor'", 10_000) { onAllNodes(hasText(waitFor, substring = true)).fetchSemanticsNodes().isNotEmpty() }
                waitForIdle()
                savePng(BOARD_SHOTS, "$id-${m.id}")
            }
        }
    } finally {
        wallClock = Clock.System
    }
}

/**
 * Runs [block] with Compose resources resolving English, as the boards are drawn, then hands back the simulator's language.
 * Compose resources reads NSLocale.preferredLanguages, which follows AppleLanguages; the app's own domain overrides the
 * simulator's global one. Restoring removes the key rather than writing back what objectForKey read: that is the resolved
 * value, and writing it pinned it in the app's persistent domain, so one run left every later run in English.
 */
internal fun <T> inEnglish(block: () -> T): T {
    clearLeftOverLanguage()
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setObject(listOf("en"), APPLE_LANGUAGES)
    try {
        return block()
    } finally {
        defaults.removeObjectForKey(APPLE_LANGUAGES)
    }
}

// The simulator does not reliably write the removal to test.kexe.plist before the test process ends (even after synchronize()),
// while the English before it can land there; the next run then started in English. So each process clears it once, first.
private val leftOverCleared by lazy { NSUserDefaults.standardUserDefaults.removeObjectForKey(APPLE_LANGUAGES) }

/** Hands the simulator's own language back if an earlier test process left the boards' English behind; once per process. */
internal fun clearLeftOverLanguage() = leftOverCleared

private const val APPLE_LANGUAGES = "AppleLanguages"
