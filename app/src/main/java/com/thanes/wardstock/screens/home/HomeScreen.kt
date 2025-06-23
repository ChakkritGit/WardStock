package com.thanes.wardstock.screens.home

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.ui.components.appbar.HomeAppBar
import com.thanes.wardstock.ui.components.home.HomeMenu
import com.thanes.wardstock.ui.components.home.HomeWrapperContent
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun HomeScreen(
  navController: NavHostController,
  context: Context,
  authViewModel: AuthViewModel,
  orderSharedViewModel: OrderViewModel
) {
  val authState by authViewModel.authState.collectAsState()

  Scaffold(
    containerColor = Colors.BluePrimary,
    topBar = { HomeAppBar(navController, context, authState, authViewModel, orderSharedViewModel) }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxHeight()
    ) {
      Column(
        modifier = Modifier
          .fillMaxHeight()
      ) {
        HomeMenu(navController, context, authState, orderSharedViewModel)
        HomeWrapperContent(context, orderSharedViewModel)
      }
    }
  }
}
