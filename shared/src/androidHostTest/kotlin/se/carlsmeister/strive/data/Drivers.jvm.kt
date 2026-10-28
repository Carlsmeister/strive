package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties

internal actual fun memoryDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { StriveDatabase.Schema.create(it) }

private fun file(name: String) = File(System.getProperty("java.io.tmpdir"), name)

internal actual fun fileDriver(name: String, schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver =
    JdbcSqliteDriver("jdbc:sqlite:${file(name).path}", Properties(), schema)

internal actual fun deleteFileDatabase(name: String) {
    file(name).delete()
}
