package com.thanes.wardstock.navigation

sealed  class Routes(val route: String) {
  object Login: Routes(route = "login_route")
  object Home: Routes(route = "home_route")
  object Setting: Routes(route = "setting_route")
  object Refill: Routes(route = "refill_route")
  object RefillDrug: Routes(route = "refill_drug_route")
  object Manage: Routes(route = "manage_route")
  object UserManagement: Routes(route = "user_management")
  object DrugManagement: Routes(route = "drug_management")
  object StockManagement: Routes(route = "stock_management")
  object MachineManagement: Routes(route = "machine_management")
  object DispenseTestTool: Routes(route = "dispensetesttool_route")
}