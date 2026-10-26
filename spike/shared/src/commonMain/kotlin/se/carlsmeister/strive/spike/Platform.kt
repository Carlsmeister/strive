package se.carlsmeister.strive.spike

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform