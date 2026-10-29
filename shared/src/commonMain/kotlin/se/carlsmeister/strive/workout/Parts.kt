package se.carlsmeister.strive.workout

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.delete
import strive.shared.generated.resources.save
import kotlin.math.roundToInt

/** A chevron drawn with two strokes, since the app carries no icon font. */
@Composable
internal fun Chevron(down: Boolean, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, size: Dp = 16.dp) =
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        val (a, b, c) = if (down) Triple(Offset(w * .15f, h * .35f), Offset(w * .5f, h * .7f), Offset(w * .85f, h * .35f))
            else Triple(Offset(w * .65f, h * .15f), Offset(w * .3f, h * .5f), Offset(w * .65f, h * .85f))
        drawLine(color, a, b, stroke, StrokeCap.Round)
        drawLine(color, b, c, stroke, StrokeCap.Round)
    }

/** A 44 pt round target holding a glyph or a drawing, for title bars. */
@Composable
internal fun IconTarget(label: String, onClick: () -> Unit, content: @Composable () -> Unit) =
    Box(
        Modifier.size(StriveTheme.sizes.touch).clip(ShapeFull).clickable(onClickLabel = label, onClick = onClick),
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
                    .background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent, MaterialTheme.shapes.small)
                    .clickable { onSelect(if (on && clearable) null else i) }
                    .padding(vertical = space.s4),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
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

private enum class Swipe { Closed, Open, Gone }

/**
 * A row that swipes left to show Ta bort, deleted by a tap on it or by swiping on past half the row: the iOS trailing swipe
 * action with full swipe, as in Mail (#17 choice 4).
 */
@Composable
internal fun SwipeToDelete(shape: Shape, onDelete: () -> Unit, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val state = remember { AnchoredDraggableState(Swipe.Closed) }
    var width by remember { mutableIntStateOf(0) }
    val open = with(density) { -(StriveTheme.sizes.touch * 2).toPx() }
    LaunchedEffect(width) {
        if (width > 0) state.updateAnchors(DraggableAnchors { Swipe.Closed at 0f; Swipe.Open at open; Swipe.Gone at -width.toFloat() })
    }
    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }.drop(1).first { it == Swipe.Gone }
        onDelete()
    }
    val offset = state.offset.takeUnless { it.isNaN() } ?: 0f
    Box(Modifier.fillMaxWidth().onSizeChanged { width = it.width }.clip(shape)) {
        Box(
            Modifier.matchParentSize().background(MaterialTheme.colorScheme.error).clickable(onClick = onDelete),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                stringResource(Res.string.delete), Modifier.width(StriveTheme.sizes.touch * 2), color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center,
            )
        }
        Box(
            Modifier.offset { IntOffset(offset.roundToInt(), 0) }
                .anchoredDraggable(state, reverseDirection = false, orientation = Orientation.Horizontal,
                    flingBehavior = AnchoredDraggableDefaults.flingBehavior(state, animationSpec = Motion.settle())),
        ) { content() }
    }
}