package se.carlsmeister.strive.theme

import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityMedium
import platform.UIKit.UIContentSizeCategoryExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraSmall
import platform.UIKit.UIContentSizeCategoryLarge
import platform.UIKit.UIContentSizeCategoryMedium
import platform.UIKit.UIContentSizeCategorySmall
import platform.UIKit.UIContentSizeCategoryUnspecified
import kotlin.test.Test
import kotlin.test.assertEquals

class TextScaleTest {
    // Apple's body size per category, in points; Large's 17 is the default and scale 1.
    private val bodyPoints = listOf(
        UIContentSizeCategoryExtraSmall to 14, UIContentSizeCategorySmall to 15, UIContentSizeCategoryMedium to 16,
        UIContentSizeCategoryLarge to 17, UIContentSizeCategoryExtraLarge to 19, UIContentSizeCategoryExtraExtraLarge to 21,
        UIContentSizeCategoryExtraExtraExtraLarge to 23, UIContentSizeCategoryAccessibilityMedium to 28,
        UIContentSizeCategoryAccessibilityLarge to 33, UIContentSizeCategoryAccessibilityExtraLarge to 40,
        UIContentSizeCategoryAccessibilityExtraExtraLarge to 47, UIContentSizeCategoryAccessibilityExtraExtraExtraLarge to 53,
    )

    @Test
    fun everyCategoryScalesAsAppleSizesBodyText() = bodyPoints.forEach { (category, points) ->
        assertEquals(points / 17f, textScale(category), "$category")
    }

    @Test
    fun theLargestAccessibilitySizeIsMoreThanTwiceTheDefault() =
        assertEquals(true, textScale(UIContentSizeCategoryAccessibilityExtraExtraExtraLarge) > 2f)

    @Test
    fun anUnknownCategoryIsTheDefault() = assertEquals(1f, textScale(UIContentSizeCategoryUnspecified))
}
