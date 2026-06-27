package com.rushov.mizu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MizuPink,
    secondary = SoftPink,
    tertiary = DarkPink,
    background = LightPink,
    surface = GlassWhite,
    onPrimary = TextOnPink,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = MizuPink,
    secondary = SoftPink,
    tertiary = DarkPink,
    background = DeepPurple,
    surface = GlassPink,
    onPrimary = TextOnPink,
    onSecondary = TextOnPink,
    onBackground = TextOnPink,
    onSurface = TextOnPink,
)

@Composable
fun MizuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}