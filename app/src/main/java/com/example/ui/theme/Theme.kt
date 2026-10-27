package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DjColorScheme = darkColorScheme(
  primary = DjGold,
  onPrimary = Color.Black,
  primaryContainer = DjGoldDark,
  onPrimaryContainer = DjGoldBright,
  secondary = DjCyan,
  onSecondary = Color.Black,
  secondaryContainer = DjSurfaceVariant,
  onSecondaryContainer = DjCyan,
  tertiary = DjAmber,
  onTertiary = Color.Black,
  background = DjDarkBg,
  onBackground = DjTextPrimary,
  surface = DjPanelBg,
  onSurface = DjTextPrimary,
  surfaceVariant = DjSurfaceVariant,
  onSurfaceVariant = DjTextSecondary,
  outline = DjBorder
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DjColorScheme,
    typography = Typography,
    content = content
  )
}
