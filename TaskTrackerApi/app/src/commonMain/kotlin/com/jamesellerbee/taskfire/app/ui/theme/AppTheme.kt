package com.jamesellerbee.taskfire.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val lightPalette = lightColors()

val darkPalette = darkColors()

object AdditionalColors {
    var backgroundVariant: Color = Color.LightGray
}

val Colors.backgroundVariant: Color
    get() = AdditionalColors.backgroundVariant

@Composable
fun AppTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (useDarkTheme) {
            AdditionalColors.backgroundVariant = Color.Gray
            darkPalette
        } else {
            AdditionalColors.backgroundVariant = Color.LightGray
            lightPalette
        }
    ) {
        content()
    }
}