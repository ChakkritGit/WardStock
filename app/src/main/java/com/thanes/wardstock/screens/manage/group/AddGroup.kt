package com.thanes.wardstock.screens.manage.group

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun AddGroup(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel,
  refillSharedViewModel: RefillViewModel,
  drugSharedViewModel: DrugViewModel
) {
  val context = LocalContext.current
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val somethingWrongMessage = stringResource(R.string.something_wrong)
  val successMessage = stringResource(R.string.successfully)
  val completeFieldMessage = stringResource(R.string.complete_field)

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LaunchedEffect(inventorySharedViewModel.inventoryState) {
    if (inventorySharedViewModel.inventoryState.isEmpty()) {
      inventorySharedViewModel.fetchInventory()
    }
  }

  LaunchedEffect(drugSharedViewModel.drugState) {
    if (drugSharedViewModel.drugState.isEmpty()) {
      drugSharedViewModel.fetchDrug()
    }
  }

  LaunchedEffect(drugSharedViewModel.drugExitsState) {
    if (drugSharedViewModel.drugExitsState.isEmpty()) {
      drugSharedViewModel.fetchDrugExits()
    }
  }

  LaunchedEffect(inventorySharedViewModel.inventoryExitsState) {
    if (inventorySharedViewModel.inventoryExitsState.isEmpty()) {
      inventorySharedViewModel.fetchInventoryExits()
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.add_group),
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
    GroupFormScreen(
      context = context,
      innerPadding = innerPadding,
      isLoading = isLoading,
      navController = navController,
      inventorySharedViewModel = inventorySharedViewModel,
      groupSharedViewModel = groupSharedViewModel,
      refillSharedViewModel = refillSharedViewModel,
      drugSharedViewModel = drugSharedViewModel,
      onSubmit = { formState ->
        true
      }
    )
  }
}