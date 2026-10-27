package se.carlsmeister.strive.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIContentSizeCategory
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityMedium
import platform.UIKit.UIContentSizeCategoryDidChangeNotification
import platform.UIKit.UIContentSizeCategoryExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraSmall
import platform.UIKit.UIContentSizeCategoryLarge
import platform.UIKit.UIContentSizeCategoryMedium
import platform.UIKit.UIContentSizeCategorySmall

// Apple's body size per text size category, in points (HIG, Typography).
private val bodyPoints = mapOf(
    UIContentSizeCategoryExtraSmall to 14, UIContentSizeCategorySmall to 15, UIContentSizeCategoryMedium to 16,
    UIContentSizeCategoryLarge to 17, UIContentSizeCategoryExtraLarge to 19, UIContentSizeCategoryExtraExtraLarge to 21,
    UIContentSizeCategoryExtraExtraExtraLarge to 23, UIContentSizeCategoryAccessibilityMedium to 28,
    UIContentSizeCategoryAccessibilityLarge to 33, UIContentSizeCategoryAccessibilityExtraLarge to 40,
    UIContentSizeCategoryAccessibilityExtraExtraLarge to 47, UIContentSizeCategoryAccessibilityExtraExtraExtraLarge to 53,
)

/** The text size setting as a font scale, with Large's 17 points as 1. */
internal fun textScale(category: UIContentSizeCategory?): Float = (bodyPoints[category] ?: 17) / 17f

private fun currentCategory() = UIApplication.sharedApplication.preferredContentSizeCategory

// shortcut: Compose Multiplatform 1.12 ignores iOS's text size setting; 1.13 scales it itself, so delete this then (#30).
@Composable
internal actual fun ProvideTextScale(content: @Composable () -> Unit) {
    var category by remember { mutableStateOf(currentCategory()) }
    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(UIContentSizeCategoryDidChangeNotification, null, NSOperationQueue.mainQueue) {
            category = currentCategory()
        }
        onDispose { center.removeObserver(observer) }
    }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, textScale(category)), content = content)
}
