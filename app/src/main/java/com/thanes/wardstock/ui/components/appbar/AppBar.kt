package com.thanes.wardstock.ui.components.appbar

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.style.TextOverflow
import com.thanes.wardstock.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
  title: String,
  onBack: () -> Unit
) {
  TopAppBar(
    title = {
      Text(
        text = title, maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Colors.BluePrimary
        )
      }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = Colors.BlueTertiary,
      titleContentColor = Colors.BluePrimary,
    ),
  )
}
