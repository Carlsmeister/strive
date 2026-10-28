package se.carlsmeister.strive.theme

import androidx.compose.runtime.Composable

// Android scales sp by its font size setting already.
@Composable
internal actual fun ProvideTextScale(content: @Composable () -> Unit) = content()
