package se.carlsmeister.strive.fixture

import strive.shared.generated.resources.Res

internal actual suspend fun exerciseSeed(): String = Res.readBytes("files/exercises.json").decodeToString()
