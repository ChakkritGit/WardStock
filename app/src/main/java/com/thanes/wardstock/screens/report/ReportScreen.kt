package com.thanes.wardstock.screens.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.components.report.ReportListItem
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun ReportScreen(navController: NavHostController) {
  var canClick by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.manage_report),
        onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        })
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      ReportListItem(navController)
    }
  }
}