package com.buildagent.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = Brand600,
    onPrimary          = White,
    primaryContainer   = Brand100,
    onPrimaryContainer = Brand800,
    secondary          = Cyan500,
    onSecondary        = White,
    secondaryContainer = Cyan100,
    onSecondaryContainer = Color(0xFF164E63),
    background         = Color(0xFFF0EFFE),   // barely-lavender canvas
    onBackground       = Gray900,
    surface            = White,
    onSurface          = Gray900,
    surfaceVariant     = Color(0xFFF5F3FF),   // slightly purple-tinted panels
    onSurfaceVariant   = Gray700,
    error              = Danger600,
    outline            = Gray300,
    outlineVariant     = Color(0xFFE0E7FF)
)

@Composable
fun BuildAgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
