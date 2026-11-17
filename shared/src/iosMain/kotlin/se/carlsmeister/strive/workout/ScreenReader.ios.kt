package se.carlsmeister.strive.workout

import platform.UIKit.UIAccessibilityIsVoiceOverRunning

internal actual fun screenReaderRunning(): Boolean = UIAccessibilityIsVoiceOverRunning()
