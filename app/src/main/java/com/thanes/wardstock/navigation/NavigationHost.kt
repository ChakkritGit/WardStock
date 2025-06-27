package com.thanes.wardstock.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.MachineViewModel
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.screens.home.HomeScreen
import com.thanes.wardstock.screens.login.LoginScreen
import com.thanes.wardstock.screens.manage.drug.ManageDrugScreen
import com.thanes.wardstock.screens.manage.ManageScreen
import com.thanes.wardstock.screens.manage.group.ManageStockScreen
import com.thanes.wardstock.screens.manage.drug.AddDrug
import com.thanes.wardstock.screens.manage.drug.EditDrug
import com.thanes.wardstock.screens.manage.machine.AddMachine
import com.thanes.wardstock.screens.manage.machine.EditMachine
import com.thanes.wardstock.screens.manage.machine.ManageMachineScreen
import com.thanes.wardstock.screens.manage.user.AddUser
import com.thanes.wardstock.screens.manage.user.EditUser
import com.thanes.wardstock.screens.manage.user.ManageUserScreen
import com.thanes.wardstock.ui.components.Refill.RefillDrug
import com.thanes.wardstock.screens.refill.RefillScreen
import com.thanes.wardstock.screens.setting.SettingScreen
import com.thanes.wardstock.screens.setting.dispense.DispenseTestTool
import com.thanes.wardstock.services.internet.rememberConnectivityState
import com.thanes.wardstock.ui.components.internet.NoInternetComposable

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
  navController: NavHostController,
  innerPadding: PaddingValues,
  splashScreen: SplashScreen,
  context: Context
) {
  val authViewModel: AuthViewModel = viewModel()
  val refillSharedViewModel: RefillViewModel = viewModel()
  val orderSharedViewModel: OrderViewModel = viewModel()
  val userSharedViewModel: UserViewModel = viewModel()
  val drugSharedViewModel: DrugViewModel = viewModel()
  val machineSharedViewModel: MachineViewModel = viewModel()
  val inventorySharedViewModel: InventoryViewModel = viewModel()
  val groupSharedViewModel: GroupViewModel = viewModel()

  val authState by authViewModel.authState.collectAsState()

  val isConnected by rememberConnectivityState(context)
  val isInitialConnected = remember { isConnected }

  if (!isInitialConnected) {
    NoInternetComposable()
    return
  }

  LaunchedEffect(Unit) {
    authViewModel.initializeAuth(context)
  }

  if (authState.isLoading) {
    splashScreen.setKeepOnScreenCondition { true }
    return
  } else {
    splashScreen.setKeepOnScreenCondition { false }
  }

  val startDestination = if (authState.isAuthenticated) {
    Routes.Home.route
  } else {
    Routes.Login.route
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = Modifier.padding(innerPadding)
  ) {

    composable(route = Routes.Login.route) {
      LoginScreen(navController, authViewModel, context)
    }

    composable(route = Routes.Home.route) {
      HomeScreen(navController, context, authViewModel, orderSharedViewModel)
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
      ManageUserScreen(navController, authState, userSharedViewModel)
    }

    composable(route = Routes.DrugManagement.route) {
      ManageDrugScreen(navController, drugSharedViewModel)
    }

    composable(route = Routes.StockManagement.route) {
      ManageStockScreen(navController, inventorySharedViewModel, groupSharedViewModel)
    }

    composable(route = Routes.MachineManagement.route) {
      ManageMachineScreen(navController, machineSharedViewModel)
    }

    composable(route = Routes.EditUser.route) {
      EditUser(navController, userSharedViewModel)
    }

    composable(route = Routes.AddUser.route) {
      AddUser(navController, userSharedViewModel)
    }

    composable(route = Routes.EditDrug.route) {
      EditDrug(navController, drugSharedViewModel)
    }

    composable(route = Routes.AddDrug.route) {
      AddDrug(navController, drugSharedViewModel)
    }

    composable(route = Routes.EditMachine.route) {
      EditMachine(navController, machineSharedViewModel)
    }

    composable(route = Routes.AddMachine.route) {
      AddMachine(navController, machineSharedViewModel)
    }
  }
}
