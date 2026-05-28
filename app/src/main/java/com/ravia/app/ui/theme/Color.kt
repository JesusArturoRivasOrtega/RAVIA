package com.ravia.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── RAVIA Brand Blues ────────────────────────────────────────────────────────
val Navy950  = Color(0xFF060D1A)
val Navy900  = Color(0xFF0A1628)
val Navy800  = Color(0xFF0D1B2A)
val Navy700  = Color(0xFF112338)
val NavyPrimary = Color(0xFF1B3A6B)
val Blue700  = Color(0xFF1565C0)
val Blue600  = Color(0xFF1976D2)
val Blue500  = Color(0xFF1E88E5)
val Blue400  = Color(0xFF42A5F5)
val Blue300  = Color(0xFF90CAF9)
val Blue100  = Color(0xFFBBDEFB)
val Cyan600  = Color(0xFF0097A7)
val Cyan500  = Color(0xFF00B4D8)
val Cyan400  = Color(0xFF48CAE4)
val Cyan100  = Color(0xFFB2EBF2)

// ─── Status / Severity Colors ─────────────────────────────────────────────────
val StatusGreen       = Color(0xFF2DC653)   // Confirmed / Safe / Resolved
val StatusGreenDark   = Color(0xFF1B8E3E)   // Resolved (darker)
val StatusGreenBg     = Color(0xFFE8F5E9)
val StatusAmber       = Color(0xFFFFC107)   // Caution / Verifying
val StatusAmberDark   = Color(0xFFFF8F00)
val StatusAmberBg     = Color(0xFFFFF8E1)
val StatusOrange      = Color(0xFFFF6D00)   // High urgency
val StatusOrangeBg    = Color(0xFFFFF3E0)
val StatusRed         = Color(0xFFE53935)   // Critical / Emergency
val StatusRedDark     = Color(0xFFC62828)
val StatusRedBg       = Color(0xFFFFEBEE)
val StatusGray        = Color(0xFF78909C)   // Pending
val StatusGrayDark    = Color(0xFF546E7A)   // False reports
val StatusGrayBg      = Color(0xFFECEFF1)
val StatusPurple      = Color(0xFF8E24AA)   // Duplicated
val StatusPurpleBg    = Color(0xFFF3E5F5)
val StatusBlue        = Color(0xFF1E88E5)   // In Progress

// ─── Priority Colors ──────────────────────────────────────────────────────────
val PriorityLow      = Color(0xFF78909C)
val PriorityMedium   = Color(0xFFFFC107)
val PriorityHigh     = Color(0xFFFF6D00)
val PriorityCritical = Color(0xFFE53935)

// ─── Neutral Palette ──────────────────────────────────────────────────────────
val GrayBackground = Color(0xFFF5F7FA)
val GraySurface    = Color(0xFFEFF1F5)
val GrayBorder     = Color(0xFFDDE1E7)
val GrayLight      = Color(0xFFF8FAFC)
val GrayMedium     = Color(0xFF94A3B8)
val GrayText       = Color(0xFF64748B)
val GrayDark       = Color(0xFF334155)
val NearBlack      = Color(0xFF0F172A)
val White          = Color(0xFFFFFFFF)

// ─── Light theme color assignments ───────────────────────────────────────────
val md_theme_light_primary            = NavyPrimary
val md_theme_light_onPrimary          = White
val md_theme_light_primaryContainer   = Blue100
val md_theme_light_onPrimaryContainer = Navy900
val md_theme_light_secondary          = Cyan500
val md_theme_light_onSecondary        = White
val md_theme_light_secondaryContainer = Cyan100
val md_theme_light_onSecondaryContainer = Navy700
val md_theme_light_tertiary           = StatusGreen
val md_theme_light_onTertiary         = White
val md_theme_light_tertiaryContainer  = StatusGreenBg
val md_theme_light_onTertiaryContainer = StatusGreenDark
val md_theme_light_error              = StatusRed
val md_theme_light_onError            = White
val md_theme_light_errorContainer     = StatusRedBg
val md_theme_light_onErrorContainer   = StatusRedDark
val md_theme_light_background         = GrayBackground
val md_theme_light_onBackground       = NearBlack
val md_theme_light_surface            = White
val md_theme_light_onSurface          = NearBlack
val md_theme_light_surfaceVariant     = GraySurface
val md_theme_light_onSurfaceVariant   = GrayDark
val md_theme_light_outline            = GrayBorder
val md_theme_light_inverseSurface     = Navy800
val md_theme_light_inverseOnSurface   = GrayLight
val md_theme_light_inversePrimary     = Blue300

// ─── Dark theme color assignments ────────────────────────────────────────────
val md_theme_dark_primary            = Blue400
val md_theme_dark_onPrimary          = Navy900
val md_theme_dark_primaryContainer   = NavyPrimary
val md_theme_dark_onPrimaryContainer = Blue300
val md_theme_dark_secondary          = Cyan400
val md_theme_dark_onSecondary        = Navy700
val md_theme_dark_secondaryContainer = Cyan600
val md_theme_dark_onSecondaryContainer = Cyan100
val md_theme_dark_tertiary           = StatusGreen
val md_theme_dark_onTertiary         = Navy900
val md_theme_dark_tertiaryContainer  = StatusGreenDark
val md_theme_dark_onTertiaryContainer = StatusGreenBg
val md_theme_dark_error              = Color(0xFFFF6B6B)
val md_theme_dark_onError            = Navy900
val md_theme_dark_errorContainer     = StatusRedDark
val md_theme_dark_onErrorContainer   = StatusRedBg
val md_theme_dark_background         = Navy950
val md_theme_dark_onBackground       = GrayLight
val md_theme_dark_surface            = Navy800
val md_theme_dark_onSurface          = GrayLight
val md_theme_dark_surfaceVariant     = Navy700
val md_theme_dark_onSurfaceVariant   = Blue300
val md_theme_dark_outline            = Color(0xFF3D5275)
val md_theme_dark_inverseSurface     = GrayLight
val md_theme_dark_inverseOnSurface   = NearBlack
val md_theme_dark_inversePrimary     = NavyPrimary
