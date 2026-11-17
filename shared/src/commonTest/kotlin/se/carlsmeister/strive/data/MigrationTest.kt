package se.carlsmeister.strive.data

import se.carlsmeister.strive.db.StriveDatabase
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
import kotlin.test.assertNull

/** The app's schema plus a test-only next version that adds a column, the way a real `.sqm` would. */
private object SchemaNext : SqlSchema<QueryResult.Value<Unit>> {
    override val version = StriveDatabase.Schema.version + 1
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
    fun dataSurvivesOpeningWithTheNextVersion() = runTest {
        val current = fileDriver(name, StriveDatabase.Schema)
        SqlSettings(StriveDatabase(current)).put("unit", "kg")
        current.close()

        // Opening with a newer schema is what runs the migration, on the phone as here.
        val next = fileDriver(name, SchemaNext)
        try {
            assertEquals("kg", SqlSettings(StriveDatabase(next)).get("unit").first())
            assertEquals(SchemaNext.version, next.executeQuery(null, "PRAGMA user_version", { it.next(); QueryResult.Value(it.getLong(0)) }, 0).value)
        } finally {
            next.close()
        }
    }

    @Test
    fun migrationLeavesUnitNull() = runTest {
        // A real version-5 file: today's schema without the unit column, so reopening it runs 5.sqm. Created with today's
        // schema alone, no migration would run.
        val old = fileDriver(name, StriveDatabase.Schema)
        StriveDatabase(old).exerciseQueries.insertSeed("Bench Press BB", "", "chest", "", "Horisontell press", "Skivstång", "weight_reps",
            false, 2.5, 20.0, "strength", "", "", "", "")
        old.execute(null, "ALTER TABLE exercise DROP COLUMN unit", 0)
        old.execute(null, "PRAGMA user_version = 5", 0)
        old.close()

        val migrated = fileDriver(name, StriveDatabase.Schema)
        try {
            val bench = SqlExercises(StriveDatabase(migrated)).all().first().single()
            assertNull(bench.unit)
            assertEquals(2.5 to 20.0, bench.step to bench.bar)
        } finally {
            migrated.close()
        }
    }
}
