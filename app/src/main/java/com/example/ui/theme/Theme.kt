package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberzoneColorScheme = darkColorScheme(
    primary = CyberzoneElectricGreen,
    onPrimary = CyberzoneTextOnGreen,
    primaryContainer = CyberzoneElectricGreenDark,
    onPrimaryContainer = CyberzoneTextPrimary,
    secondary = CyberzoneElectricGreenBright,
    onSecondary = CyberzoneTextOnGreen,
    secondaryContainer = CyberzoneSurfaceElevated,
    onSecondaryContainer = CyberzoneTextPrimary,
    background = CyberzoneBackground,
    onBackground = CyberzoneTextPrimary,
    surface = CyberzoneSurface,
    onSurface = CyberzoneTextPrimary,
    surfaceVariant = CyberzoneSurfaceElevated,
    onSurfaceVariant = CyberzoneTextSecondary,
    outline = CyberzoneBorder,
    outlineVariant = CyberzoneBorderSubtle,
    error = CyberzoneError,
    onError = CyberzoneTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberzoneColorScheme,
        typography = PinProTypography,
        content = content
    )
}
