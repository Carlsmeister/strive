package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import androidx.compose.runtime.staticCompositionLocalOf
import app.cash.sqldelight.db.SqlDriver
import strive.shared.generated.resources.Res

/** The database file's name on both phones. */
internal const val DATABASE = "strive.db"

/** Everything the app hands around, built once when it starts and wired by hand (ADR 0005). */
class AppGraph internal constructor(driver: SqlDriver) {
    private val database = StriveDatabase(driver)
    internal val settings: Settings = SqlSettings(database)
    private val sqlExercises = SqlExercises(database)
    internal val exercises: Exercises = sqlExercises

    /** Brings the exercise library up to the seed shipped with this build; cheap when it already is. */
    internal suspend fun prepare() =
        seedExercises(Res.readBytes("files/exercises.json").decodeToString(), EXERCISE_SEED, sqlExercises, settings)
}

/** Raise when files/exercises.json changes, so installed apps take the new seed (#41). */
internal const val EXERCISE_SEED = 2

/** How a screen reaches the graph: `LocalAppGraph.current.settings`. */
internal val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("App provides the AppGraph") }
