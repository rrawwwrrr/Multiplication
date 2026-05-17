package com.multiply.kids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PurpleLight,
    secondary = Orange,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = OrangeLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = PurpleDark,
    onSurface = PurpleDark,
)

@Composable
fun MultiplyKidsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
