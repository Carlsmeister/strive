package se.carlsmeister.strive.ui

import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.FixedTextSize
import se.carlsmeister.strive.theme.LocalGlassSource
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.StriveMaterials
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass

internal enum class StartChoice { Continue, Empty, Run }

/** With a workout open, Start only continues it (SH-03-inprog), so nothing starts a second one. */
internal fun startChoices(workoutOpen: Boolean) =
    if (workoutOpen) listOf(StartChoice.Continue) else listOf(StartChoice.Empty, StartChoice.Run)

/** The bubbles' centres from Start's centre, raised by [lift] (P2: SH-03-today, SH-03-run, SH-03-inprog; two take the inner pair). */
internal fun bubbleCentres(count: Int, lift: Dp = 0.dp): List<DpOffset> {
    require(count in 1..4) { "Start has room for one to four bubbles, not $count" }
    val arc = when (count) {
        1 -> listOf(0 to -160)
        2 -> listOf(-47 to -157, 47 to -157)
        3 -> listOf(-112 to -106, 0 to -164, 112 to -106)
        else -> listOf(-112 to -106, -47 to -157, 47 to -157, 112 to -106)
    }
    return arc.map { (x, y) -> DpOffset(x.dp, y.dp - lift) }
}

/** MO-start's turn of the plus and growth of the panel: 350 ms, on CSS's `ease`, the curve its 120 ms frame (30°, .85) sits on. */
internal fun <T> startOpening(): TweenSpec<T> = tween(350, easing = Ease)

private val BubbleSize = 56.dp

/** One bubble: [label] and [sub] under its [icon]; [filled] is `.bub.main`; [description] is what a screen reader says. */
internal class BubbleItem(
    val icon: ImageVector,
    val label: String,
    val sub: String? = null,
    val filled: Boolean = false,
    val description: String = label,
    val onClick: () -> Unit,
)

/**
 * `.bub`: a 56 dp circle of thick material with edge and shadow (primary, no edge, when filled), its 24 dp icon in the middle and
 * its label 6 dp under it, one line of at most 160 dp. The click and description sit on the circle.
 */
@Composable
internal fun Bubble(item: BubbleItem, modifier: Modifier = Modifier) = FixedTextSize {
    val colors = MaterialTheme.colorScheme
    val materials = StriveTheme.materials
    // `.bub` sets no backdrop filter and `.no-glass` leaves it be: the scheme's thick tint laid over the panel, never blurred or solid.
    val fill = if (item.filled) StriveMaterials(colors.primary, colors.primary, Color.Transparent, materials.shadow) else materials
    Column(modifier.widthIn(max = 160.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(BubbleSize).pressable(true, item.onClick).semantics { contentDescription = item.description }
                .glass(null, fill, thick = true, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, null, Modifier.size(StriveTheme.sizes.icon), tint = if (item.filled) colors.onPrimary else colors.onSurface)
        }
        // The board's three text-shadows in the background colour, as one glow; `font:` resets `.strive`'s tabular-nums.
        val glow = Shadow(colors.background, blurRadius = with(LocalDensity.current) { 6.dp.toPx() })
        val small = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox, shadow = glow)
        val label = small.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight(600))
        // The label is inside `.bub`'s link, so a tap on it is the bubble's; a pointer handler, not a click, keeps the bubble's one
        // description and action on the circle.
        val onClick by rememberUpdatedState(item.onClick)
        Column(
            Modifier.padding(top = 6.dp).pointerInput(Unit) { detectTapGestures { onClick() } },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(item.label, color = colors.onSurface, style = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            item.sub?.let { Text(it, color = colors.onSurfaceVariant, style = small, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

/** `.bpanel`: thick glass with 28 dp corners, edge and shadow, veiled in on-surface; solid under Reduce Transparency. */
@Composable
internal fun BubblePanel(modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.extraLarge
    val veil = MaterialTheme.colorScheme.onSurface.copy(alpha = StriveTheme.states.veilOpacity)
    // `::before`'s veil fills the padding box, inside the hairline edge.
    Box(modifier.glass(LocalGlassSource.current, StriveTheme.glass, thick = true, shape = shape).padding(StriveTheme.borders.hairline).background(veil, shape))
}

/**
 * Start open (SH-03, MO-start): [items] as bubbles in the arc over Start on a panel sized to them, over a clear scrim that closes
 * Start with [closeLabel]. Fills its parent; Start's centre is [startAbove] over its bottom edge, centred, and the arc is raised by
 * [lift]. Opening, the panel grows from .6 and each bubble springs from Start's centre 40 ms after the one before; closing, all
 * return together; under Reduce Motion they only fade.
 */
@Composable
internal fun StartBubbles(
    open: Boolean,
    items: List<BubbleItem>,
    startAbove: Dp,
    lift: Dp,
    closeLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = open
    if (!state.currentState && !state.targetState) return
    // Closing keeps the bubbles that were open, so the workout a bubble starts does not swap them while they fly back.
    val frozen = remember(open) { items }
    val shown = if (open) items else frozen
    val transition = rememberTransition(state, "start")
    val still = StriveTheme.reduceMotion
    val release = Motion.release<Float>()
    val panel = transition.animateFloat({ if (targetState && !still) startOpening() else Motion.fade() }, "panel") { if (it) 1f else 0f }
    val bubbles = shown.indices.map { i ->
        transition.animateFloat(
            { if (targetState && !still) tween(release.durationMillis, i * Motion.staggerMillis, release.easing) else Motion.fade() },
            "bubble $i",
        ) { if (it) 1f else 0f }
    }
    val centres = bubbleCentres(shown.size, lift)
    // One act per opening: a tap during the close, even a second tap in the same frame as the first, does nothing.
    val acted = remember(open) { mutableStateOf(!open) }
    fun act(action: () -> Unit) {
        if (acted.value) return
        acted.value = true
        action()
    }
    Box(modifier.fillMaxSize().testTag("start-bubbles")) {
        // `.scrim.clear`: a tap anywhere off the panel closes Start; gone as Start closes, so the screen behind takes taps again.
        if (open) Box(Modifier.fillMaxSize().clickable(null, null, onClickLabel = closeLabel) { act(onDismiss) })
        Layout(
            content = {
                BubblePanel(
                    // The panel lies over the scrim, as on the board: a tap on it, between the bubbles, does nothing.
                    // Only while open: closing, the panel shrinks inside its unscaled bounds, and the screen behind takes taps again.
                    (if (open) Modifier.pointerInput(Unit) { detectTapGestures { } } else Modifier).graphicsLayer {
                        val p = panel.value
                        alpha = p.coerceIn(0f, 1f)
                        // Faded per drawing, not offscreen: an offscreen layer cuts the shadow off at the layer's edge.
                        compositingStrategy = CompositingStrategy.ModulateAlpha
                        if (!still) {
                            scaleX = .6f + .4f * p
                            scaleY = scaleX
                            transformOrigin = TransformOrigin(.5f, 1f)
                        }
                    },
                )
                shown.forEachIndexed { i, item ->
                    Bubble(
                        BubbleItem(item.icon, item.label, item.sub, item.filled, item.description) { act(item.onClick) },
                        Modifier.graphicsLayer {
                            val p = bubbles[i].value
                            alpha = p.coerceIn(0f, 1f)
                            compositingStrategy = CompositingStrategy.ModulateAlpha
                            if (!still) {
                                // From Start's centre, growing about the circle's centre.
                                translationX = -centres[i].x.toPx() * (1 - p)
                                translationY = -centres[i].y.toPx() * (1 - p)
                                scaleX = .2f + .8f * p
                                scaleY = scaleX
                                transformOrigin = TransformOrigin(.5f, BubbleSize.toPx() / 2 / size.height)
                            }
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { measurables, constraints ->
            val bubblesPlaced = measurables.drop(1).map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            val half = BubbleSize.roundToPx() / 2
            val startX = constraints.maxWidth / 2
            val startY = constraints.maxHeight - startAbove.roundToPx()
            // Each bubble by its circle's centre, as `.bub`'s −28 margins place it; its column centres the circle over a wider label.
            val spots = bubblesPlaced.mapIndexed { i, b ->
                IntOffset(startX + centres[i].x.roundToPx() - ((b.width - 2 * half) / 2f).roundToInt() - half, startY + centres[i].y.roundToPx() - half)
            }
            // The panel holds the bubbles and their labels with 12 dp all round, as SH-03-today and SH-03-inprog draw it.
            val pad = 12.dp.roundToPx()
            val left = spots.minOf { it.x } - pad
            val top = spots.minOf { it.y } - pad
            val right = spots.indices.maxOf { spots[it].x + bubblesPlaced[it].width } + pad
            val bottom = spots.indices.maxOf { spots[it].y + bubblesPlaced[it].height } + pad
            val panelPlaced = measurables[0].measure(Constraints.fixed(right - left, bottom - top))
            layout(constraints.maxWidth, constraints.maxHeight) {
                panelPlaced.place(left, top)
                bubblesPlaced.forEachIndexed { i, b -> b.place(spots[i]) }
            }
        }
    }
}
