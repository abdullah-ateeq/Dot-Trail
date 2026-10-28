package com.example.dottrail.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.dottrail.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color(0xFF04291E),
    primaryContainer = Color(0xFF0F3A2E),
    onPrimaryContainer = BrandPrimaryLight,
    secondary = BrandPrimaryLight,
    onSecondary = Color(0xFF04291E),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = BrandError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6FAF3),
    onPrimaryContainer = Color(0xFF04291E),
    secondary = BrandPrimaryDark,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = BrandError,
    onError = Color.White
)

@Composable
fun DotTrailTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
