package se.carlsmeister.strive

import se.carlsmeister.strive.db.StriveDatabase
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.DATABASE

private var graph: AppGraph? = null

/** One graph per process, built on the main thread; activities come and go around it. */
fun appGraph(context: Context): AppGraph =
    graph ?: AppGraph(AndroidSqliteDriver(StriveDatabase.Schema, context.applicationContext, DATABASE)).also { graph = it }
