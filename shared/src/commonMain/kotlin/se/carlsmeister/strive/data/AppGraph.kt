package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import androidx.compose.runtime.staticCompositionLocalOf
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import se.carlsmeister.strive.workout.WorkoutToasts
import se.carlsmeister.strive.workout.holdFor
import se.carlsmeister.strive.workout.screenReaderRunning
import strive.shared.generated.resources.Res

/** The database file's name on both phones. */
internal const val DATABASE = "strive.db"

/** Everything the app hands around, built once when it starts and wired by hand (ADR 0005). */
class AppGraph internal constructor(driver: SqlDriver) {
    private val database = StriveDatabase(driver)
    internal val settings: Settings = SqlSettings(database)
    private val sqlExercises = SqlExercises(database)
    internal val exercises: Exercises = sqlExercises
    internal val sessions: Sessions = SqlSessions(database)

    /** The rest counting down, in memory: it outlives the workout screen when the workout is minimised (Q19). */
    internal val rest = kotlinx.coroutines.flow.MutableStateFlow<Rest?>(null)

    /** Timers that outlive the screen, on Default so they run in real time under UI tests too (P5). */
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** How long a workout toast shows; board shots raise it so a toast never ends (P5). A Removed toast under a screen reader holds until acted on. */
    internal var toastHoldMillis = 4_000L
    internal val toasts = WorkoutToasts(scope, sessions::removeExercise) { holdFor(it, toastHoldMillis, screenReaderRunning()) }

    /** Cancels the graph's timers; tests call it when they are done with a graph. */
    internal fun close() = scope.cancel()

    /** Brings the exercise library up to the seed shipped with this build; cheap when it already is. */
    internal suspend fun prepare() =
        seedExercises(Res.readBytes("files/exercises.json").decodeToString(), EXERCISE_SEED, sqlExercises, settings)
}

/** Raise when files/exercises.json changes, so installed apps take the new seed (#41). */
internal const val EXERCISE_SEED = 2

/** How a screen reaches the graph: `LocalAppGraph.current.settings`. */
internal val LocalAppGraph = staticCompositionLocalOf<AppGraph> { error("App provides the AppGraph") }
