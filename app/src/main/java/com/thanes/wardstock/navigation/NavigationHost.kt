package com.thanes.wardstock.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.screens.home.HomeScreen
import com.thanes.wardstock.screens.login.LoginScreen
import com.thanes.wardstock.screens.setting.SettingScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(navController: NavHostController, innerPadding: PaddingValues, context: Context) {
  var token by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    token = DataManager.getToken(context)
  }

  token?.let { tokenValue ->
    val startDestination = if (tokenValue.isNotEmpty()) Routes.Home.route else Routes.Login.route

    NavHost(
      navController = navController,
      startDestination = startDestination,
      modifier = Modifier.padding(innerPadding)
    ) {
      composable(route = Routes.Login.route) {
        LoginScreen(navController, context)
      }
      composable(route = Routes.Home.route) {
        HomeScreen(navController, context)
      }
      composable(route = Routes.Setting.route) {
        SettingScreen(navController, context)
      }
    }
  }
}