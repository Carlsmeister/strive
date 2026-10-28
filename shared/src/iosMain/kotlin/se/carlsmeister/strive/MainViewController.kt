package se.carlsmeister.strive

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import se.carlsmeister.strive.data.AppGraph
import se.carlsmeister.strive.data.DATABASE
import se.carlsmeister.strive.data.StriveDatabase

private val graph by lazy { AppGraph(NativeSqliteDriver(StriveDatabase.Schema, DATABASE)) }

fun MainViewController() = ComposeUIViewController { App(graph) }
