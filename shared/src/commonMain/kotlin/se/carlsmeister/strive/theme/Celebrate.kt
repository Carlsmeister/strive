package se.carlsmeister.strive.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import org.jetbrains.compose.resources.Font
import strive.shared.generated.resources.Res
import strive.shared.generated.resources.unbounded_bold

/** moments.css `.celebrate`: Unbounded 700 at .88 of the host's size, -0.02 em, tabular figures. Inside a value it takes no leading. */
internal fun TextStyle.celebrate(family: FontFamily, insideValue: Boolean = false): TextStyle = copy(
    fontFamily = family,
    fontWeight = FontWeight.Bold,
    fontSize = fontSize * 0.88f,
    letterSpacing = (-0.02).em,
    fontFeatureSettings = "tnum",
    lineHeight = if (insideValue) 1.em else lineHeight,
)

/** The bundled Unbounded Bold, loaded in a composition. */
@Composable
internal fun celebrateFamily(): FontFamily = FontFamily(Font(Res.font.unbounded_bold, FontWeight.Bold))
