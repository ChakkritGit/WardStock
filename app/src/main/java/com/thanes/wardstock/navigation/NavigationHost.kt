package com.thanes.wardstock.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.screens.home.HomeScreen
import com.thanes.wardstock.screens.login.LoginScreen
import com.thanes.wardstock.screens.manage.ManageScreen
import com.thanes.wardstock.ui.components.Refill.RefillDrug
import com.thanes.wardstock.screens.refill.RefillScreen
import com.thanes.wardstock.screens.setting.SettingScreen
import com.thanes.wardstock.screens.setting.dispense.DispenseTestTool

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(navController: NavHostController, innerPadding: PaddingValues, context: Context) {
  var token by remember { mutableStateOf<String?>(null) }

  val refillSharedViewModel: RefillViewModel = viewModel()
  val orderSharedViewModel: OrderViewModel = viewModel()

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
        HomeScreen(navController, context, orderSharedViewModel)
      }

      composable(route = Routes.Setting.route) {
        SettingScreen(navController, context)
      }

      composable(route = Routes.DispenseTestTool.route) {
        DispenseTestTool(navController, context)
      }

      composable(route = Routes.Refill.route) {
        RefillScreen(navController, context, refillSharedViewModel)
      }

      composable(route = Routes.RefillDrug.route) {
        RefillDrug(navController, context, refillSharedViewModel)
      }

      composable(route = Routes.Manage.route) {
        ManageScreen(navController)
      }

      composable(route = Routes.UserManagement.route) {
        Text("User Management")
      }

      composable(route = Routes.DrugManagement.route) {
        Text("Drug Management")
      }

      composable(route = Routes.StockManagement.route) {
        Text("Stock Management")
      }

      composable(route = Routes.MachineManagement.route) {
        Text("Machine Management")
      }
    }
  }
}