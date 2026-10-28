package se.carlsmeister.strive.data

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Version 1 plus a test-only version 2 that adds a column, the way a real `1.sqm` would. */
private object SchemaV2 : SqlSchema<QueryResult.Value<Unit>> {
    override val version = 2L
    override fun create(driver: SqlDriver) = StriveDatabase.Schema.create(driver)
    override fun migrate(driver: SqlDriver, oldVersion: Long, newVersion: Long, vararg callbacks: AfterVersion): QueryResult.Value<Unit> {
        driver.execute(null, "ALTER TABLE setting ADD COLUMN note TEXT", 0)
        return QueryResult.Unit
    }
}

class MigrationTest {
    private val name = "migration-test.db"

    @BeforeTest
    @AfterTest
    fun clean() = deleteFileDatabase(name)

    @Test
    fun version1DataSurvivesOpeningWithVersion2() = runTest {
        val v1 = fileDriver(name, StriveDatabase.Schema)
        SqlSettings(StriveDatabase(v1)).put("unit", "kg")
        v1.close()

        // Opening with a newer schema is what runs the migration, on the phone as here.
        val v2 = fileDriver(name, SchemaV2)
        try {
            assertEquals("kg", SqlSettings(StriveDatabase(v2)).get("unit").first())
            assertEquals(2L, v2.executeQuery(null, "PRAGMA user_version", { it.next(); QueryResult.Value(it.getLong(0)) }, 0).value)
        } finally {
            v2.close()
        }
    }
}
