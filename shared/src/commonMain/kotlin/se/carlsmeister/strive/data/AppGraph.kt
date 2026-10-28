package se.carlsmeister.strive.data

import androidx.compose.runtime.staticCompositionLocalOf
import app.cash.sqldelight.db.SqlDriver

/** The database file's name on both phones. */
internal const val DATABASE = "strive.db"

/** Everything the app hands around, built once when it starts and wired by hand (ADR 0005). */
class AppGraph internal constructor(driver: SqlDriver) {
    internal val settings: Settings = SqlSettings(StriveDatabase(driver))
}

/** How a screen reaches the graph: `LocalAppGraph.current.settings`. */
internal val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("App provides the AppGraph") }
