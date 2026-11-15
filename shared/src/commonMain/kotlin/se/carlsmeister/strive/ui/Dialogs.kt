package se.carlsmeister.strive.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.LocalGlassSource
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.theme.glass
import se.carlsmeister.strive.theme.settle
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.save

/** A dialog window over the card's one scrim of black at [scrim]: the platform's own scrim, so nothing draws a second. */
internal expect fun dialogWindow(scrim: Color): DialogProperties

/**
 * The window every system dialog opens in (P6): Compose's Dialog, for modality, focus and back, filling the screen so its content
 * places its own panel. A tap on the scrim is [onDismiss]. The panels' glass blurs the screen's haze source ([LocalGlassSource],
 * which reaches into the window), provided that source paints its own background.
 */
@Composable
private fun DialogWindow(onDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) =
    Dialog(onDismiss, dialogWindow(Color.Black.copy(alpha = StriveTheme.states.scrimOpacity))) {
        val dismiss by rememberUpdatedState(onDismiss)
        Box(Modifier.fillMaxSize()) {
            // Under the panel, as its sibling, so a tap on the panel never reaches it.
            Spacer(Modifier.matchParentSize().pointerInput(Unit) { detectTapGestures { dismiss() } })
            content()
        }
    }

/** A sheet's entrance: it rises from below on `Motion.settle`, or fades in under Reduce Motion. */
@Composable
private fun Modifier.rise(): Modifier {
    val reduceMotion = StriveTheme.reduceMotion
    val shown = remember { Animatable(0f) }
    LaunchedEffect(Unit) { shown.animateTo(1f, if (reduceMotion) Motion.fade() else Motion.settle()) }
    return graphicsLayer { if (reduceMotion) alpha = shown.value else translationY = (1f - shown.value) * size.height }
}

/** `.alert` in `.center`: the scrim, the alert's panel centred 24 dp clear of the screen's edges, and content of the caller's own. */
@Composable
internal fun AlertBox(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) =
    DialogWindow(onDismiss) { AlertFrame(Modifier.align(Alignment.Center).padding(StriveTheme.spacing.s24), content) }

/** `.alert.glass.thick`: 290 dp of thick glass, 28 dp corners, 20 dp padding, its parts 8 dp apart and centred. */
@Composable
private fun AlertFrame(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier.testTag("glass-alert").width(290.dp)
        .glass(LocalGlassSource.current, StriveTheme.glass, thick = true, shape = MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s20),
    verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
    horizontalAlignment = Alignment.CenterHorizontally,
    content = content,
)

/** An alert: only for an uncommon action that cannot be undone (#16). */
@Composable
internal fun Alert(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = true) =
    DialogWindow(onDismiss) { AlertPanel(title, body, confirm, onConfirm, onDismiss, destructive, Modifier.align(Alignment.Center).padding(StriveTheme.spacing.s24)) }

/** [Alert]'s panel without its window: the title, one line of body, and Cancel beside the confirming button. */
@Composable
internal fun AlertPanel(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = true,
                        modifier: Modifier = Modifier) = AlertFrame(modifier) {
    AlertText(title, MaterialTheme.typography.titleMedium)
    AlertText(body, MaterialTheme.typography.bodyMedium)
    AlertActions(confirm, if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, onConfirm, onDismiss)
}

/** A text to write in the alert's panel, for a name or a note; Save keeps it. */
@Composable
internal fun TextPrompt(title: String, initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit, singleLine: Boolean = true) {
    var text by remember { mutableStateOf(initial) }
    AlertBox(onDismiss) {
        AlertText(title, MaterialTheme.typography.titleMedium)
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), singleLine = singleLine, minLines = if (singleLine) 1 else 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
        AlertActions(stringResource(Res.string.save), MaterialTheme.colorScheme.primary, { onSave(text.trim()) }, onDismiss)
    }
}

@Composable
private fun AlertText(text: String, style: TextStyle) = Text(text, Modifier.fillMaxWidth(), style = style.css(), textAlign = TextAlign.Center)

/** `.alert .acts`: Cancel and [confirm] side by side, equal, 8 dp apart and 8 dp further down. */
@Composable
private fun AlertActions(confirm: String, color: Color, onConfirm: () -> Unit, onCancel: () -> Unit) =
    Row(Modifier.fillMaxWidth().padding(top = StriveTheme.spacing.s8), horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        AlertButton(stringResource(Res.string.cancel), MaterialTheme.colorScheme.primary, onCancel)
        AlertButton(confirm, color, onConfirm)
    }

/** `.btn` in `.acts`: at least 44 dp, 16 dp corners, surface-container-high, title-small. */
@Composable
private fun RowScope.AlertButton(label: String, color: Color, onClick: () -> Unit) = Box(
    Modifier.weight(1f).pressable(true, onClick).background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
        .heightIn(min = StriveTheme.sizes.touch).padding(horizontal = StriveTheme.spacing.s16),
    contentAlignment = Alignment.Center,
) { Text(label, color = color, style = MaterialTheme.typography.titleSmall.css(), textAlign = TextAlign.Center) }

internal class SheetAction(val label: String, val destructive: Boolean = false, val onClick: () -> Unit)

/** The iOS action sheet: choices after an intentional action, a header, and a separate cancel at the bottom (#16). */
@Composable
internal fun ActionSheet(header: String, actions: List<SheetAction>, cancel: String, onDismiss: () -> Unit) = DialogWindow(onDismiss) {
    ActionSheetPanel(header, actions, cancel, onDismiss,
        Modifier.align(Alignment.BottomCenter).rise().navigationBarsPadding().padding(StriveTheme.spacing.s8))
}

/** `.asheet` without its window: the header and the actions in one block, the cancel row in a second, 8 dp apart. */
@Composable
internal fun ActionSheetPanel(header: String, actions: List<SheetAction>, cancel: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) =
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography
        SheetBlock {
            SheetRow(header, colors.onSurfaceVariant, type.bodySmall, StriveTheme.sizes.touch)
            for (a in actions) SheetRow(a.label, if (a.destructive) colors.error else colors.primary, type.bodyLarge, 56.dp, line = true, onClick = a.onClick)
        }
        SheetBlock { SheetRow(cancel, colors.primary, type.labelLarge, 56.dp, onClick = onDismiss) }
    }

/** `.asheet .list`: 16 dp corners, thick and blurred, with no edge or shadow. */
@Composable
private fun SheetBlock(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).glass(LocalGlassSource.current, StriveTheme.glass, thick = true), content = content)

/**
 * `.asheet .row`: centred text, padding 12/16, at least [minHeight]. [line] is `.row + .row::after`, a hairline from 16 dp to the
 * end, floored to whole device pixels as Chrome draws it. It presses as a list row does.
 */
@Composable
private fun SheetRow(label: String, color: Color, style: TextStyle, minHeight: Dp, line: Boolean = false, onClick: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val hairline = with(LocalDensity.current) { StriveTheme.borders.hairline.toPx().toInt().coerceAtLeast(1).toFloat() }
    Box(
        Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(source, indication = null, role = Role.Button, onClick = onClick) else Modifier)
            .background(if (pressed) colors.surfaceContainerHighest else Color.Transparent)
            .drawBehind { if (line) drawRect(colors.outlineVariant, Offset(16.dp.toPx(), 0f), Size(size.width - 16.dp.toPx(), hairline)) }
            .heightIn(min = minHeight).padding(horizontal = StriveTheme.spacing.s16, vertical = StriveTheme.spacing.s12),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = color, style = style.css(), textAlign = TextAlign.Center) }
}

/**
 * `.sheet.glass.thick` with its `.sheet-head`: at the bottom, full width, 28 dp top corners, padding 8 / 16 / 16 plus the navigation
 * bar, its parts 12 dp apart: the grabber, the head ([cancel] at the start, [title] on the sheet's centre, [done] in bold at the
 * end), then [content]. It rises as the action sheet does; a tap on the scrim, or back, is [onDismiss].
 */
@Composable
internal fun HeaderSheet(
    title: String,
    onDismiss: () -> Unit,
    cancel: String? = null,
    onCancel: () -> Unit = onDismiss,
    done: String? = null,
    onDone: () -> Unit = onDismiss,
    content: @Composable ColumnScope.() -> Unit,
) = DialogWindow(onDismiss) {
    val space = StriveTheme.spacing
    Column(
        Modifier.align(Alignment.BottomCenter).rise().fillMaxWidth().glass(LocalGlassSource.current, StriveTheme.glass, thick = true, shape = SheetShape)
            .padding(start = space.s16, top = space.s8, end = space.s16).navigationBarsPadding().padding(bottom = space.s16),
        verticalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(36.dp, 5.dp).background(MaterialTheme.colorScheme.outline, ShapeFull))
        // `1fr auto 1fr`: the title takes what it needs and the sides share the rest, so it sits on the sheet's centre.
        Row(Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { cancel?.let { NavText(it, onCancel) } }
            Text(title, style = MaterialTheme.typography.titleMedium.css(), textAlign = TextAlign.Center)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { done?.let { NavText(it, onDone, bold = true) } }
        }
        content()
    }
}

/** `.sheet`'s corners: extra-large at the top only. */
private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
