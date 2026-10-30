package se.carlsmeister.strive.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.Category
import se.carlsmeister.strive.data.Exercise
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.Mark
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.add_count
import strive.shared.generated.resources.add_exercises
import strive.shared.generated.resources.back
import strive.shared.generated.resources.cancel
import strive.shared.generated.resources.chip_conditioning
import strive.shared.generated.resources.chip_mobility
import strive.shared.generated.resources.done
import strive.shared.generated.resources.equipment
import strive.shared.generated.resources.filter
import strive.shared.generated.resources.in_my_programmes
import strive.shared.generated.resources.library
import strive.shared.generated.resources.no_match
import strive.shared.generated.resources.search

/**
 * The exercise library (#17 L1): one list with a sticky heading per muscle, search, pattern chips and a filter sheet. Given [pick] it
 * is the add-exercise picker (Q19): rows tick in the order tapped, and Lägg till hands them over.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LibraryScreen(padding: PaddingValues, onOpen: (Long) -> Unit, onBack: () -> Unit, pick: ((List<Long>) -> Unit)? = null) {
    var picked by rememberSaveable { mutableStateOf(listOf<Long>()) }
    // Null until the first read, so the saved scroll position is not spent on an empty list.
    val all by LocalAppGraph.current.exercises.all().collectAsState(null)
    var filter by rememberSaveable(stateSaver = LibraryFilterSaver) { mutableStateOf(LibraryFilter()) }
    var sheet by remember { mutableStateOf(false) }
    val sections = remember(all, filter) { all?.let { filter(it, filter) } }
    val list = rememberLazyListState()
    // A new filter starts at the top; a plain scroll would be undone by the list following the old first row's key.
    fun change(f: LibraryFilter) {
        filter = f
        list.requestScrollToItem(0)
    }
    val space = StriveTheme.spacing
    val direction = LocalLayoutDirection.current
    val sides = Modifier.padding(start = padding.calculateStartPadding(direction), end = padding.calculateEndPadding(direction))

    Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()).then(sides)) {
        if (pick == null) {
            TextButton(onBack, Modifier.padding(start = space.s8)) { Text(stringResource(Res.string.back)) }
            Text(stringResource(Res.string.library), Modifier.padding(horizontal = space.s16), style = MaterialTheme.typography.headlineMedium)
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = space.s8), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onBack) { Text(stringResource(Res.string.cancel)) }
                Text(stringResource(Res.string.add_exercises), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                Button({ pick(picked) }, enabled = picked.isNotEmpty()) { Text(stringResource(Res.string.add_count, picked.size)) }
            }
        }
        Row(Modifier.padding(horizontal = space.s16, vertical = space.s8), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space.s8)) {
            OutlinedTextField(filter.query, { change(filter.copy(query = it)) }, Modifier.weight(1f), singleLine = true,
                placeholder = { Text(stringResource(Res.string.search)) })
            // The button fills while a filter is on (#17 R1).
            if (filter.filtering) FilledTonalButton({ sheet = true }) { Text(stringResource(Res.string.filter)) }
            else TextButton({ sheet = true }) { Text(stringResource(Res.string.filter)) }
        }
        Chips(filter.chip) { change(filter.copy(chip = if (filter.chip == it) null else it)) }
        if (sections == null) return@Column
        if (sections.isEmpty()) {
            Text(stringResource(Res.string.no_match), Modifier.padding(space.s16), style = MaterialTheme.typography.bodyLarge)
        }
        LazyColumn(Modifier.fillMaxSize(), list, contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())) {
            for ((muscle, exercises) in sections) {
                stickyHeader(key = muscle) {
                    Text(stringResource(muscle.label),
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = space.s16, vertical = space.s8),
                        style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }
                items(exercises, key = { it.id }) { e ->
                    if (pick == null) ExerciseRow(e, picked = null) { onOpen(e.id) }
                    else ExerciseRow(e, picked = e.id in picked) { picked = if (e.id in picked) picked - e.id else picked + e.id }
                }
            }
        }
    }
    if (sheet) FilterSheet(filter, onChange = ::change, onDone = { sheet = false })
}

@Composable
private fun Chips(selected: Chip?, onTap: (Chip) -> Unit) {
    val chips = Patterns.keys.map { Chip.Pattern(it) } + Chip.Of(Category.Mobility) + Chip.Of(Category.Conditioning)
    LazyRow(contentPadding = PaddingValues(horizontal = StriveTheme.spacing.s16), horizontalArrangement = Arrangement.spacedBy(StriveTheme.spacing.s8)) {
        items(chips) { chip ->
            val label = when (chip) {
                is Chip.Pattern -> stringResource(Patterns.getValue(chip.name))
                is Chip.Of -> stringResource(if (chip.category == Category.Mobility) Res.string.chip_mobility else Res.string.chip_conditioning)
            }
            FilterChip(selected = chip == selected, onClick = { onTap(chip) }, label = { Text(label) })
        }
    }
}

@Composable
private fun ExerciseRow(exercise: Exercise, picked: Boolean?, onClick: () -> Unit) {
    val space = StriveTheme.spacing
    Row(Modifier.fillMaxWidth().heightIn(min = StriveTheme.sizes.touch).clickable(onClick = onClick).padding(horizontal = space.s16, vertical = space.s8),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space.s8)) {
        Column(Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
            val pattern = Patterns[exercise.pattern]?.let { stringResource(it) } ?: exercise.pattern
            val equipment = Equipment[exercise.equipment]?.let { stringResource(it) } ?: exercise.equipment
            Text("$pattern · $equipment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Unmarked exercises have no badge (#17 choice 11).
        exercise.mark?.let {
            Text(stringResource(it.label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.small).padding(horizontal = space.s8))
        }
        // A choice gets a checkmark (#16, Lists and tables).
        if (picked != null) Text(if (picked) "✓" else "", Modifier.width(space.s24), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterSheet(filter: LibraryFilter, onChange: (LibraryFilter) -> Unit, onDone: () -> Unit) {
    val space = StriveTheme.spacing
    ModalBottomSheet(onDismissRequest = onDone) {
        Column(Modifier.padding(horizontal = space.s16).padding(bottom = space.s24), verticalArrangement = Arrangement.spacedBy(space.s12)) {
            Text(stringResource(Res.string.equipment), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
                for ((value, label) in Equipment) {
                    val on = value in filter.equipment
                    FilterChip(on, { onChange(filter.copy(equipment = if (on) filter.equipment - value else filter.equipment + value)) },
                        label = { Text(stringResource(label)) })
                }
            }
            Text(stringResource(Res.string.in_my_programmes), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space.s8)) {
                for (mark in listOf(Mark.Yes, Mark.Sub, Mark.Test, null, Mark.No)) {
                    val on = mark in filter.marks
                    FilterChip(on, { onChange(filter.copy(marks = if (on) filter.marks - mark else filter.marks + mark)) },
                        label = { Text(stringResource(mark.label)) })
                }
            }
            Button(onDone, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.done)) }
        }
    }
}