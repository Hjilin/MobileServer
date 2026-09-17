package com.mobileserver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MiuiOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MiuiOrangeLight,
    onPrimaryContainer = MiuiOrangeDark,
    secondary = MiuiBlue,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = MiuiBackground,
    onBackground = MiuiTextPrimary,
    surface = MiuiSurface,
    onSurface = MiuiTextPrimary,
    surfaceVariant = MiuiCard,
    onSurfaceVariant = MiuiTextSecondary,
    outline = MiuiDivider,
    error = MiuiRed
)

@Composable
fun MobileServerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
