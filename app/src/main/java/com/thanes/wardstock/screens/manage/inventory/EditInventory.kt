package com.thanes.wardstock.screens.manage.inventory

import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.MachineViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage

@Composable
fun EditInventory(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  machineSharedViewModel: MachineViewModel,
  refillSharedViewModel: RefillViewModel
) {
  val context = LocalContext.current
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val successMessage = stringResource(R.string.successfully)
  val completeFieldMessage = stringResource(R.string.complete_field)

  val inventory = inventorySharedViewModel.selectedInventory

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LaunchedEffect(machineSharedViewModel.machineState) {
    if (machineSharedViewModel.machineState.isEmpty()) {
      machineSharedViewModel.fetchMachine()
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.edit),
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
    if (inventory != null) {
      InventoryFormScreen(
        context = context,
        innerPadding = innerPadding,
        isLoading = isLoading,
        navController = navController,
        inventorySharedViewModel,
        machineSharedViewModel = machineSharedViewModel,
        refillSharedViewModel = refillSharedViewModel,
        initialData = InventoryFormState(
          id = inventory.id,
          position = inventory.position,
          min = inventory.min,
          max = inventory.max,
          status = inventory.status,
          machineId = inventory.machineId,
          comment = inventory.comment ?: ""
        ),
        onSubmit = { formState ->
          if (isLoading) return@InventoryFormScreen true

          try {
            isLoading = true

            val isValid =
              formState.position != null

            if (!isValid) {
              errorMessage = completeFieldMessage
              isLoading = false
              return@InventoryFormScreen false
            }

            val response = ApiRepository.updateInventory(
              id = inventory.id,
              position = formState.position,
              min = formState.min,
              max = formState.max,
              status = formState.status,
              machineId = formState.machineId,
              comment = formState.comment
            )

            return@InventoryFormScreen if (response.isSuccessful) {
              errorMessage = successMessage
              machineSharedViewModel.fetchMachine()
              refillSharedViewModel.fetchRefill()
              navController.popBackStack()
              true
            } else {
              val errorJson = response.errorBody()?.string()
              val message = parseErrorMessage(response.code(), errorJson)
              errorMessage = message
              false
            }
          } catch (e: Exception) {
            errorMessage = parseExceptionMessage(e)
            false
          } finally {
            isLoading = false
          }
        })
    }
  }
}