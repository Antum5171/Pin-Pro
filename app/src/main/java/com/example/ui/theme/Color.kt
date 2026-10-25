package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PinPro Design Tokens:
 * Refined dark grey theme (not pure black) with emerald green accent used sparingly
 * for primary actions, active states, and route lines.
 */
val CyberzoneBackground = Color(0xFF16181D)         // Refined dark grey background
val CyberzoneSurface = Color(0xFF20242B)            // Card surface
val CyberzoneSurfaceElevated = Color(0xFF292F38)    // Elevated card surface / input field
val CyberzoneBorder = Color(0xFF353D49)             // Subtle 1px border
val CyberzoneBorderSubtle = Color(0xFF272D36)

// Refined Emerald Green Accent (#10B981) used sparingly
val CyberzoneElectricGreen = Color(0xFF10B981)
val CyberzoneElectricGreenBright = Color(0xFF34D399)
val CyberzoneElectricGreenDark = Color(0xFF059669)
val CyberzoneElectricGreenGlow = Color(0x3310B981)
val CyberzoneElectricGreenSubtle = Color(0x1F10B981)

// Text tokens
val CyberzoneTextPrimary = Color(0xFFF3F4F6)
val CyberzoneTextSecondary = Color(0xFF9CA3AF)
val CyberzoneTextMuted = Color(0xFF6B7280)
val CyberzoneTextOnGreen = Color(0xFF111827)

// Status colors
val StatusPending = Color(0xFFF59E0B)       // Amber
val StatusOnTheWay = Color(0xFF3B82F6)      // Blue
val StatusDone = Color(0xFF10B981)          // Muted Green
val StatusUrgent = Color(0xFFEF4444)        // Red

val CyberzoneError = StatusUrgent
val CyberzoneWarning = StatusPending

// Aliases for full compatibility
val PinProNeonGreen = CyberzoneElectricGreen
val PinProNeonGreenPressed = CyberzoneElectricGreenDark
val PinProNeonGreenSubtle = CyberzoneElectricGreenSubtle
val PinProBackground = CyberzoneBackground
val PinProSurface = CyberzoneSurface
val PinProSurfaceVariant = CyberzoneSurfaceElevated
val PinProBorder = CyberzoneBorder
val PinProTextPrimary = CyberzoneTextPrimary
val PinProTextMuted = CyberzoneTextSecondary
val PinProError = CyberzoneError
val PinProWarning = CyberzoneWarning
