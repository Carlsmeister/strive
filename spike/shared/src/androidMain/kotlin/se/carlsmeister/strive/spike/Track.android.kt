package se.carlsmeister.strive.spike

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// shortcut: Android location is out of #11's scope (the risk is iOS); stubs keep the compile check green.
actual fun locationFixes(): Flow<Fix> = emptyFlow()

actual fun saveFile(name: String, text: String): String = ""

actual fun batteryPercent(): Int? = null