package se.carlsmeister.strive.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIAccessibilityDarkerSystemColorsEnabled
import platform.UIKit.UIAccessibilityDarkerSystemColorsStatusDidChangeNotification
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIAccessibilityIsReduceTransparencyEnabled
import platform.UIKit.UIAccessibilityReduceMotionStatusDidChangeNotification
import platform.UIKit.UIAccessibilityReduceTransparencyStatusDidChangeNotification

private fun read() = Accessibility(
    highContrast = UIAccessibilityDarkerSystemColorsEnabled(),
    reduceTransparency = UIAccessibilityIsReduceTransparencyEnabled(),
    reduceMotion = UIAccessibilityIsReduceMotionEnabled(),
)

@Composable
internal actual fun accessibility(): Accessibility {
    var current by remember { mutableStateOf(read()) }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observers = listOf(
            UIAccessibilityDarkerSystemColorsStatusDidChangeNotification,
            UIAccessibilityReduceTransparencyStatusDidChangeNotification,
            UIAccessibilityReduceMotionStatusDidChangeNotification,
        ).map { center.addObserverForName(it, null, NSOperationQueue.mainQueue) { current = read() } }
        onDispose { observers.forEach(center::removeObserver) }
    }
    return current
}