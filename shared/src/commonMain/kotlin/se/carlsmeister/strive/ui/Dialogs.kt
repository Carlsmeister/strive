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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
 *
 * The content ends above the keyboard by its own `imePadding`, not the window's: that consumes the keyboard's inset, so a sheet's
 * navigation-bar padding under it shrinks to nothing instead of adding the home indicator's height above the keyboard.
 */
@Composable
private fun DialogWindow(onDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) =
    Dialog(onDismiss, dialogWindow(Color.Black.copy(alpha = StriveTheme.states.scrimOpacity))) {
        val dismiss by rememberUpdatedState(onDismiss)
        Box(Modifier.fillMaxSize().imePadding()) {
            // Under the panel, as its sibling: Compose passes a tap on to the next sibling down unless the one above records a hit,
            // so every panel records one (Modifier.panel) and only a tap beside it reaches this.
            Spacer(Modifier.matchParentSize().pointerInput(Unit) { detectTapGestures { dismiss() } })
            content()
        }
    }

/** A panel in a dialog window records the hit of a tap on it, even where nothing in it acts, so the tap never reaches the scrim. */
private fun Modifier.panel() = pointerInput(Unit) {}

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
    modifier.testTag("glass-alert").width(290.dp).panel()
        .glass(LocalGlassSource.current, StriveTheme.glass, thick = true, shape = MaterialTheme.shapes.extraLarge).padding(StriveTheme.spacing.s20),
    verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
    horizontalAlignment = Alignment.CenterHorizontally,
    content = content,
)

/** An alert: only for an uncommon action that cannot be undone (#16). [ghost] leaves both buttons unfilled, as WO-16 draws them. */
@Composable
internal fun Alert(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = true,
                   ghost: Boolean = false) =
    DialogWindow(onDismiss) {
        AlertPanel(title, body, confirm, onConfirm, onDismiss, destructive, Modifier.align(Alignment.Center).padding(StriveTheme.spacing.s24), ghost)
    }

/** [Alert]'s panel without its window: the title, one line of body, and Cancel beside the confirming button. */
@Composable
internal fun AlertPanel(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = true,
                        modifier: Modifier = Modifier, ghost: Boolean = false) = AlertFrame(modifier) {
    AlertText(title, MaterialTheme.typography.titleMedium)
    AlertText(body, MaterialTheme.typography.bodyMedium)
    AlertActions(confirm, if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, onConfirm, onDismiss, ghost)
}

/**
 * A text to write in the alert's panel, for a name or a note; Save keeps it (WO-08-rename, WO-08-note, WO-09-sticky). [body] is the
 * alert's centred line under the title; the field, 8 dp further down, takes the caret at once, holds [placeholder] while empty and,
 * [clearable], ends in a clear glyph while it holds text. [caption] is `.cap` under it, left-aligned. [ghost] draws Cancel and Save
 * unfilled, as WO-08 and WO-09 do; WO-07-custom fills them.
 */
@Composable
internal fun TextPrompt(
    title: String,
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    singleLine: Boolean = true,
    body: String? = null,
    placeholder: String? = null,
    caption: String? = null,
    ghost: Boolean = true,
    clearable: Boolean = singleLine,
) {
    var text by remember { mutableStateOf(initial) }
    AlertBox(onDismiss) {
        val focus = remember { FocusRequester() }
        AlertText(title, MaterialTheme.typography.titleMedium)
        body?.let { AlertText(it, MaterialTheme.typography.bodyMedium) }
        InputField(
            text, { text = it }, Modifier.fillMaxWidth().padding(top = StriveTheme.spacing.s8).focusRequester(focus), placeholder.orEmpty(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences), singleLine = singleLine,
            onClear = if (clearable) ({ text = "" }) else null,
        )
        // `.alert p.cap`: `.alert p`'s body-medium outranks `.cap`'s font; `.cap` keeps its colour.
        caption?.let { Text(it, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium.css()) }
        AlertActions(stringResource(Res.string.save), MaterialTheme.colorScheme.primary, { onSave(text.trim()) }, onDismiss, ghost)
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
}

/** `.alert h2` or `.alert p`: a centred line of the alert's, full width. */
@Composable
internal fun AlertText(text: String, style: TextStyle) = Text(text, Modifier.fillMaxWidth(), style = style.css(), textAlign = TextAlign.Center)

/**
 * `.alert .acts`, 8 dp further down: Cancel and [confirm] side by side, equal and 8 dp apart. When either label would not fit its
 * half on one line, measured, it is `.acts.stack`: both full width, 8 dp apart, [confirm] first and Cancel under it (no board draws
 * the stacked order). [ghost] is `.btn.ghost`: no fill, and never stacked; a label too long for its half wraps in it, both buttons
 * as tall as the taller, as WO-16 draws "Discard workout".
 */
@Composable
private fun AlertActions(confirm: String, color: Color, onConfirm: () -> Unit, onCancel: () -> Unit, ghost: Boolean = false) = Layout(
    {
        AlertButton(stringResource(Res.string.cancel), MaterialTheme.colorScheme.primary, onCancel, ghost = ghost)
        AlertButton(confirm, color, onConfirm, ghost = ghost)
    },
    Modifier.fillMaxWidth().padding(top = StriveTheme.spacing.s8),
) { (cancel, ok), constraints ->
    val gap = StriveTheme.spacing.s8.roundToPx()
    val width = constraints.maxWidth
    val half = (width - gap) / 2
    fun Measurable.at(w: Int) = measure(constraints.copy(minWidth = w, maxWidth = w, minHeight = 0))
    if (ghost || maxOf(cancel.maxIntrinsicWidth(Constraints.Infinity), ok.maxIntrinsicWidth(Constraints.Infinity)) <= half) {
        // `.acts` is a flex row: both stretch to the taller.
        val h = maxOf(cancel.minIntrinsicHeight(half), ok.minIntrinsicHeight(half))
        val (c, o) = cancel.measure(Constraints.fixed(half, h)) to ok.measure(Constraints.fixed(half, h))
        layout(width, h) { c.placeRelative(0, 0); o.placeRelative(width - half, 0) }
    } else {
        val (o, c) = ok.at(width) to cancel.at(width)
        layout(width, o.height + gap + c.height) { o.placeRelative(0, 0); c.placeRelative(0, o.height + gap) }
    }
}

/** `.btn` in `.acts`: at least 44 dp, 16 dp corners, surface-container-high (none when [ghost]), title-small. */
@Composable
internal fun AlertButton(label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier, ghost: Boolean = false) = Box(
    modifier.pressable(true, onClick).background(if (ghost) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
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

/** `.asheet .list`: 16 dp corners, thick and blurred, with no edge or shadow. A tap in the 8 dp between two blocks is the scrim's. */
@Composable
private fun SheetBlock(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxWidth().panel().clip(MaterialTheme.shapes.large).glass(LocalGlassSource.current, StriveTheme.glass, thick = true), content = content)

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
 * end), then [content]. It rises as the action sheet does; a tap on the scrim, or back, is [onDismiss]. [full] is `.sheet.full`: its
 * top 60 dp from the window's and no padding at the foot, not even the navigation bar's: its content scrolls to the window's edge
 * and brings its own foot inside the scroll (EX-05-sheet).
 */
@Composable
internal fun HeaderSheet(
    title: String,
    onDismiss: () -> Unit,
    cancel: String? = null,
    onCancel: () -> Unit = onDismiss,
    done: String? = null,
    onDone: () -> Unit = onDismiss,
    full: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) = DialogWindow(onDismiss) {
    val space = StriveTheme.spacing
    Column(
        Modifier.align(Alignment.BottomCenter).then(if (full) Modifier.padding(top = 60.dp).fillMaxHeight() else Modifier)
            .rise().fillMaxWidth().panel().glass(LocalGlassSource.current, StriveTheme.glass, thick = true, shape = SheetShape)
            .padding(start = space.s16, top = space.s8, end = space.s16)
            .then(if (full) Modifier else Modifier.navigationBarsPadding().padding(bottom = space.s16)),
        verticalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(36.dp, 5.dp).background(MaterialTheme.colorScheme.outline, ShapeFull))
        // `1fr auto 1fr`: Cancel and Done keep their size, however long the title runs; the title sits on the sheet's centre.
        CentredBar(
            0.dp, Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch),
            start = { Box { cancel?.let { NavText(it, onCancel) } } },
            middle = {
                Text(title, style = MaterialTheme.typography.titleMedium.css(), textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            end = { Box { done?.let { NavText(it, onDone, bold = true) } } },
        )
        content()
    }
}

/** `.sheet`'s corners: extra-large at the top only. */
private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
