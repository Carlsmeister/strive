package se.carlsmeister.strive.workout

import android.content.Context
import android.view.accessibility.AccessibilityManager

/** The application's context, set once by `appGraph`; null in tests that build a graph without one. */
internal var appContext: Context? = null

// Touch exploration is on while TalkBack, or another screen reader, reads the screen.
internal actual fun screenReaderRunning(): Boolean =
    appContext?.getSystemService(AccessibilityManager::class.java)?.isTouchExplorationEnabled == true
