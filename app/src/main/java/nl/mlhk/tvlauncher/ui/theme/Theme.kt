package nl.mlhk.tvlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MlhkPrimary,
    secondary = MlhkNeonPink,
    tertiary = MlhkNeonCyan,
    background = MlhkBackground,
    surface = MlhkSurface,
    onPrimary = MlhkBackground,
    onSecondary = MlhkTextMain,
    onBackground = MlhkTextMain,
    onSurface = MlhkTextMain
)

@Composable
fun MLHKTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
