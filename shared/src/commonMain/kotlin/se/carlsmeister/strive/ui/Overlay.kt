package se.carlsmeister.strive.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Whether a transition driven by this state belongs in the composition: while it shows, and while it moves, an enter cut short
 * included. Out of it once its exit ends, so its frame loop goes with it: one left there can ask for a frame for good (#57).
 */
internal val MutableTransitionState<Boolean>.showsOrMoves: Boolean get() = currentState || targetState || !isIdle

/**
 * `AnimatedVisibility` in the composition only while it shows or moves, an enter cut short included: the workout's dock and strip,
 * and [WorkoutBar]'s name. Left there for good, its transition can go on asking for a frame, every frame, after it has ended. Compose
 * keeps that loop by a `derivedStateOf`, which trusts a hash of its inputs' records; on iOS the hash repeats when the overlay hides at
 * one count of snapshots after it showed (`OverlayTest`), and the loop is never taken away (#57). Out of the composition, the loop
 * goes with it.
 */
@Composable
internal fun Overlay(visible: Boolean, modifier: Modifier, enter: EnterTransition, exit: ExitTransition, content: @Composable AnimatedVisibilityScope.() -> Unit) {
    val state = remember { MutableTransitionState(visible) }
    state.targetState = visible
    if (state.showsOrMoves) AnimatedVisibility(state, modifier, enter, exit, content = content)
}
