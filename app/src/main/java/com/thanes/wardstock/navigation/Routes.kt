package com.thanes.wardstock.navigation

sealed class Routes(val route: String) {
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
  object EditUser: Routes(route = "edit_user_route")
  object AddUser: Routes(route = "add_user_route")
  object EditDrug: Routes(route = "edit_drug_route")
  object AddDrug: Routes(route = "add_drug_route")
  object EditMachine: Routes(route = "edit_machine_route")
  object AddMachine: Routes(route = "add_machine_route")
  object AddInventory: Routes(route = "add_inventory_route")
  object EditInventory: Routes(route = "edit_inventory_route")
  object EditGroup: Routes(route = "edit_group_route")
  object AddGroup: Routes(route = "add_group_route")
  object ManageReport: Routes(route = "manage_report")
  object ReportDrugMinMax: Routes(route = "report_drug_min_max")

  object DispenseTestTool: Routes(route = "dispensetesttool_route")
}