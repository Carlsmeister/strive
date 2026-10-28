package se.carlsmeister.strive

import se.carlsmeister.strive.db.StriveDatabase
import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.DATABASE

private val graph by lazy { AppGraph(NativeSqliteDriver(StriveDatabase.Schema, DATABASE)) }

fun MainViewController() = ComposeUIViewController { App(graph) }
