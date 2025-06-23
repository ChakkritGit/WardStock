package com.thanes.wardstock.screens.manage.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun EditUser(navController: NavHostController, userSharedViewModel: UserViewModel) {
  var canClick by remember { mutableStateOf(true) }
  var userData = userSharedViewModel.selectedUser

  Scaffold(
    topBar = {
      AppBar(
        title = userData?.display ?: "—",
        onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {

    }
  }
}