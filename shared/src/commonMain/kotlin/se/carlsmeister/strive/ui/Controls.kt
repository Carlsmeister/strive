package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import se.carlsmeister.strive.theme.Scheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveTheme

/** `.chip`: a capsule small button that filters; a chosen one (`.chip.on`) fills teal. It presses as a button does. */
@Composable
internal fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.heightIn(min = 36.dp).pressable(true, onClick).semantics { this.selected = selected }.clip(ShapeFull)
            .background(if (selected) colors.primary else colors.surfaceContainerHigh)
            .padding(horizontal = StriveTheme.spacing.s16),
        contentAlignment = Alignment.Center,
    ) {
        val base = MaterialTheme.typography.bodyMedium
        // `.chip` sets `font:`, which resets `.strive`'s tabular-nums.
        val weight = if (selected) FontWeight(600) else base.fontWeight
        val style = base.copy(fontWeight = weight, fontFeatureSettings = null, lineHeightStyle = CssLineBox)
        Text(text, color = if (selected) colors.onPrimary else colors.onSurface, style = style, maxLines = 1, softWrap = false)
    }
}

/** `.field` (#16 I3): a grey field; focused, it rings teal (`.field.focus`). [leading] is the 20 dp glyph before the text. */
@Composable
internal fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leading: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val ring = StriveTheme.borders.focus
    var focused by remember { mutableStateOf(false) }
    // `.field .in` sets `font:`, which resets `.strive`'s tabular-nums.
    val style = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface, fontFeatureSettings = null, lineHeightStyle = CssLineBox)
    // A field that takes focus puts the caret after its text, as iOS does; the String overload would start it at 0.
    var edit by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    BasicTextField(
        if (edit.text == value) edit else TextFieldValue(value, TextRange(value.length)),
        { edit = it; if (it.text != value) onValueChange(it.text) },
        modifier.onFocusChanged { focused = it.isFocused },
        textStyle = style, singleLine = true, cursorBrush = SolidColor(colors.primary), keyboardOptions = keyboardOptions,
    ) { field ->
        // Border-box, as CSS: the 2 dp ring sits inside the 44 dp and the 12 dp padding starts inside the ring.
        Row(
            Modifier.heightIn(min = StriveTheme.sizes.touch).background(colors.surfaceContainerHighest, shape)
                .border(ring, if (focused) colors.primary else Color.Transparent, shape).padding(ring)
                .padding(horizontal = StriveTheme.spacing.s12),
            horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) Icon(leading, contentDescription = null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
            // `.field .in`: 10 px above and below one line box, pinned to the style's line height.
            // shortcut: an empty BasicTextField measures 1.3 dp taller than a filled one on Skiko (Compose Multiplatform 1.12.1), and
            // the pin hides that; drop it when an upgrade makes the empty and filled fields equally tall without it.
            Box(Modifier.weight(1f).padding(vertical = 10.dp).height(with(LocalDensity.current) { style.lineHeight.toDp() })) {
                if (value.isEmpty()) Text(placeholder, color = colors.onSurfaceVariant, style = style, maxLines = 1)
                field()
            }
        }
    }
}

/**
 * `.switch` on its own: drawn 52 × 32, tapped across at least the 44 dp touch size, which it keeps as its height. [label] is
 * what a screen reader says.
 */
@Composable
internal fun Toggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier.heightIn(min = StriveTheme.sizes.touch)
            .toggleable(checked, interactionSource = null, indication = null, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { SwitchTrack(checked) }
}

/** `.between`: a setting's text, its switch at the end. The whole row toggles, and its text is the switch's label. */
@Composable
internal fun ToggleRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.touchTall()
            .toggleable(checked, interactionSource = null, indication = null, role = Role.Switch, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // `.between`'s text inherits `.strive`'s font untouched, so it keeps the theme's tabular figures.
        val style = MaterialTheme.typography.bodyLarge.copy(lineHeightStyle = CssLineBox)
        Text(text, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = style)
        SwitchTrack(checked)
    }
}

/**
 * Its layout stays its own height, while what follows in the chain takes taps across at least the touch size centred on it: the
 * extra hangs over its neighbours, so nothing drawn moves. What follows is measured that tall, so it centres what it draws.
 */
internal fun Modifier.touchTall() = layout { measurable, constraints ->
    val own = constraints.constrainHeight(measurable.minIntrinsicHeight(constraints.maxWidth))
    val tapHeight = maxOf(own, StriveTheme.sizes.touch.roundToPx())
    val tapArea = measurable.measure(constraints.copy(minHeight = tapHeight, maxHeight = tapHeight))
    layout(tapArea.width, own) { tapArea.place(0, (own - tapHeight) / 2) }
}

/**
 * `.segc`: switches a view. Equal segments in a track, the chosen one on surface-bright as iOS draws it: lifted by a shadow in
 * light, outlined in dark, where surface-bright equals the track, and outlined heavier under Increase Contrast. Each segment is a
 * tab that says whether it is chosen, and takes taps a touch tall while the track keeps its drawn 40 dp.
 */
@Composable
internal fun SegmentedTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val scheme = StriveTheme.scheme
    val dark = scheme == Scheme.Dark || scheme == Scheme.DarkHc
    val highContrast = scheme == Scheme.LightHc || scheme == Scheme.DarkHc
    val gap = 3.dp
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier.background(if (dark) colors.surfaceContainerHigh else colors.surfaceContainerHighest, MaterialTheme.shapes.small)
            .padding(gap).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            // `box-shadow: inset 0 0 0 w` is a border inside the box; the light shadow is `0 1px 3px rgb(0 0 0 / .2)`.
            val chosen = when {
                !on -> Modifier
                highContrast -> Modifier.background(colors.surfaceBright, shape).border(1.5.dp, colors.outline, shape)
                dark -> Modifier.background(colors.surfaceBright, shape).border(1.dp, colors.outlineVariant, shape)
                else -> Modifier.dropShadow(shape, Shadow(3.dp, Color.Black, offset = DpOffset(0.dp, 1.dp), alpha = .2f)).background(colors.surfaceBright, shape)
            }
            Box(
                Modifier.weight(1f).touchTall()
                    .selectable(on, interactionSource = null, indication = null, role = Role.Tab, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                // `.segc > *` sets `font:`, which resets `.strive`'s tabular-nums.
                val style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight(if (on) 600 else 500), fontFeatureSettings = null, lineHeightStyle = CssLineBox,
                )
                Text(
                    label, Modifier.fillMaxWidth().then(chosen).padding(horizontal = 4.dp, vertical = 7.dp),
                    color = colors.onSurface, style = style, textAlign = TextAlign.Center, maxLines = 1, softWrap = false,
                )
            }
        }
    }
}

/**
 * `.seg`: segments in a grey track, the iOS segmented control; tapping the chosen one again clears it when [clearable]. [compact]
 * is D's focus card (`.seg.compact`): a darker track, 34 pt segments.
 */
@Composable
internal fun Segments(options: List<String>, selected: Int?, onSelect: (Int?) -> Unit, modifier: Modifier = Modifier, clearable: Boolean = true,
                      compact: Boolean = false) {
    val space = StriveTheme.spacing
    val gap = if (compact) 3.dp else 2.dp
    // Its own height unless the caller sets a taller one, which the segments then fill (`.seg`'s grid stretches them).
    Row(
        modifier.height(IntrinsicSize.Min).background(
            if (compact) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerHighest,
            if (compact) RoundedCornerShape(12.dp) else MaterialTheme.shapes.medium,
        ).padding(gap),
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).fillMaxHeight().then(if (compact) Modifier.height(34.dp) else Modifier.heightIn(min = StriveTheme.sizes.touch - 4.dp))
                    // Teal, what the tokens keep for chosen: the catalogue's surface is darker than the track in dark mode.
                    .background(if (on) MaterialTheme.colorScheme.primary else Color.Transparent, if (compact) RoundedCornerShape(9.dp) else MaterialTheme.shapes.small)
                    .clickable { onSelect(if (on && clearable) null else i) }
                    // A tab that says whether it is chosen, as VoiceOver reads iOS's segmented control.
                    .semantics { role = Role.Tab; this.selected = on }
                    .padding(vertical = if (compact) 0.dp else space.s4),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1,
                    color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/** `.switch`'s drawing only: 52 × 32, teal on; the knob stays white in every mode. */
@Composable
private fun SwitchTrack(checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    Box(Modifier.size(52.dp, 32.dp).background(if (checked) colors.primary else colors.surfaceContainerHighest, ShapeFull).padding(2.dp)) {
        // `::after`'s box-shadow: 0 2px 6px rgb(0 0 0 / .25).
        Box(
            Modifier.offset(x = if (checked) 20.dp else 0.dp).size(28.dp)
                .dropShadow(CircleShape, Shadow(6.dp, Color.Black, offset = DpOffset(0.dp, 2.dp), alpha = .25f))
                .background(Color.White, CircleShape),
        )
    }
}
