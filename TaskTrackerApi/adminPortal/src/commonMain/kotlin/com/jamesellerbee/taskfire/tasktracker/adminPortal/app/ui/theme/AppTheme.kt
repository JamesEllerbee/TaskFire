package com.jamesellerbee.taskfire.tasktracker.adminPortal.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val lightPalette = lightColors()

val darkPalette = darkColors()


private var _backgroundVariant: Color = Color.LightGray

val Colors.backgroundVariant: Color
    get() = _backgroundVariant

@Composable
fun AppTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (useDarkTheme) {
            _backgroundVariant = Color.Gray
            darkPalette
        } else {
            _backgroundVariant = Color.LightGray
            lightPalette
        }
    ) {
        content()
    }
}