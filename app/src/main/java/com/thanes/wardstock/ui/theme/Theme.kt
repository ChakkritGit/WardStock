package com.thanes.wardstock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val LightColorScheme = lightColorScheme(
  primary = Colors.BluePrimary,
  secondary = Colors.BlueSecondary,
  tertiary = Colors.BlueTertiary
)

@Composable
fun WardStockTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
