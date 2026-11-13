package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.carlsmeister.strive.theme.CssLineBox
import se.carlsmeister.strive.theme.StriveIcons
import se.carlsmeister.strive.theme.StriveTheme

/** A row's title colour: `.row`, `.row.act` (teal) or `.row.dang` (red). */
internal enum class RowTone { Plain, Action, Danger }

/**
 * `.row`: the title left; a [value] (`.val`) and a [chevron] right; a [leading] glyph in a 30 dp tile (`.row.lead`). [centered] is
 * `.row.center`. A pressed row turns surface-container-highest, with no ripple and no scale (the Lists README).
 */
@Composable
internal fun ListRow(
    text: String,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    value: String? = null,
    chevron: Boolean = false,
    tone: RowTone = RowTone.Plain,
    centered: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val space = StriveTheme.spacing
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    Row(
        modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(source, indication = null, role = Role.Button, onClick = onClick) else Modifier)
            .background(if (pressed) colors.surfaceContainerHighest else Color.Transparent)
            .heightIn(min = StriveTheme.sizes.touch).padding(horizontal = space.s16, vertical = space.s12),
        horizontalArrangement = Arrangement.spacedBy(space.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(Modifier.size(30.dp).background(colors.surfaceContainerHigh, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(leading, contentDescription = null, Modifier.size(18.dp), tint = colors.onSurface)
            }
        }
        val ink = when (tone) {
            RowTone.Plain -> colors.onSurface
            RowTone.Action -> colors.primary
            RowTone.Danger -> colors.error
        }
        val style = MaterialTheme.typography.bodyLarge.copy(lineHeightStyle = CssLineBox)
        Text(text, Modifier.weight(1f), color = ink, style = style, textAlign = if (centered) TextAlign.Center else null)
        if (value != null || chevron) {
            Row(horizontalArrangement = Arrangement.spacedBy(space.s4), verticalAlignment = Alignment.CenterVertically) {
                if (value != null) Text(value, color = colors.onSurfaceVariant, style = style, maxLines = 1, softWrap = false)
                if (chevron) Icon(StriveIcons.ChevR, contentDescription = null, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
            }
        }
    }
}

/**
 * `.list` (rule A): a long list is one block, a 0.5 dp hairline between rows from [hairlineInset] to the end (56 dp under
 * leading tiles). [tiles] is `.list.tiles`, for a short list you start or edit: rows 4 dp apart, round only at the ends.
 */
@Composable
internal fun ListBlock(rows: List<@Composable () -> Unit>, modifier: Modifier = Modifier, tiles: Boolean = false, hairlineInset: Dp = 16.dp) {
    val colors = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    if (tiles) {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s4)) {
            rows.forEachIndexed { i, row ->
                val top = if (i == 0) shapes.large else shapes.extraSmall
                val bottom = if (i == rows.lastIndex) shapes.large else shapes.extraSmall
                val shape = shapes.extraSmall.copy(top.topStart, top.topEnd, bottom.bottomEnd, bottom.bottomStart)
                Box(Modifier.clip(shape).background(colors.surface)) { row() }
            }
        }
    } else {
        val hairline = StriveTheme.borders.hairline
        Column(modifier.clip(shapes.large).background(colors.surface)) {
            rows.forEachIndexed { i, row ->
                // `.row + .row::after`: over the row, so a pressed row keeps its line.
                val line = if (i == 0) Modifier else Modifier.drawWithContent {
                    drawContent()
                    drawRect(colors.outlineVariant, Offset(hairlineInset.toPx(), 0f), Size(size.width - hairlineInset.toPx(), hairline.toPx()))
                }
                Box(line) { row() }
            }
        }
    }
}

/** `.group`: lists under a title (`h4`), 8 dp apart. */
@Composable
internal fun ListGroup(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        Text(
            title, Modifier.padding(horizontal = StriveTheme.spacing.s16).semantics { heading() },
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleSmall.copy(lineHeightStyle = CssLineBox),
        )
        content()
    }
}
