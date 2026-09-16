package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Modern, Refined Color Palette (Apple / Google M3 Inspired)
// Clean, warm, accessible, avoiding the "matrix" terminal aesthetic.
// =========================================================================

// Brand Accents
val BrandBlue = Color(0xFF2563EB)         // Vibrant Azure Sapphire
val BrandBlueLight = Color(0xFFEFF6FF)
val BrandBlueDark = Color(0xFF1D4ED8)

val BrandAmber = Color(0xFFF59E0B)        // Warm Daylight Sun Amber
val BrandAmberLight = Color(0xFFFFFBEB)
val BrandAmberDark = Color(0xFFD97706)

// PAC Blocks Colors (Matching official #SOYBARINAS / Corpoelec PAC graphic)
val BlockAColor = Color(0xFFEA580C)       // Bloque A - Naranja / Coral
val BlockABg = Color(0xFFFFF7ED)
val BlockABorder = Color(0xFFFED7AA)

val BlockBColor = Color(0xFF0284C7)       // Bloque B - Azul Cielo
val BlockBBg = Color(0xFFF0F9FF)
val BlockBBorder = Color(0xFFBAE6FD)

val BlockCColor = Color(0xFFCA8A04)       // Bloque C - Amarillo / Ámbar
val BlockCBg = Color(0xFFFEFCE8)
val BlockCBorder = Color(0xFFFEF08A)

val BlockDColor = Color(0xFF16A34A)       // Bloque D - Verde
val BlockDBg = Color(0xFFF0FDF4)
val BlockDBorder = Color(0xFFBBF7D0)

// Status Colors (Warm, human, non-generic)
val StatusNormalGreen = Color(0xFF10B981)
val StatusNormalGreenBg = Color(0xFFECFDF5)
val StatusNormalGreenBorder = Color(0xFFA7F3D0)
val StatusNormalGreenText = Color(0xFF065F46)

val StatusScheduledRed = Color(0xFFF43F5E) // Warm Rose / Coral (not harsh neon red)
val StatusScheduledRedBg = Color(0xFFFFF1F2)
val StatusScheduledRedBorder = Color(0xFFFECDD3)
val StatusScheduledRedText = Color(0xFF9F1239)

val StatusIrregularPurple = Color(0xFF8B5CF6)
val StatusIrregularPurpleBg = Color(0xFFF5F3FF)

// -------------------------------------------------------------------------
// LIGHT THEME PALETTE (Primary default: warm, airy, clean porcelain canvas)
// -------------------------------------------------------------------------
val LightCanvas = Color(0xFFF8FAFC)       // Soft luminous slate-white
val LightSurface = Color(0xFFFFFFFF)      // Crisp pure white cards
val LightSurfaceVariant = Color(0xFFF1F5F9)// Subtle tinted surface for inputs/chips
val LightBorder = Color(0xFFE2E8F0)       // Ultra-soft card boundary
val LightBorderSubtle = Color(0xFFF1F5F9)
val LightTextPrimary = Color(0xFF0F172A)  // Deep slate navy (sharp, elegant, readable)
val LightTextSecondary = Color(0xFF475569)// Slate medium gray
val LightTextTertiary = Color(0xFF94A3B8) // Slate muted gray

// -------------------------------------------------------------------------
// DARK THEME PALETTE (Google AI Studio Carbon & Dark Charcoal Aesthetic)
// Clean minimalist grays and deep carbon blacks matching Google AI Studio interface
// -------------------------------------------------------------------------
val DarkCanvas = Color(0xFF131314)        // Exact Google AI Studio deep charcoal/black canvas
val DarkSurface = Color(0xFF1E1F20)       // Exact Google AI Studio editor & panel card background
val DarkSurfaceVariant = Color(0xFF282A2C)// Google AI Studio elevated rows, input & button surfaces
val DarkSurfaceContainerHigh = Color(0xFF2E3134) // High elevation cards/modals
val DarkBorder = Color(0xFF333538)        // Subtle minimalist border divider from Google AI Studio
val DarkBorderSubtle = Color(0xFF27292B)  // Sub-row dividers
val DarkTextPrimary = Color(0xFFE3E3E3)   // Google AI Studio high-contrast crisp text
val DarkTextSecondary = Color(0xFFC4C7C5) // Google AI Studio medium-contrast secondary text
val DarkTextTertiary = Color(0xFF8E918F)  // Google AI Studio muted text

// Google AI Studio Minimalist Accent Palette
val GoogleAISkyBlue = Color(0xFFA8C7FA)   // Signature Google / Gemini Light Blue
val GoogleAIBlueContainer = Color(0xFF1F2B3E)
val GoogleAIOnBlueContainer = Color(0xFFD3E3FD)
val GoogleAIAmber = Color(0xFFFDD663)     // Google Warm Amber
val GoogleAIGreen = Color(0xFF81C995)     // Google Soft Emerald Green
val GoogleAIRed = Color(0xFFF28B82)       // Google Soft Coral Red
val GoogleAIPurple = Color(0xFFD7AEFB)    // Google Soft Pastel Lilac

// Error Colors
val ErrorRed = Color(0xFFBA1A1A)
val ErrorRedContainer = Color(0xFFFFDAD6)

// Compatibility aliases
val GoogleBlue = BrandBlue
val GoogleGreen = StatusNormalGreen
val GoogleYellow = BrandAmber
val GoogleRed = StatusScheduledRed
val ElectricAmber = BrandAmber
val ElectricAmberDark = BrandAmberDark
val ElectricAmberLight = BrandAmberLight
val ElectricBlue = BrandBlue
val ElectricBlueLight = GoogleAISkyBlue
val ElectricBlueDark = BrandBlueDark
