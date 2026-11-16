package se.carlsmeister.strive.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

// shortcut: Android's dialog window dims with the platform's own amount, not [scrim]; set the window's dimAmount once Android ships.
internal actual fun dialogWindow(scrim: Color) = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
