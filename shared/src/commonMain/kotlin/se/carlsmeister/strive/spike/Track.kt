package se.carlsmeister.strive.spike

import kotlinx.coroutines.flow.Flow
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Fix(val epochMillis: Long, val latitude: Double, val longitude: Double, val accuracyMeters: Double)

/** Location fixes, about one a second, until the collector stops. */
expect fun locationFixes(): Flow<Fix>

/** Writes [text] to the app's documents folder and returns the path. */
expect fun saveFile(name: String, text: String): String

/** Battery level in percent, or null where the platform cannot say. */
expect fun batteryPercent(): Int?

fun List<Fix>.distanceMeters(maxAccuracyMeters: Double = Double.MAX_VALUE): Double =
    filter { it.accuracyMeters <= maxAccuracyMeters }.zipWithNext(::metersBetween).sum()

fun List<Fix>.gapsSeconds(): List<Double> = zipWithNext { a, b -> (b.epochMillis - a.epochMillis) / 1000.0 }

fun List<Fix>.toCsv(header: String): String =
    "# $header\nepoch_ms,latitude,longitude,accuracy_m\n" +
        joinToString("\n") { "${it.epochMillis},${it.latitude},${it.longitude},${it.accuracyMeters}" }

/** Great-circle distance (haversine) on a sphere of the Earth's mean radius. */
fun metersBetween(a: Fix, b: Fix): Double {
    val toRad = kotlin.math.PI / 180
    val dLat = (b.latitude - a.latitude) * toRad
    val dLon = (b.longitude - a.longitude) * toRad
    val h = sin(dLat / 2).let { it * it } +
        cos(a.latitude * toRad) * cos(b.latitude * toRad) * sin(dLon / 2).let { it * it }
    return 2 * 6_371_008.8 * asin(sqrt(h))
}