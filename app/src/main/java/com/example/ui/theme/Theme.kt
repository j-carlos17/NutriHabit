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
    primary = Mint80,
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Emerald80,
    secondary = EmeraldGrey80,
    onSecondary = Color(0xFF1F2937),
    tertiary = ProteinColor,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFFD1D5DB)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = EmeraldGrey40,
    onSecondary = Color.White,
    tertiary = ProteinColor,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF4B5563)
)

@Composable
fun NutriHabitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded emerald & macro colors consistent
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
        content = content
    )
}
