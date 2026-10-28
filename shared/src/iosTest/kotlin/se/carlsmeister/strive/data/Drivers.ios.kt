package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import co.touchlab.sqliter.DatabaseFileContext

internal actual fun memoryDriver(): SqlDriver = inMemoryDriver(StriveDatabase.Schema)

internal actual fun fileDriver(name: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver =
    NativeSqliteDriver(schema, name)

internal actual fun deleteFileDatabase(name: String) = DatabaseFileContext.deleteDatabase(name)
