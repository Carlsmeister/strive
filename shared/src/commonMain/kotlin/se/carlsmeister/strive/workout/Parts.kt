package se.carlsmeister.strive.workout

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.settle
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.delete
import strive.shared.generated.resources.save
import kotlin.math.roundToInt

/** A chevron drawn with two strokes, since the app carries no icon font: down, or right for something that opens. */
@Composable
internal fun Chevron(down: Boolean, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, size: Dp = 16.dp) =
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        val (a, b, c) = if (down) Triple(Offset(w * .15f, h * .35f), Offset(w * .5f, h * .7f), Offset(w * .85f, h * .35f))
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

/** Segments in a grey track, the iOS segmented control; tapping the chosen one again clears it when [clearable]. */
@Composable
internal fun Segments(options: List<String>, selected: Int?, onSelect: (Int?) -> Unit, modifier: Modifier = Modifier, clearable: Boolean = true) {
    val space = StriveTheme.spacing
    Row(
        modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.shapes.medium).padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).heightIn(min = StriveTheme.sizes.touch - 4.dp)
                    // Teal, what the tokens keep for chosen: the catalogue's surface is darker than the track in dark mode.
                    .background(if (on) MaterialTheme.colorScheme.primary else Color.Transparent, MaterialTheme.shapes.small)
                    .clickable { onSelect(if (on && clearable) null else i) }
                    // A tab that says whether it is chosen, as VoiceOver reads iOS's segmented control.
                    .semantics { role = Role.Tab; this.selected = on }
                    .padding(vertical = space.s4),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1,
                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** An alert: only for an uncommon action that cannot be undone (#16). */
@Composable
internal fun Alert(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = true) =
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s20),
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
        ) {
            Text(title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(body, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Row(Modifier.padding(top = StriveTheme.spacing.s8)) {
                TextButton(onDismiss, Modifier.weight(1f)) { Text(stringResource(Res.string.cancel)) }
                TextButton(onConfirm, Modifier.weight(1f)) {
                    Text(confirm, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

/** A text to write in an alert-sized box, for a name or a note; Spara keeps it. */
@Composable
internal fun TextPrompt(title: String, initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit, singleLine: Boolean = true) {
    var text by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s20),
            verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s12),
        ) {
            Text(title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), singleLine = singleLine, minLines = if (singleLine) 1 else 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
            Row {
                TextButton(onDismiss, Modifier.weight(1f)) { Text(stringResource(Res.string.cancel)) }
                TextButton({ onSave(text.trim()) }, Modifier.weight(1f)) { Text(stringResource(Res.string.save)) }
            }
        }
    }
}

internal class SheetAction(val label: String, val destructive: Boolean = false, val onClick: () -> Unit)

/** The iOS action sheet: choices after an intentional action, a header, and a separate cancel at the bottom (#16). */
@Composable
internal fun ActionSheet(header: String, actions: List<SheetAction>, cancel: String, onDismiss: () -> Unit) {
    val reduceMotion = StriveTheme.reduceMotion
    val rise = remember { Animatable(if (reduceMotion) 0f else 1f) }
    LaunchedEffect(Unit) { rise.animateTo(0f, Motion.settle()) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)) {
            var height by remember { mutableIntStateOf(0) }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(StriveTheme.spacing.s8)
                    .onSizeChanged { height = it.height }.graphicsLayer { translationY = rise.value * height },
                verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
            ) {
                val block = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
                Column(block) {
                    Text(header, Modifier.fillMaxWidth().padding(StriveTheme.spacing.s12), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    for (a in actions) {
                        HorizontalDivider(thickness = StriveTheme.borders.hairline, color = MaterialTheme.colorScheme.outlineVariant)
                        SheetRow(a.label, if (a.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, a.onClick)
                    }
                }
                Column(block) { SheetRow(cancel, MaterialTheme.colorScheme.primary, onDismiss, bold = true) }
            }
        }
    }
}

@Composable
private fun SheetRow(label: String, color: Color, onClick: () -> Unit, bold: Boolean = false) = Text(
    label,
    Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = StriveTheme.sizes.touch + 12.dp).padding(StriveTheme.spacing.s16),
    color = color, textAlign = TextAlign.Center,
    style = if (bold) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge,
)

/**
 * Where a row let go of settles, as Mail's rows do: past half its width it is deleted, a flick left shows Ta bort and a flick right
 * hides it, and otherwise it goes to the nearer of shut and open. Speed alone never deletes, so a quick short swipe is safe.
 */
internal fun swipeTarget(offset: Float, velocity: Float, width: Float, open: Float, flick: Float): Float = when {
    offset < -width / 2 -> -width
    velocity < -flick -> open
    velocity > flick -> 0f
    offset < open / 2 -> open
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
    Box(Modifier.fillMaxWidth().onSizeChanged { width = it.width }.clip(shape)) {
        // Only while the row moves: at rest the red would show through the antialiased corners as a pink rim.
        if (offset < 0f) Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.error), contentAlignment = Alignment.CenterEnd) {
            // Only the part the row uncovers is the button, so a tap or VoiceOver finds it where it shows.
            Box(Modifier.width(StriveTheme.sizes.touch * 2).fillMaxHeight().clickable(onClick = onDelete), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.onError, style = MaterialTheme.typography.labelLarge)
            }
        }
        Box(
            Modifier.offset { IntOffset(offset.roundToInt(), 0) }
                .draggable(drag, Orientation.Horizontal, onDragStopped = { velocity ->
                    val target = swipeTarget(offset, velocity, width.toFloat(), open, flick)
                    // The finger's speed carries into the spring, so letting go has no seam (apple-motion).
                    animate(offset, target, velocity, Motion.settle()) { value, _ -> offset = value }
                    if (target == -width.toFloat()) onDelete()
                }),
        ) { row.content() }
    }
}
