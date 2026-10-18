package nl.mlhk.tvlauncher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MLHK 2026 Core Palette (from mlhk.nl / mlhk-source.css)
val MlhkBackground = Color(0xFF080712)
val MlhkSurface = Color(0xFF12111E)
val MlhkSurfaceHover = Color(0xFF25213A)
val MlhkCardBg = Color(0xFF120E20)
val MlhkCardBorder = Color(0x1AFFFFFF)
val MlhkCardBorderGlow = Color(0x66FF2F7D)

val MlhkPrimary = Color(0xFFFF4C9A)
val MlhkNeonPink = Color(0xFFFF2F7D)
val MlhkNeonPinkHover = Color(0xFFFF478F)
val MlhkNeonCyan = Color(0xFF00E5FF)
val MlhkNeonPurple = Color(0xFF9B51E0)
val MlhkNeonViolet = Color(0xFF5949F2)

val MlhkTextMain = Color(0xFFF8F5FF)
val MlhkTextMuted = Color(0xFFAAA3BD)
val MlhkTextSub = Color(0xFFC9BCD6)

val StatusGreen = Color(0xFF10B981)
val StatusOrange = Color(0xFFF59E0B)
val StatusRed = Color(0xFFEF4444)
val StatusGrey = Color(0xFF6B7280)

// MLHK Signature Gradient
val MlhkBrandGradient = Brush.horizontalGradient(
    listOf(MlhkNeonPink, MlhkNeonPurple, MlhkNeonCyan)
)
val MlhkAccentLineGradient = Brush.horizontalGradient(
    listOf(Color.Transparent, MlhkNeonPink.copy(alpha = 0.8f), MlhkNeonViolet.copy(alpha = 0.6f), MlhkNeonCyan.copy(alpha = 0.8f), Color.Transparent)
)
