package se.carlsmeister.strive.workout

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OverlayTest {
    /**
     * An `AnimatedVisibility` left in the composition asked for a frame for good when it hid exactly 1 240 snapshots after it showed,
     * and a flow test that closed the dock at that count never went idle (#57). 1 240 is 31 times the 40 bytes between a state's two
     * records on iOS; every multiple of 31 × 8 up to 31 × 72 is tried, eight to each side, in case that size changes.
     */
    @Test
    fun anOverlayHiddenAnyNumberOfSnapshotsAfterItShowedLeavesTheScreenIdle() = runSkikoComposeUiTest(Size(200f, 400f), Density(1f)) {
        var visible by mutableStateOf(false)
        setContent {
            Box(Modifier.fillMaxSize()) {
                Overlay(visible, Modifier.align(Alignment.BottomCenter), slideInVertically(tween(48)) { it }, slideOutVertically(tween(48)) { it }) {
                    Box(Modifier.size(50.dp))
                }
            }
        }
        val spare = mutableStateOf(0)
        fun settle() {
            var frames = 0
            while (hasPendingWork() && frames++ < 60) mainClock.advanceTimeByFrame()
        }

        /** Shows the overlay, uses up [snapshots] snapshot ids and hides it; the ids used from shown to hidden. */
        fun showAndHide(snapshots: Long): Long {
            visible = true
            settle()
            val shown = Snapshot.current.snapshotId
            for (i in 0 until snapshots) {
                spare.value++
                Snapshot.sendApplyNotifications()
            }
            visible = false
            settle()
            return Snapshot.current.snapshotId - shown
        }
        val least = showAndHide(0)
        val stillBusyAfter = (1..9).flatMap { size -> (-8L..8L).map { 248L * size + it } }.filter { gap ->
            showAndHide(gap - least)
            hasPendingWork()
        }
        assertEquals(emptyList(), stillBusyAfter, "gaps in snapshot ids after which the hidden overlay still asks for frames")
    }
}
