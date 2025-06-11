package com.thanes.wardstock.screens.home

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.models.UserData
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.ui.components.appbar.HomeAppBar
import com.thanes.wardstock.ui.components.home.HomeMenu
import com.thanes.wardstock.ui.components.home.HomeWrapperContent
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun HomeScreen(navController: NavHostController, context: Context) {
  var userData by remember { mutableStateOf<UserData?>(null) }

  LaunchedEffect(Unit) {
    userData = DataManager.getUserData(context)
  }

  Scaffold(
    containerColor = Colors.BluePrimary,
    topBar = { HomeAppBar(navController, context) }
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
        if (userData?.role == "SUPER") {
          HomeMenu(navController, context)
        }
        HomeWrapperContent()
      }
    }
  }
}
