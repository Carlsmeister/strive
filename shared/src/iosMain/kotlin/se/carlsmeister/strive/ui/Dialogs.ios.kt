package se.carlsmeister.strive.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

// The window's own scrim is the card's one scrim; the content fills the window, insets and the keyboard's too, and places its panel itself.
internal actual fun dialogWindow(scrim: Color) =
    DialogProperties(usePlatformDefaultWidth = false, usePlatformInsets = false, useSoftwareKeyboardInset = false, scrimColor = scrim)
