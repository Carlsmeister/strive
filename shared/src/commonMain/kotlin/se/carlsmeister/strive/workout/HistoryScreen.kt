package se.carlsmeister.strive.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import se.carlsmeister.strive.data.LocalAppGraph
import se.carlsmeister.strive.data.Workout
import se.carlsmeister.strive.theme.StriveTheme
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.all_activities
import strive.shared.generated.resources.back
import strive.shared.generated.resources.history_sets
import strive.shared.generated.resources.minutes
import strive.shared.generated.resources.no_workouts
import strive.shared.generated.resources.start_a_workout
import strive.shared.generated.resources.week_workouts
import strive.shared.generated.resources.weekdays_short
import kotlin.math.roundToLong

/**
 * History (#17 choice 14, H3): weeks as groups with a summing header, each workout led by its weekday and date. Alla · Styrka ·
 * Löpning joins when runs exist (#5); record counts when records do (#46).
 */
@Composable
internal fun HistoryScreen(padding: PaddingValues, onOpen: (Long) -> Unit, onBack: () -> Unit, onStart: () -> Unit) {
    val sessions = LocalAppGraph.current.sessions
    val history by remember { sessions.history() }.collectAsState(null)
    val space = StriveTheme.spacing
    Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        TextButton(onBack, Modifier.padding(start = space.s8)) { Text(stringResource(Res.string.back)) }
        Text(stringResource(Res.string.all_activities), Modifier.padding(horizontal = space.s16), style = MaterialTheme.typography.headlineMedium)
        val all = history ?: return@Column
        if (all.isEmpty()) {
            // Q47's first use: History says so and offers a workout.
            Column(Modifier.fillMaxSize().padding(space.s24), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space.s16, Alignment.CenterVertically)) {
                Text(stringResource(Res.string.no_workouts), style = MaterialTheme.typography.titleMedium)
                Button(onStart) { Text(stringResource(Res.string.start_a_workout)) }
            }
            return@Column
        }
        val zone = remember { TimeZone.currentSystemDefault() }
        LazyColumn(
            contentPadding = PaddingValues(start = space.s16, end = space.s16, top = space.s16, bottom = padding.calculateBottomPadding() + space.s24),
            verticalArrangement = Arrangement.spacedBy(space.s8),
        ) {
            for (week in weeks(all, zone)) {
                item(key = "w${week.year}-${week.week}") {
                    Text(
                        pluralStringResource(Res.plurals.week_workouts, week.workouts.size, week.week, week.workouts.size),
                        Modifier.padding(top = space.s8, start = space.s16), style = MaterialTheme.typography.titleSmall,
                    )
                }
                item(key = "b${week.year}-${week.week}") {
                    Block {
                        week.workouts.forEachIndexed { i, w ->
                            if (i > 0) Hairline()
                            HistoryRow(w) { onOpen(w.id) }
                        }
                    }
                }
            }
        }
    }
}

/** H3's row: the day in a column, then name and start time, then minutes, sets and volume (Carl wants to watch volume grow). */
@Composable
private fun HistoryRow(w: Workout, onClick: () -> Unit) {
    val space = StriveTheme.spacing
    val swedish = Locale.current.language == "sv"
    val start = local(w.startedAt)
    val s = summary(w, now())
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = StriveTheme.sizes.touch + space.s16)
            .padding(horizontal = space.s16, vertical = space.s8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space.s12),
    ) {
        Column(Modifier.width(StriveTheme.sizes.touch), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringArrayResource(Res.array.weekdays_short)[start.dayOfWeek.isoDayNumber - 1], style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${start.day}", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
        Column(Modifier.weight(1f)) {
            Text(w.name, style = MaterialTheme.typography.bodyLarge)
            Text(hhmm(start), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(space.s4)) {
            val grey = MaterialTheme.colorScheme.onSurfaceVariant
            Text(stringResource(Res.string.minutes, s.minutes.toInt()), style = MaterialTheme.typography.bodySmall, color = grey)
            val sets = stringResource(Res.string.history_sets, s.sets)
            Text(if (s.volume > 0) "$sets · ${grouped(s.volume.roundToLong(), swedish)} kg" else sets, style = MaterialTheme.typography.bodySmall, color = grey)
        }
    }
}