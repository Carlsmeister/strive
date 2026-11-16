package se.carlsmeister.strive.ui

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.StriveTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class OverlayTest {
    /**
     * An `AnimatedVisibility` left in the composition asked for a frame for good when it hid at one count of snapshots after it
     * showed, and a flow test that closed the dock at that count never went idle (#57). This test measures that gap, from the enter's
     * end to the exit's end, as 1 238; Compose's hash makes it 1 240 = 31 × 40. That 40 is the bytes between a state's two records on
     * iOS, and that the identity hash is the address, are inferred from the count, not read in Kotlin/Native's runtime. So every
     * multiple of 31 × 8 up to 31 × 72 is tried, eight to each side, in case that size is another.
     */
    @Test
    fun anOverlayHiddenNearTheFirstNineMultiplesOf248SnapshotsAfterItShowedLeavesTheScreenIdle() = runSkikoComposeUiTest(Size(200f, 400f), Density(1f)) {
        var visible by mutableStateOf(false)
        setContent {
            Box(Modifier.fillMaxSize()) {
                Overlay(visible, Modifier.align(Alignment.BottomCenter), slideInVertically(tween(48)) { it }, slideOutVertically(tween(48)) { it }) {
                    Box(Modifier.size(50.dp))
                }
            }
        }
        assertEquals(emptyList(), busyGapsNearMultiplesOf248 { visible = it }, "gaps in snapshot ids after which the hidden overlay still asks for frames")
    }

    /** WorkoutBar's name and progress fade out through the same gate, so the next screen that adopts the bar cannot hang (#57). */
    @Test
    fun aWorkoutBarsTitleHiddenNearTheFirstNineMultiplesOf248SnapshotsAfterItShowedLeavesTheScreenIdle() = runSkikoComposeUiTest(Size(390f, 200f), Density(1f)) {
        var scrolled by mutableStateOf(false)
        setContent {
            StriveTheme { WorkoutBar({}, {}, {}, title = if (scrolled) "Push B" else null, progress = if (scrolled) "24:13 · 1/14 sets" else null) }
        }
        assertEquals(emptyList(), busyGapsNearMultiplesOf248 { scrolled = it }, "gaps in snapshot ids after which the bar's hidden title still asks for frames")
    }

    /**
     * Shows and hides through [show], using up snapshot ids in between near every multiple of 31 × 8 up to 31 × 72, eight to each
     * side; the gaps after which the screen still asks for frames.
     */
    private fun ComposeUiTest.busyGapsNearMultiplesOf248(show: (Boolean) -> Unit): List<Long> {
        val spare = mutableStateOf(0)
        fun settle() {
            var frames = 0
            while (hasPendingWork() && frames++ < 60) mainClock.advanceTimeByFrame()
        }

        /** Shows, uses up [snapshots] snapshot ids and hides; the ids used from shown to hidden. */
        fun showAndHide(snapshots: Long): Long {
            show(true)
            settle()
            val shown = Snapshot.current.snapshotId
            for (i in 0 until snapshots) {
                spare.value++
                Snapshot.sendApplyNotifications()
            }
            show(false)
            settle()
            return Snapshot.current.snapshotId - shown
        }
        val least = showAndHide(0)
        return (1..9).flatMap { size -> (-8L..8L).map { 248L * size + it } }.filter { gap ->
            showAndHide(gap - least)
            hasPendingWork()
        }
    }

    /** Scrolled past row 1 and back inside the strip's spring, or a set tapped and the outside tapped as the dock rises. */
    @Test
    fun anOverlayHiddenWhileItEntersStaysUntilItsExitEnds() = runSkikoComposeUiTest(Size(200f, 400f), Density(1f)) {
        var visible by mutableStateOf(false)
        var inTree = false
        setContent {
            Box(Modifier.fillMaxSize()) {
                Overlay(visible, Modifier.align(Alignment.BottomCenter), slideInVertically(tween(300)) { it }, slideOutVertically(tween(300)) { it }) {
                    Box(Modifier.size(50.dp))
                    DisposableEffect(Unit) {
                        inTree = true
                        onDispose { inTree = false }
                    }
                }
            }
        }
        visible = true
        repeat(3) { mainClock.advanceTimeByFrame() } // composed, then the enter under way
        visible = false
        mainClock.advanceTimeByFrame()
        assertTrue(inTree, "an overlay hidden while it entered left at once instead of sliding back out")
        var frames = 0
        while (hasPendingWork() && frames++ < 60) mainClock.advanceTimeByFrame()
        assertFalse(inTree, "an overlay whose exit ended stayed in the composition")
    }

    /** A workout screen re-entered already scrolled composes the strip shown. */
    @Test
    fun anOverlayShownFromTheStartIsInPlaceWithoutEntering() = runSkikoComposeUiTest(Size(200f, 400f), Density(1f)) {
        var first: EnterExitState? = null
        setContent {
            Overlay(true, Modifier, slideInVertically(tween(300)) { it }, slideOutVertically(tween(300)) { it }) {
                if (first == null) first = transition.currentState
                Box(Modifier.size(50.dp))
            }
        }
        assertEquals(EnterExitState.Visible, first)
    }
}
