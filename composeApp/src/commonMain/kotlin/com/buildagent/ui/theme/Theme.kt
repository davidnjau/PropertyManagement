package com.buildagent.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Brand600,
    onPrimary = White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand700,
    background = Gray50,
    onBackground = Gray900,
    surface = White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    error = Danger600,
    outline = Color(0xFFE5E7EB)
)

@Composable
fun BuildAgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
