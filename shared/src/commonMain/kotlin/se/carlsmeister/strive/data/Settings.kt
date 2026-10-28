package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/** The app's settings, such as the weight unit. Screens read and write them only through this (#33). */
internal interface Settings {
    /** The value of [key], sent again whenever it changes; null until it is set. */
    fun get(key: String): Flow<String?>

    suspend fun put(key: String, value: String)
}

internal class SqlSettings(database: StriveDatabase, private val io: CoroutineContext = Dispatchers.IO) : Settings {
    private val queries = database.settingQueries

    override fun get(key: String) = queries.get(key).asFlow().mapToOneOrNull(io)

    override suspend fun put(key: String, value: String) {
        withContext(io) { queries.put(key, value) }
    }
}
