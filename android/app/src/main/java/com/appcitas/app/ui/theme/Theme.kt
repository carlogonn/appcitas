package com.appcitas.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Pink40,
    onPrimary = Gray5,
    primaryContainer = Pink10,
    onPrimaryContainer = Gray90,
    secondary = Purple40,
    onSecondary = Gray5,
    secondaryContainer = Purple10,
    onSecondaryContainer = Gray90,
    tertiary = Teal40,
    onTertiary = Gray5,
    tertiaryContainer = Teal10,
    onTertiaryContainer = Gray90,
    background = Gray5,
    onBackground = Gray90,
    surface = Gray5,
    onSurface = Gray90,
    surfaceVariant = Gray10,
    onSurfaceVariant = Gray70,
    error = ErrorRed,
    onError = Gray5
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink20,
    onPrimary = Gray90,
    primaryContainer = Pink80,
    onPrimaryContainer = Pink10,
    secondary = Purple20,
    onSecondary = Gray90,
    secondaryContainer = Purple80,
    onSecondaryContainer = Purple10,
    tertiary = Teal20,
    onTertiary = Gray90,
    tertiaryContainer = Teal80,
    onTertiaryContainer = Teal10,
    background = Gray90,
    onBackground = Gray5,
    surface = Gray90,
    onSurface = Gray5,
    surfaceVariant = Gray80,
    onSurfaceVariant = Gray30,
    error = ErrorRed,
    onError = Gray90
)

@Composable
fun AppCitasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
