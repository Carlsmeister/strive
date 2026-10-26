package se.carlsmeister.strive.spike

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@Composable
fun App() {
    MaterialTheme {
        val fixes = remember { mutableStateListOf<Fix>() }
        var recording by remember { mutableStateOf(false) }
        var elapsed by remember { mutableStateOf(Duration.ZERO) }
        var startBattery by remember { mutableStateOf<Int?>(null) }
        var savedTo by remember { mutableStateOf<String?>(null) }

        fun save() {
            if (fixes.isEmpty()) return
            savedTo = saveFile(
                "run-${fixes.first().epochMillis}.csv",
                fixes.toList().toCsv("battery_start=$startBattery battery_now=${batteryPercent()} elapsed_s=${elapsed.inWholeSeconds}"),
            )
        }

        if (recording) {
            LaunchedEffect(Unit) {
                val start = TimeSource.Monotonic.markNow()
                launch {
                    while (true) {
                        elapsed = start.elapsedNow()
                        delay(1.seconds)
                    }
                }
                locationFixes().collect { fix ->
                    fixes += fix
                    // shortcut: rewrites the whole file every 60 fixes so a killed app loses at most a minute; fine for one 30-minute run.
                    if (fixes.size % 60 == 0) save()
                }
            }
        }

        val gaps = fixes.toList().gapsSeconds()
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = {
                if (recording) {
                    recording = false
                    save()
                } else {
                    fixes.clear()
                    savedTo = null
                    elapsed = Duration.ZERO
                    startBattery = batteryPercent()
                    recording = true
                }
            }) {
                Text(if (recording) "Stop" else "Start")
            }
            Text("Time ${elapsed.inWholeMinutes} min ${elapsed.inWholeSeconds % 60} s")
            Text("Points ${fixes.size}")
            Text("Distance ${fixes.toList().distanceMeters().toInt()} m (all points)")
            Text("Distance ${fixes.toList().distanceMeters(maxAccuracyMeters = 20.0).toInt()} m (accuracy ≤ 20 m)")
            Text("Longest gap ${(gaps.maxOrNull() ?: 0.0).toInt()} s, gaps over 30 s: ${gaps.count { it > 30 }}")
            Text("Battery ${startBattery ?: "–"} % → ${batteryPercent() ?: "–"} %")
            savedTo?.let { Text("Saved ${it.substringAfterLast('/')}") }
        }
    }
}