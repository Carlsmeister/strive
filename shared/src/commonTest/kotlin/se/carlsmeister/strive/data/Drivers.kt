package se.carlsmeister.strive.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/** A fresh in-memory database with schema version 1. */
internal expect fun memoryDriver(): SqlDriver

/** A database file opened the way the app opens it: created, or migrated up to [schema]'s version. */
internal expect fun fileDriver(name: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver

internal expect fun deleteFileDatabase(name: String)
