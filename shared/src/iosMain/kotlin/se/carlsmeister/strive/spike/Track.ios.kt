@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package se.carlsmeister.strive.spike

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.*
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIDevice
import platform.darwin.NSObject

actual fun locationFixes(): Flow<Fix> = callbackFlow {
    val manager = CLLocationManager()
    val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            didUpdateLocations.filterIsInstance<CLLocation>().forEach { location ->
                location.coordinate.useContents {
                    trySend(
                        Fix(
                            (location.timestamp.timeIntervalSince1970 * 1000).toLong(),
                            latitude,
                            longitude,
                            location.horizontalAccuracy,
                        )
                    )
                }
            }
        }

        // Called once when the delegate is set and again after the permission prompt.
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            if (status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways) {
                manager.startUpdatingLocation()
            }
        }
    }
    manager.desiredAccuracy = kCLLocationAccuracyBest
    // No distance filter: a fix every second even when standing still, so a gap means iOS stopped us.
    manager.distanceFilter = kCLDistanceFilterNone
    manager.activityType = CLActivityTypeFitness
    manager.pausesLocationUpdatesAutomatically = false
    manager.allowsBackgroundLocationUpdates = true
    manager.showsBackgroundLocationIndicator = true
    manager.delegate = delegate
    manager.requestWhenInUseAuthorization()
    awaitClose {
        manager.stopUpdatingLocation()
        // manager.delegate is weak; referencing the delegate here keeps it alive until the flow closes.
        if (manager.delegate === delegate) manager.delegate = null
    }
}

actual fun saveFile(name: String, text: String): String {
    val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
    val path = "$dir/$name"
    NSString.create(string = text).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    return path
}

actual fun batteryPercent(): Int? {
    UIDevice.currentDevice.batteryMonitoringEnabled = true
    val level = UIDevice.currentDevice.batteryLevel
    return if (level < 0) null else (level * 100).toInt()
}