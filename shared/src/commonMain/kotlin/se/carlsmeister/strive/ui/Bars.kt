package se.carlsmeister.strive.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.back
import strive.shared.generated.resources.finish
import strive.shared.generated.resources.free_rest
import strive.shared.generated.resources.minimise
import strive.shared.generated.resources.workout_options

/**
 * `.nav`, a pushed screen's bar: [back] (a teal chevron and the parent's name) on the left, the title centred on the bar, [actions]
 * ([NavText] or plain [RoundButton]s) on the right, 4 dp apart. [glassed], once content runs under it: thick glass over a hairline.
 */
@Composable
internal fun NavBar(
    title: String,
    modifier: Modifier = Modifier,
    back: String? = null,
    onBack: () -> Unit = {},
    glassed: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    val touch = StriveTheme.sizes.touch
    Column(modifier.fillMaxWidth().background(if (glassed) StriveTheme.glass.thick else Color.Transparent)) {
        CentredBar(
            space.s8, Modifier.fillMaxWidth().heightIn(min = touch).padding(horizontal = space.s8),
            start = {
                Row(horizontalArrangement = Arrangement.spacedBy(space.s4), verticalAlignment = Alignment.CenterVertically) {
                    if (back != null) BackLabel(back, onBack)
                }
            },
            middle = {
                // `.nav h1`, `.back`, `.navtext`, `.lt h1`, `.wtb .mid .wn` and `small` set `font:`, which resets `.strive`'s tabular-nums.
                Text(title, style = type.titleMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox), textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            end = {
                Row(horizontalArrangement = Arrangement.spacedBy(space.s4), verticalAlignment = Alignment.CenterVertically, content = actions)
            },
        )
        if (glassed) Hairline()
    }
}

/**
 * `.back`: a teal chevron and the [parent]'s name, the screen it goes back to, ending in an ellipsis where a title leaves it too little
 * room. VoiceOver reads the name and says a tap goes back. Tagged `back`, so a flow finds it apart from a tab of the same name.
 */
@Composable
internal fun BackLabel(parent: String, onBack: () -> Unit, modifier: Modifier = Modifier) =
    Row(
        modifier.testTag("back").heightIn(min = StriveTheme.sizes.touch).pressable(true, onBack, label = stringResource(Res.string.back))
            .padding(end = StriveTheme.spacing.s8),
        horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(StriveIcons.Back, contentDescription = null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
        Text(parent, color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox),
            softWrap = false, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }

/** `.navtext`, a bar's text action: teal, [bold] for the confirming one (`.b`), [dim] when it cannot act yet (`.dim`). */
@Composable
internal fun NavText(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, bold: Boolean = false, dim: Boolean = false) =
    Box(
        modifier.heightIn(min = StriveTheme.sizes.touch).alpha(if (dim) StriveTheme.states.disabledOpacity else 1f).pressable(!dim, onClick)
            .padding(horizontal = StriveTheme.spacing.s8),
        contentAlignment = Alignment.Center,
    ) {
        val style = if (bold) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
        Text(label, color = if (dim) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
            style = style.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox), softWrap = false)
    }

/** `.lt`, a root screen's large title (Home, Plan, You, the workout's name) with [actions] at the end. */
@Composable
internal fun LargeTitle(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) =
    Row(
        modifier.padding(top = StriveTheme.spacing.s4),
        horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8), verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, Modifier.weight(1f),
            style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
        actions()
    }

/**
 * `.wtb`, F1, the workout's one bar: ⌄ and ⏱ on the left, Finish on the right, filled teal once [allLogged]. Scrolled, the caller
 * sets [title] and [progress], which fade into the middle with a small ⋯, and turns the bar [glassed]; [hairline] draws its edge.
 */
@Composable
internal fun WorkoutBar(
    onMinimise: () -> Unit,
    onRest: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    allLogged: Boolean = false,
    glassed: Boolean = false,
    hairline: Boolean = false,
    title: String? = null,
    progress: String? = null,
    onMenu: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    Column(modifier.fillMaxWidth().background(if (glassed) StriveTheme.glass.thick else colors.background)) {
        CentredBar(
            0.dp, Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(start = space.s12, end = space.s12, bottom = 6.dp),
            start = {
                Row(horizontalArrangement = Arrangement.spacedBy(space.s4)) {
                    RoundButton(StriveIcons.Down, stringResource(Res.string.minimise), onMinimise, kind = RoundKind.Plain, iconSize = 22.dp)
                    RoundButton(StriveIcons.Timer, stringResource(Res.string.free_rest), onRest, iconSize = 20.dp)
                }
            },
            middle = {
                AnimatedVisibility(title != null, enter = fadeIn(Motion.fade()), exit = fadeOut(Motion.fade())) {
                    // Keeps showing the name it had while it fades away.
                    val shown = remember { mutableStateOf(title to progress) }
                    if (title != null) shown.value = title to progress
                    val (name, sub) = shown.value
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(name.orEmpty(), Modifier.weight(1f, fill = false),
                                style = type.titleMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            MorePill(stringResource(Res.string.workout_options), onMenu, small = true)
                        }
                        if (sub != null) Text(sub, color = colors.onSurfaceVariant,
                            style = type.bodyMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
                    }
                }
            },
            end = {
                // A 40 dp capsule tapped across the 44 dp touch size; the row is 44 tall already, so nothing drawn moves.
                Box(Modifier.heightIn(min = StriveTheme.sizes.touch).pressable(true, onFinish), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.height(40.dp).clip(ShapeFull)
                            .background(if (allLogged) colors.primary else colors.surfaceContainerHigh).padding(horizontal = space.s16),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(Res.string.finish), color = if (allLogged) colors.onPrimary else colors.onSurface,
                            style = type.titleSmall.copy(lineHeightStyle = CssLineBox), softWrap = false)
                    }
                }
            },
        )
        if (hairline) Hairline()
    }
}

/**
 * `1fr auto 1fr`: [start] and [end] at the bar's edges, [middle] on the bar's centre, [gap] from each side. The middle gets only what is
 * left once the wider side has its content, so a long title ends in an ellipsis and never squeezes a side's buttons.
 */
@Composable
private fun CentredBar(gap: Dp, modifier: Modifier, start: @Composable () -> Unit, middle: @Composable () -> Unit, end: @Composable () -> Unit) =
    Layout(listOf(start, middle, end), modifier) { (s, m, e), c ->
        val loose = c.copy(minWidth = 0, minHeight = 0)
        val left = s.first().measure(loose)
        val right = e.first().measure(loose)
        val room = c.maxWidth - 2 * (maxOf(left.width, right.width) + gap.roundToPx())
        val title = m.firstOrNull()?.measure(loose.copy(maxWidth = room.coerceAtLeast(0))) // none once AnimatedVisibility has faded out
        val height = maxOf(left.height, right.height, title?.height ?: 0).coerceIn(c.minHeight, c.maxHeight)
        val centre = Alignment.CenterVertically
        layout(c.maxWidth, height) {
            left.place(0, centre.align(left.height, height))
            right.place(c.maxWidth - right.width, centre.align(right.height, height))
            title?.place((c.maxWidth - title.width) / 2, centre.align(title.height, height))
        }
    }

/** A bar's bottom edge: `border-bottom: hairline solid outline-variant`, below the bar as a border-box adds it. */
@Composable
private fun Hairline() =
    Box(Modifier.fillMaxWidth().height(StriveTheme.borders.hairline).background(MaterialTheme.colorScheme.outlineVariant))
