package se.carlsmeister.strive.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun accessibility(): Accessibility {
    val resolver = LocalContext.current.contentResolver
    // shortcut: read when the screen is composed, not when the setting changes; observe it with a ContentObserver once a component needs it live.
    val reduceMotion = remember(resolver) { Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    // Contrast (Android 14+) waits for Carl's call in #30 on medium contrast. Android has no Reduce Transparency.
    return Accessibility(reduceMotion = reduceMotion)
}
