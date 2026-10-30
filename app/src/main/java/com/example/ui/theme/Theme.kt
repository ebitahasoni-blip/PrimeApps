package com.example.ui.theme

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
    primary = PrimeNavyPrimaryDark,
    onPrimary = PrimeNavyOnPrimaryDark,
    primaryContainer = PrimeNavyPrimaryContainerDark,
    onPrimaryContainer = PrimeNavyOnPrimaryContainerDark,
    secondary = PrimeTealSecondaryDark,
    onSecondary = PrimeTealOnSecondaryDark,
    secondaryContainer = PrimeTealSecondaryContainerDark,
    onSecondaryContainer = PrimeTealOnSecondaryContainerDark,
    tertiary = PrimeGoldTertiaryDark,
    onTertiary = PrimeGoldOnTertiaryDark,
    tertiaryContainer = PrimeGoldTertiaryContainerDark,
    onTertiaryContainer = PrimeGoldOnTertiaryContainerDark,
    background = PrimeBackgroundDark,
    onBackground = PrimeOnBackgroundDark,
    surface = PrimeSurfaceDark,
    onSurface = PrimeOnSurfaceDark,
    surfaceVariant = PrimeSurfaceVariantDark,
    onSurfaceVariant = PrimeOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimeNavyPrimaryLight,
    onPrimary = PrimeNavyOnPrimaryLight,
    primaryContainer = PrimeNavyPrimaryContainerLight,
    onPrimaryContainer = PrimeNavyOnPrimaryContainerLight,
    secondary = PrimeTealSecondaryLight,
    onSecondary = PrimeTealOnSecondaryLight,
    secondaryContainer = PrimeTealSecondaryContainerLight,
    onSecondaryContainer = PrimeTealOnSecondaryContainerLight,
    tertiary = PrimeGoldTertiaryLight,
    onTertiary = PrimeGoldOnTertiaryLight,
    tertiaryContainer = PrimeGoldTertiaryContainerLight,
    onTertiaryContainer = PrimeGoldOnTertiaryContainerLight,
    background = PrimeBackgroundLight,
    onBackground = PrimeOnBackgroundLight,
    surface = PrimeSurfaceLight,
    onSurface = PrimeOnSurfaceLight,
    surfaceVariant = PrimeSurfaceVariantLight,
    onSurfaceVariant = PrimeOnSurfaceVariantLight
)

@Composable
fun PrimeEssayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our distinguished academic theme by default
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
