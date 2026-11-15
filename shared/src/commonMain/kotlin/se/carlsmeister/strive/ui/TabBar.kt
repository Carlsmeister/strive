package se.carlsmeister.strive.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The bar's gap above the screen's edge (P9): 22 dp over a home indicator, 8 dp above a taller system bar. */
internal fun barBottom(inset: Dp) = if (inset <= 34.dp) 22.dp else inset + 8.dp
