/*
 * File: Theme.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealMint,
    secondary = EmeraldGlow,
    tertiary = SuccessGreen,
    background = DeepDarkBg,
    surface = DeepDarkSurface,
    onPrimary = DeepDarkBg,
    onSecondary = DeepDarkBg,
    onTertiary = DeepDarkBg,
    onBackground = LightText,
    onSurface = LightText,
    error = ErrorRed,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = LightText
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGlow,
    secondary = TealMint,
    tertiary = SuccessGreen,
    error = ErrorRed,
    errorContainer = ErrorRed.copy(alpha = 0.1f)
)

@Composable
fun SmartFactoryTheme(
    // Force Dark Theme as requested
    darkTheme: Boolean = true,
    // We disable dynamic color so our custom Garment Factory colors always show
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
        content = content
    )
}