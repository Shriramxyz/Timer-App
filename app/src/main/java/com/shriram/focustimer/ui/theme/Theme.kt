package com.shriram.focustimer.ui.theme

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
    primary = TomatoPrimaryDark,
    onPrimary = TomatoOnPrimaryDark,
    primaryContainer = TomatoPrimaryContainerDark,
    onPrimaryContainer = TomatoOnPrimaryContainerDark,
    secondary = TomatoSecondaryDark,
    onSecondary = TomatoOnSecondaryDark,
    secondaryContainer = TomatoSecondaryContainerDark,
    onSecondaryContainer = TomatoOnSecondaryContainerDark,
    tertiary = TomatoTertiaryDark,
    onTertiary = TomatoOnTertiaryDark,
    tertiaryContainer = TomatoTertiaryContainerDark,
    onTertiaryContainer = TomatoOnTertiaryContainerDark,
    background = TomatoBackgroundDark,
    onBackground = TomatoOnBackgroundDark,
    surface = TomatoSurfaceDark,
    onSurface = TomatoOnSurfaceDark,
    surfaceVariant = TomatoSurfaceVariantDark,
    onSurfaceVariant = TomatoOnSurfaceVariantDark,
    outline = TomatoOutlineDark,
    outlineVariant = TomatoOutlineVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = TomatoPrimary,
    onPrimary = TomatoOnPrimary,
    primaryContainer = TomatoPrimaryContainer,
    onPrimaryContainer = TomatoOnPrimaryContainer,
    secondary = TomatoSecondary,
    onSecondary = TomatoOnSecondary,
    secondaryContainer = TomatoSecondaryContainer,
    onSecondaryContainer = TomatoOnSecondaryContainer,
    tertiary = TomatoTertiary,
    onTertiary = TomatoOnTertiary,
    tertiaryContainer = TomatoTertiaryContainer,
    onTertiaryContainer = TomatoOnTertiaryContainer,
    background = TomatoBackground,
    onBackground = TomatoOnBackground,
    surface = TomatoSurface,
    onSurface = TomatoOnSurface,
    surfaceVariant = TomatoSurfaceVariant,
    onSurfaceVariant = TomatoOnSurfaceVariant,
    outline = TomatoOutline,
    outlineVariant = TomatoOutlineVariant,
)

@Composable
fun FocusTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color false by default to ensure the signature Warm Tomato palette is visible
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
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
