package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TradingPrimary,
    onPrimary = TradingOnPrimary,
    primaryContainer = TradingPrimaryContainer,
    secondary = TradingWinGreen,
    tertiary = TradingLossRed,
    background = TradingBackground,
    surface = TradingSurface,
    surfaceVariant = TradingSurfaceVariant,
    onBackground = TradingTextPrimary,
    onSurface = TradingTextPrimary,
    onSurfaceVariant = TradingTextSecondary,
    outline = TradingCardBorder
)

@Composable
fun JournnexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
