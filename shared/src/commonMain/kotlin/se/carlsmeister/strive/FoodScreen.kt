package se.carlsmeister.strive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme
import se.carlsmeister.strive.ui.ActionButton
import se.carlsmeister.strive.ui.ActionKind
import se.carlsmeister.strive.ui.LargeTitle
import se.carlsmeister.strive.workout.dayHeading
import se.carlsmeister.strive.workout.local
import se.carlsmeister.strive.workout.now
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.food_later_body
import strive.shared.generated.resources.food_later_title
import strive.shared.generated.resources.food_log_later
import strive.shared.generated.resources.tab_food

/** Food until nutrition is built (SH-05): the large title, today's date and one `.empty.card` saying it comes later. */
@Composable
internal fun FoodScreen(padding: PaddingValues) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    // `.body`: 8 dp under the safe area, 16 dp sides, children 16 dp apart.
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(top = padding.calculateTopPadding() + space.s8, bottom = padding.calculateBottomPadding())
            .padding(horizontal = space.s16),
        verticalArrangement = Arrangement.spacedBy(space.s16),
    ) {
        // `.lt-sub`'s -12 px margin on the body's 16 px gap: 4 dp under the title.
        Column(verticalArrangement = Arrangement.spacedBy(space.s4)) {
            LargeTitle(stringResource(Res.string.tab_food))
            // shortcut: read once per composition, so past midnight the date stays yesterday's until Food recomposes; take it from the shell's clock once that ticks without a workout open.
            Text(dayHeading(local(now())), color = colors.onSurfaceVariant,
                style = type.bodyMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
        }
        Column(
            Modifier.fillMaxWidth().background(colors.surface, MaterialTheme.shapes.large).padding(vertical = space.s32, horizontal = space.s16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space.s8),
        ) {
            Icon(StriveIcons.Food, contentDescription = null, Modifier.size(44.dp), tint = colors.onSurfaceVariant)
            Text(stringResource(Res.string.food_later_title), textAlign = TextAlign.Center,
                style = type.titleMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
            // `.empty p`'s max-width: 30ch, which Chrome resolves to 283.4 px in body-medium's 15 px.
            // shortcut: fixed at 15 px's 30ch, so a larger text size wraps onto more lines than 30ch would; measure "0" in the style when that matters.
            Text(stringResource(Res.string.food_later_body), Modifier.widthIn(max = 283.dp), color = colors.onSurfaceVariant, textAlign = TextAlign.Center,
                style = type.bodyMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
            // The food log (EL-17) is a later issue: the one deviation SH-05 allows.
            ActionButton(stringResource(Res.string.food_log_later), onClick = {}, Modifier.fillMaxWidth().padding(top = space.s8), kind = ActionKind.Tinted)
        }
    }
}
