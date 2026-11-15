package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.ShapeLargeIncreased
import se.carlsmeister.strive.theme.StriveTheme

/**
 * `.card`: one thing with its own button, solid, no shadow. An [eyebrow] over the [title], a [meta] line, and at most one filled
 * [action] 12 dp below; [large] is `.card.xl`'s corner.
 */
@Composable
internal fun SurfaceCard(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    meta: String? = null,
    large: Boolean = false,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    Column(
        modifier.background(colors.surface, if (large) ShapeLargeIncreased else MaterialTheme.shapes.large).padding(space.s16),
        verticalArrangement = Arrangement.spacedBy(space.s4),
    ) {
        // `.card .eyebrow`, `.title` and `.meta` set `font:`, which resets `.strive`'s tabular-nums.
        if (eyebrow != null) Text(eyebrow, color = colors.onSurfaceVariant,
            style = type.labelMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
        Text(title, color = colors.onSurface, style = titleStyle.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
        if (meta != null) Text(meta, color = colors.onSurfaceVariant,
            style = type.bodyMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox))
        if (action != null) Box(Modifier.padding(top = space.s12)) { action() }
    }
}

/** A stat tile's number: [key] over [value] with a grey [unit], and a [sub] line. Numbers are never teal. */
internal data class Stat(val key: String, val value: String, val unit: String? = null, val sub: String? = null)

// shortcut: only .stats.four, the style the card draws; add .stats and .three when a screen draws them.
/** `.stats.four`: a tile per number in equal columns, every tile as tall as the tallest. */
@Composable
internal fun StatTiles(stats: List<Stat>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val space = StriveTheme.spacing
    // Each `.stat` text's rule sets `font:`, which resets `.strive`'s tabular-nums: the card's figures are proportional.
    val muted = type.labelMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox)
    Row(modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(space.s8)) {
        stats.forEach { stat ->
            Column(
                Modifier.weight(1f).fillMaxHeight().background(colors.surface, MaterialTheme.shapes.large)
                    .padding(horizontal = space.s12, vertical = space.s8),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(stat.key, color = colors.onSurfaceVariant, style = muted)
                // `.v` never wraps: "64 min" overhangs its tile's padding, as on the card, rather than breaking.
                Row(Modifier.wrapContentWidth(Alignment.Start, unbounded = true)) {
                    // 600 22/28
                    val value = type.headlineSmall.copy(fontWeight = FontWeight(600), fontFeatureSettings = null, lineHeightStyle = CssLineBox)
                    Text(stat.value, Modifier.alignByBaseline(), color = colors.onSurface, style = value, softWrap = false)
                    if (stat.unit != null) Text(
                        stat.unit, Modifier.padding(start = space.s4).alignByBaseline(), color = colors.onSurfaceVariant,
                        style = type.titleMedium.copy(fontFeatureSettings = null, lineHeightStyle = CssLineBox), softWrap = false,
                    )
                }
                if (stat.sub != null) Text(stat.sub, color = colors.onSurfaceVariant, style = muted)
            }
        }
    }
}
