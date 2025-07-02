package com.thanes.wardstock.ui.components.loadifempty

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LoadIfEmpty(state: List<*>, fetch: () -> Unit) {
  LaunchedEffect(state) {
    if (state.isEmpty()) fetch()
  }
}
