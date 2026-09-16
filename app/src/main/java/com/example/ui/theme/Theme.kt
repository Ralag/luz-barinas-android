package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GoogleAISkyBlue, // Google / Gemini Light Blue (#A8C7FA)
    onPrimary = Color(0xFF041E49),
    primaryContainer = GoogleAIBlueContainer,
    onPrimaryContainer = GoogleAIOnBlueContainer,
    secondary = GoogleAIAmber, // Google Warm Amber (#FDD663)
    onSecondary = Color(0xFF2E2000),
    secondaryContainer = Color(0xFF382E12),
    onSecondaryContainer = Color(0xFFFFE08B),
    tertiary = GoogleAIGreen, // Google Soft Emerald (#81C995)
    onTertiary = Color(0xFF04210E),
    tertiaryContainer = Color(0xFF132D1C),
    onTertiaryContainer = Color(0xFFA8E6B8),
    background = DarkCanvas, // Deep Google AI Studio Carbon Black (#131314)
    surface = DarkSurface, // Google AI Studio Surface (#1E1F20)
    surfaceVariant = DarkSurfaceVariant, // Google AI Studio Elevated Surface (#282A2C)
    surfaceContainerHigh = DarkSurfaceContainerHigh, // #2E3134
    onBackground = DarkTextPrimary, // Google Crisp Text (#E3E3E3)
    onSurface = DarkTextPrimary, // #E3E3E3
    onSurfaceVariant = DarkTextSecondary, // Google Medium Gray (#C4C7C5)
    outline = DarkBorder, // Google AI Studio Minimalist Divider (#333538)
    outlineVariant = DarkBorderSubtle // #27292B
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandAmber,
    onSecondary = Color.White,
    tertiary = StatusNormalGreen,
    background = LightCanvas,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    secondaryContainer = Color(0xFFFFF8E1),
    onSecondaryContainer = Color(0xFF1F1B00),
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = Color(0xFF002204),
    surfaceContainerHigh = Color(0xFFECEFF1),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outlineVariant = LightBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Default to clean, modern daylight theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

