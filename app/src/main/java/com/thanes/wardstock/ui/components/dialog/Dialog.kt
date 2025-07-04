package com.thanes.wardstock.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AlertDialog(
  dialogTitle: String,
  dialogText: String,
  icon: ImageVector,
) {
  AlertDialog(
    icon = {
      Icon(icon, contentDescription = "Example Icon")
    },
    title = {
      Text(text = dialogTitle)
    },
    text = {
      Text(text = dialogText)
    },
    onDismissRequest = {},
    confirmButton = {},
    dismissButton = {}
  )
}