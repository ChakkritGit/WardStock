package com.thanes.wardstock.screens.manage.inventory

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.R

data class Position(val label: String, val value: Int)

@Composable
fun AddInventory(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel
) {
  val context = LocalContext.current

  fun getAvailablePositions(context: Context, inventorySharedViewModel: InventoryViewModel): List<Position> {
    val labelPrefix = context.getString(R.string.drug_inventory_no)

    val usedPositions = inventorySharedViewModel.inventoryState.map { it.position }

    return List(60) { index ->
      Position(label = "$labelPrefix ${index + 1}", value = index + 1)
    }.filter { position -> !usedPositions.contains(position.value) }
  }

  LaunchedEffect(inventorySharedViewModel.inventoryState) {
    if (inventorySharedViewModel.inventoryState.isNotEmpty()) {
      val availablePositions = getAvailablePositions(context, inventorySharedViewModel)
      Log.d("Available Positions", availablePositions.toString())
    }
  }
}