package se.carlsmeister.strive.theme

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Android 14's contrast runs from -1 to 1; medium and high both pick Strive's one high-contrast scheme (#30). */
internal fun highContrast(contrast: Float) = contrast > 0f

@Composable
internal actual fun accessibility(): Accessibility {
    val context = LocalContext.current
    // shortcut: read when the screen is composed, not when the setting changes; observe it with a ContentObserver once a component needs it live.
    val reduceMotion = remember(context) { Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
    // Android has no Reduce Transparency.
    return Accessibility(highContrast = Build.VERSION.SDK_INT >= 34 && contrast(context), reduceMotion = reduceMotion)
}

@RequiresApi(34)
@Composable
private fun contrast(context: Context): Boolean {
    val modes = context.getSystemService(UiModeManager::class.java)
    var contrast by remember(modes) { mutableFloatStateOf(modes.contrast) }
    DisposableEffect(modes) {
        val listener = UiModeManager.ContrastChangeListener { contrast = it }
        modes.addContrastChangeListener(context.mainExecutor, listener)
        onDispose { modes.removeContrastChangeListener(listener) }
    }
    return highContrast(contrast)
}
