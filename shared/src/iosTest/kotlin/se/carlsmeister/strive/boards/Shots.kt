package se.carlsmeister.strive.boards

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.writeToFile

/** A picture of the screen, `[folder]/[name].png`, for a person to look at; the tests never compare them. */
@OptIn(ExperimentalTestApi::class, ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun ComposeUiTest.savePng(folder: String, name: String) {
    waitForIdle()
    val bytes = Image.makeFromBitmap(onAllNodes(isRoot())[0].captureToImage().asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
    NSFileManager.defaultManager.createDirectoryAtPath(folder, true, null, null)
    bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()).writeToFile("$folder/$name.png", true) }
}
