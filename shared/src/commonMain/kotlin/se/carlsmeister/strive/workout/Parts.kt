package se.carlsmeister.strive.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.delete
import kotlin.math.roundToInt

/** A chevron drawn with two strokes, since the app carries no icon font: down, or right for something that opens. */
@Composable
internal fun Chevron(down: Boolean, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, size: Dp = 16.dp, up: Boolean = false) =
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        val (a, b, c) = if (up) Triple(Offset(w * .15f, h * .65f), Offset(w * .5f, h * .3f), Offset(w * .85f, h * .65f))
            else if (down) Triple(Offset(w * .15f, h * .35f), Offset(w * .5f, h * .7f), Offset(w * .85f, h * .35f))
            else Triple(Offset(w * .35f, h * .15f), Offset(w * .7f, h * .5f), Offset(w * .35f, h * .85f))
        drawLine(color, a, b, stroke, StrokeCap.Round)
        drawLine(color, b, c, stroke, StrokeCap.Round)
    }

/** A 44 pt round target holding a glyph or a drawing, for title bars. */
@Composable
internal fun IconTarget(label: String, onClick: () -> Unit, content: @Composable () -> Unit) =
    Box(
        Modifier.size(StriveTheme.sizes.touch).clip(ShapeFull).clickable(onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { content() }

/**
 * Where a row let go of settles, as Mail's rows do: past half its width it is deleted, a flick left shows Ta bort and a flick right
 * hides it, and otherwise it rests open only once Ta bort is fully shown (MO-gestures: under 88 pt it springs back closed). Speed
 * alone never deletes, so a quick short swipe is safe.
 */
internal fun swipeTarget(offset: Float, velocity: Float, width: Float, open: Float, flick: Float): Float = when {
    offset < -width / 2 -> -width
    velocity < -flick -> open
    velocity > flick -> 0f
    offset <= open -> open
    else -> 0f
}

/** What a swiped row lets its content ask. */
internal class SwipeRow(private val moved: () -> Boolean, private val shut: () -> Unit) {
    /**
     * A tap on the row's content. The finger that swiped is still over the row when it lifts, so a swipe also arrives as a tap;
     * on a row that is open or moving it shuts the row instead, as Mail's rows do.
     */
    fun tap(action: () -> Unit) = if (moved()) shut() else action()
}

/**
 * A row that swipes left to show Ta bort, deleted by a tap on it or by swiping on past half the row: the iOS trailing swipe
 * action with full swipe, as in Mail (#17 choice 4). Its content routes its taps through [SwipeRow.tap].
 */
@Composable
internal fun SwipeToDelete(shape: Shape, onDelete: () -> Unit, content: @Composable SwipeRow.() -> Unit) {
    val density = LocalDensity.current
    var width by remember { mutableIntStateOf(0) }
    var offset by remember { mutableFloatStateOf(0f) }
    val open = with(density) { -(StriveTheme.sizes.touch * 2).toPx() }
    val flick = with(density) { 300.dp.toPx() }
    val drag = rememberDraggableState { delta -> offset = (offset + delta).coerceIn(-width.toFloat(), 0f) }
    val scope = rememberCoroutineScope()
    val row = remember { SwipeRow(moved = { offset != 0f }, shut = { scope.launch { animate(offset, 0f, 0f, Motion.settle()) { v, _ -> offset = v } } }) }
    val haptics = LocalHapticFeedback.current
    Box(Modifier.fillMaxWidth().onSizeChanged { width = it.width }.clip(shape)) {
        Box(
            Modifier.offset { IntOffset(offset.roundToInt(), 0) }
                .draggable(drag, Orientation.Horizontal, onDragStopped = { velocity ->
                    val target = swipeTarget(offset, velocity, width.toFloat(), open, flick)
                    // The finger's speed carries into the spring, so letting go has no seam (apple-motion).
                    animate(offset, target, velocity, Motion.settle()) { value, _ -> offset = value }
                    if (target == -width.toFloat()) {
                        haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) // MO-gestures' medium haptic
                        onDelete()
                    }
                }),
        ) { row.content() }
        // Over the row and only as wide as it uncovers, so its edge is square against the row (WO-14), and a tap or VoiceOver finds the
        // button where it shows. Only while the row moves: at rest the red would show through the antialiased corners as a pink rim.
        // The error token is pale pink in dark mode; its container is the deep red that reads as delete there.
        val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        val red = if (dark) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.error
        val onRed = if (dark) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onError
        // Past half the red fills the row and Delete hugs its left (WO-14-full).
        val full = offset < -width / 2f
        if (offset < 0f) Box(Modifier.matchParentSize(), contentAlignment = Alignment.CenterEnd) {
            Box(
                Modifier.width(with(density) { (-offset).toDp() }).fillMaxHeight().background(red).clickable(onClick = onDelete),
                contentAlignment = if (full) Alignment.CenterStart else Alignment.Center,
            ) {
                Text(stringResource(Res.string.delete), Modifier.padding(start = if (full) 16.dp else 0.dp), color = onRed,
                    style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
            }
        }
    }
}

/** The glyphs design D draws, in its 24-unit viewBox, since the app carries no icon font. */
internal enum class Glyph { Check, Calendar, Clock, Timer, Pin, Note, Swap, Down, Up, Back }

@Composable
internal fun Glyph(glyph: Glyph, size: Dp, modifier: Modifier = Modifier, color: Color = LocalContentColor.current, stroke: Float = 2f) =
    Canvas(modifier.size(size)) {
        val k = this.size.width / 24f
        val style = Stroke(stroke * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(x: Float, y: Float) = Offset(x * k, y * k)
        fun line(vararg xy: Float) = drawPath(Path().apply { moveTo(xy[0] * k, xy[1] * k); for (i in 2 until xy.size step 2) lineTo(xy[i] * k, xy[i + 1] * k) }, color, style = style)
        when (glyph) {
            Glyph.Check -> line(5f, 12.5f, 9.5f, 17f, 19f, 7.5f)
            Glyph.Calendar -> {
                drawRoundRect(color, p(3.5f, 5f), Size(17 * k, 15 * k), CornerRadius(3 * k), style = style)
                line(3.5f, 10f, 20.5f, 10f); line(8f, 3f, 8f, 7f); line(16f, 3f, 16f, 7f)
            }
            Glyph.Clock -> { drawCircle(color, 8.5f * k, p(12f, 12f), style = style); line(12f, 7.5f, 12f, 12f, 15f, 14f) }
            Glyph.Timer -> { drawCircle(color, 8 * k, p(12f, 13f), style = style); line(12f, 9f, 12f, 13f, 14.5f, 15.5f); line(9.5f, 2.5f, 14.5f, 2.5f) }
            Glyph.Pin -> { line(12f, 16f, 12f, 21f); line(8f, 3f, 16f, 3f, 15f, 9f, 18f, 12f, 6f, 12f, 9f, 9f, 8f, 3f) }
            Glyph.Note -> { drawRoundRect(color, p(5f, 3f), Size(14 * k, 18 * k), CornerRadius(2 * k), style = style); line(9f, 8f, 15f, 8f); line(9f, 12f, 15f, 12f); line(9f, 16f, 13f, 16f) }
            Glyph.Swap -> { line(7f, 4f, 3f, 8f, 7f, 12f); line(3f, 8f, 17f, 8f); line(17f, 20f, 21f, 16f, 17f, 12f); line(21f, 16f, 7f, 16f) }
            Glyph.Down -> line(6f, 9f, 12f, 15f, 18f, 9f)
            Glyph.Up -> line(6f, 15f, 12f, 9f, 18f, 15f)
            Glyph.Back -> line(15f, 6f, 9f, 12f, 15f, 18f)
        }
    }

/** D's rows reach [by] past the columns on each side (margin 0 -6px; padding 0 6px), so a tint or a swipe has room round its content. */
internal fun Modifier.bleed(by: Dp) = layout { measurable, constraints ->
    val px = by.roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.minWidth + 2 * px, maxWidth = constraints.maxWidth + 2 * px))
    layout(placeable.width - 2 * px, placeable.height) { placeable.place(-px, 0) }
}

/** A drawn button: a filled shape with its content centred, in the colours D gives it instead of Material's tonal roles. */
@Composable
internal fun Btn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.medium,
    label: String? = null,
    enabled: Boolean = true,
    body: @Composable RowScope.() -> Unit,
) = Row(
    modifier.alpha(if (enabled) 1f else 0.38f).clip(shape).background(container)
        .clickable(enabled = enabled, onClickLabel = label, role = Role.Button, onClick = onClick),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
) { CompositionLocalProvider(LocalContentColor provides content) { body() } }
