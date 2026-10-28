package se.carlsmeister.strive.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.intl.Locale
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.Exercise
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.back
import strive.shared.generated.resources.bar_weight
import strive.shared.generated.resources.equipment
import strive.shared.generated.resources.in_my_programmes
import strive.shared.generated.resources.instructions
import strive.shared.generated.resources.kg
import strive.shared.generated.resources.muscles
import strive.shared.generated.resources.no
import strive.shared.generated.resources.no_workouts
import strive.shared.generated.resources.note
import strive.shared.generated.resources.per_side
import strive.shared.generated.resources.settings_type
import strive.shared.generated.resources.tab_about
import strive.shared.generated.resources.tab_chart
import strive.shared.generated.resources.tab_history
import strive.shared.generated.resources.tab_records
import strive.shared.generated.resources.tips
import strive.shared.generated.resources.weight_step
import strive.shared.generated.resources.yes

private val Tabs = listOf(Res.string.tab_about, Res.string.tab_history, Res.string.tab_chart, Res.string.tab_records)

/** Exercise detail (#17 X1): Om · Historik · Diagram · Rekord, Om first. The last three fill with #42 and #46. */
@Composable
internal fun ExerciseScreen(id: Long, padding: PaddingValues, onBack: () -> Unit) {
    val exercise by LocalAppGraph.current.exercises.byId(id).collectAsState(null)
    var tab by remember { mutableIntStateOf(0) }
    val space = StriveTheme.spacing
    Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        TextButton(onBack, Modifier.padding(start = space.s8)) { Text(stringResource(Res.string.back)) }
        val e = exercise ?: return@Column
        Text(e.name, Modifier.padding(horizontal = space.s16), style = MaterialTheme.typography.headlineMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = space.s16, vertical = space.s8)) {
            Tabs.forEachIndexed { i, label ->
                SegmentedButton(tab == i, { tab = i }, SegmentedButtonDefaults.itemShape(i, Tabs.size)) { Text(stringResource(label), maxLines = 1) }
            }
        }
        if (tab == 0) {
            About(e, Modifier.verticalScroll(rememberScrollState()).padding(bottom = padding.calculateBottomPadding()))
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.no_workouts), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun About(e: Exercise, modifier: Modifier) {
    val space = StriveTheme.spacing
    val swedish = Locale.current.language == "sv"
    // Carl's illustrations arrive one by one (decision 31); without one the page simply starts with the text.
    val image by produceState<ImageBitmap?>(null, e.name) {
        value = runCatching { Res.readBytes(imagePath(e.name)).decodeToImageBitmap() }.getOrNull()
    }
    Column(modifier.padding(horizontal = space.s16), verticalArrangement = Arrangement.spacedBy(space.s16)) {
        image?.let { Image(it, contentDescription = e.name, Modifier.fillMaxWidth().aspectRatio(it.width.toFloat() / it.height)) }
        MarkRow(e)
        Section(Res.string.instructions, if (swedish) e.stepsSv else e.stepsEn)
        Section(Res.string.tips, if (swedish) e.tipsSv else e.tipsEn)
        Section(Res.string.muscles, (listOf(e.primary) + e.secondary).map { stringResource(it.label) }.joinToString(", "))
        Section(Res.string.equipment, Equipment[e.equipment]?.let { stringResource(it) } ?: e.equipment)
        Column {
            Setting(Res.string.settings_type, stringResource(e.type.label))
            Setting(Res.string.per_side, stringResource(if (e.perSide) Res.string.yes else Res.string.no))
            e.step?.let { Setting(Res.string.weight_step, stringResource(Res.string.kg, kilos(it, swedish))) }
            e.bar?.let { Setting(Res.string.bar_weight, stringResource(Res.string.kg, kilos(it, swedish))) }
        }
        e.note?.let { Section(Res.string.note, it) }
    }
}

/** "I mina program": a pull-down menu on the row, and a line saying what the choice does (#17 choice 10). */
@Composable
private fun MarkRow(e: Exercise) {
    val exercises = LocalAppGraph.current.exercises
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.in_my_programmes), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Box {
                TextButton({ open = true }) { Text(stringResource(e.mark.label)) }
                DropdownMenu(open, { open = false }) {
                    for (mark in listOf(Mark.Yes, Mark.Sub, Mark.Test, Mark.No, null)) {
                        DropdownMenuItem({ Text(stringResource(mark.label)) }, onClick = {
                            open = false
                            scope.launch { exercises.setMark(e.id, mark) }
                        })
                    }
                }
            }
        }
        Text(stringResource(e.mark.line), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Section(title: StringResource, body: String) = Column {
    Text(stringResource(title), style = MaterialTheme.typography.titleSmall)
    Text(body, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun Setting(title: StringResource, value: String) =
    Row(Modifier.fillMaxWidth().padding(vertical = StriveTheme.spacing.s4)) {
        Text(stringResource(title), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

/** 2.5 reads "2,5" in Swedish; whole kilos lose their decimals. */
internal fun kilos(value: Double, swedish: Boolean): String {
    val text = if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
    return if (swedish) text.replace('.', ',') else text
}