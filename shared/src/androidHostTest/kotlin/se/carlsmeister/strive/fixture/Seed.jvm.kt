package se.carlsmeister.strive.fixture

import java.io.File

internal actual suspend fun exerciseSeed(): String = File("src/commonMain/composeResources/files/exercises.json").readText()
