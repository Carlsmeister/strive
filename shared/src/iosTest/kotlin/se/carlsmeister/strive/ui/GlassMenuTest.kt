package se.carlsmeister.strive.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.LocalDarkTheme
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class GlassMenuTest {
    /** A double tap on Swap pushed two library pickers: the rows stayed live through the menu's exit fade. */
    @Test
    fun aRowTappedAgainWhileTheMenuFadesOutDoesNothing() = runSkikoComposeUiTest(Size(1170f, 2532f), Density(3f)) {
        var open by mutableStateOf(false)
        var acts = 0
        setContent {
            CompositionLocalProvider(LocalDarkTheme provides true) { StriveTheme {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 60.dp, end = 16.dp).size(44.dp)) {
                        GlassMenu(open, { open = false }) {
                            MenuTiles(MenuTile(StriveIcons.Note, "Note") {}, MenuTile(StriveIcons.Swap, "Swap") { open = false; acts++ })
                            MenuRow("Remove exercise", { open = false; acts++ })
                        }
                    }
                }
            } }
        }
        for (target in listOf("Swap", "Remove exercise")) {
            acts = 0
            open = true
            waitForIdle()
            mainClock.autoAdvance = false
            onNodeWithText(target).performTouchInput { click() }
            mainClock.advanceTimeByFrame() // closed, the fade under way and the row still on screen
            onNodeWithText(target).performTouchInput { click() }
            mainClock.autoAdvance = true
            waitForIdle()
            assertEquals(1, acts,"$target acted more than once in one opening")
        }
    }
}
