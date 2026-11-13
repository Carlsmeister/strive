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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.Motion
import se.carlsmeister.strive.theme.ShapeFull
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.finish
import strive.shared.generated.resources.free_rest
import strive.shared.generated.resources.minimise
import strive.shared.generated.resources.workout_options

/**
 * `.nav`, a pushed screen's bar: [back] (a teal chevron and the parent's name) on the left, the title centred on the bar, [actions]
 * on the right, 4 dp apart. [glassed], once content runs under it: thick glass over a hairline.
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
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    val touch = StriveTheme.sizes.touch
    Column(modifier.fillMaxWidth().background(if (glassed) StriveTheme.glass.thick else Color.Transparent)) {
        // `1fr auto 1fr`: the two sides share what the title leaves, so the title sits on the bar's centre.
        Row(
            Modifier.fillMaxWidth().heightIn(min = touch).padding(horizontal = space.s8),
            horizontalArrangement = Arrangement.spacedBy(space.s8), verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(space.s4), verticalAlignment = Alignment.CenterVertically) {
                if (back != null) Row(
                    Modifier.heightIn(min = touch).pressable(true, onBack).padding(end = space.s8),
                    horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(StriveIcons.Back, contentDescription = null, Modifier.size(22.dp), tint = colors.primary)
                    Text(back, color = colors.primary, style = type.bodyLarge.copy(lineHeightStyle = CssLineBox), softWrap = false)
                }
            }
            Text(title, style = type.titleMedium.copy(lineHeightStyle = CssLineBox), textAlign = TextAlign.Center, softWrap = false)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(space.s4, Alignment.End), verticalAlignment = Alignment.CenterVertically,
                content = actions)
        }
        if (glassed) Hairline()
    }
}

/** `.lt`, a root screen's large title (Home, Plan, You, the workout's name) with [actions] at the end. */
@Composable
internal fun LargeTitle(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) =
    Row(
        modifier.padding(top = StriveTheme.spacing.s4),
        horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8), verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge.copy(lineHeightStyle = CssLineBox))
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
        Row(
            Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(start = space.s12, end = space.s12, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(space.s4)) {
                RoundButton(StriveIcons.Down, stringResource(Res.string.minimise), onMinimise, kind = RoundKind.Plain, iconSize = 22.dp)
                RoundButton(StriveIcons.Timer, stringResource(Res.string.free_rest), onRest, iconSize = 20.dp)
            }
            AnimatedVisibility(title != null, enter = fadeIn(Motion.fade()), exit = fadeOut(Motion.fade())) {
                // Keeps showing the name it had while it fades away.
                val shown = remember { mutableStateOf(title to progress) }
                if (title != null) shown.value = title to progress
                val (name, sub) = shown.value
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name.orEmpty(), style = type.titleMedium.copy(lineHeightStyle = CssLineBox), softWrap = false)
                        MorePill(stringResource(Res.string.workout_options), onMenu, small = true)
                    }
                    if (sub != null) Text(sub, color = colors.onSurfaceVariant, style = type.bodyMedium.copy(lineHeightStyle = CssLineBox))
                }
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Box(
                    Modifier.height(40.dp).pressable(true, onFinish).clip(ShapeFull)
                        .background(if (allLogged) colors.primary else colors.surfaceContainerHigh).padding(horizontal = space.s16),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(Res.string.finish), color = if (allLogged) colors.onPrimary else colors.onSurface,
                        style = type.titleSmall.copy(lineHeightStyle = CssLineBox), softWrap = false)
                }
            }
        }
        if (hairline) Hairline()
    }
}

/** A bar's bottom edge: `border-bottom: hairline solid outline-variant`, below the bar as a border-box adds it. */
@Composable
private fun Hairline() =
    Box(Modifier.fillMaxWidth().height(StriveTheme.borders.hairline).background(MaterialTheme.colorScheme.outlineVariant))
