package com.thanes.wardstock.ui.components.manage

import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.NavigationItem
import com.thanes.wardstock.navigation.Routes

class MenuItems {
  companion object {
    fun getMainMenuItems(): List<NavigationItem> {
      return listOf(
        NavigationItem(
          iconRes = R.drawable.group_24px,
          titleRes = R.string.user_management,
          route = Routes.UserManagement.route
        ),
        NavigationItem(
          iconRes = R.drawable.medication_24px,
          titleRes = R.string.drug_management,
          route = Routes.DrugManagement.route
        ),
        NavigationItem(
          iconRes = R.drawable.orders_24px,
          titleRes = R.string.stock_management,
          route = Routes.StockManagement.route
        ),
        NavigationItem(
          iconRes = R.drawable.manufacturing_24px,
          titleRes = R.string.machine_management,
          route = Routes.MachineManagement.route
        )
      )
    }
  }
}