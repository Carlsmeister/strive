package se.carlsmeister.strive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A wheel of [values] as WO-08-time draws it: five 40 dp rows, the centre one on a band, the others fading with their distance from
 * it. It opens on [selected]; a fling snaps to a row, a tap scrolls its row to the centre, and the centre row goes to [onSelect].
 * [alignEnd] sets the values against the end, for the left of two wheels. [label] names the column for VoiceOver.
 */
@Composable
internal fun Wheel(values: List<String>, selected: Int, onSelect: (Int) -> Unit, label: String, alignEnd: Boolean, modifier: Modifier = Modifier) {
    val state = rememberLazyListState(selected)
    val scope = rememberCoroutineScope()
    val half = with(LocalDensity.current) { (WheelRow / 2).roundToPx() }
    // Two rows of padding above the first, so the first visible item is the one on the band.
    val centre by remember(half) { derivedStateOf { state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > half) 1 else 0 } }
    LaunchedEffect(centre) { onSelect(centre) }
    val colors = MaterialTheme.colorScheme
    val small = MaterialTheme.shapes.small
    // The two wheels' bands meet in the middle as the board's one band across the sheet.
    val band = if (alignEnd) small.copy(topEnd = ZeroCornerSize, bottomEnd = ZeroCornerSize) else small.copy(topStart = ZeroCornerSize, bottomStart = ZeroCornerSize)
    Box(modifier.height(WheelRow * 5)) {
        Box(Modifier.align(Alignment.Center).fillMaxWidth().height(WheelRow).background(colors.surfaceContainerHighest, band))
        LazyColumn(
            Modifier.fillMaxSize().semantics { contentDescription = label }, state, PaddingValues(vertical = WheelRow * 2),
            flingBehavior = rememberSnapFlingBehavior(state),
        ) {
            itemsIndexed(values) { i, value ->
                val far = abs(i - centre)
                val size = when (far) { 0 -> 22.sp; 1 -> 20.sp; else -> 18.sp }
                Box(
                    Modifier.fillMaxWidth().height(WheelRow).clickable(interactionSource = null, indication = null) {
                        scope.launch { state.animateScrollToItem(i) }
                    }.padding(horizontal = 22.dp),
                    contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart,
                ) {
                    Text(
                        value, Modifier.alpha(when (far) { 0 -> 1f; 1 -> 0.55f; else -> 0.3f }),
                        color = if (far == 0) colors.onSurface else colors.onSurfaceVariant, fontSize = size, lineHeight = size,
                        fontWeight = FontWeight.Normal, style = MaterialTheme.typography.bodyLarge.css(), // `400 22px/1`, proportional figures
                    )
                }
            }
        }
    }
}

private val WheelRow = 40.dp
