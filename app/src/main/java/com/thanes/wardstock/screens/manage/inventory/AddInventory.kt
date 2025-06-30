package com.thanes.wardstock.screens.manage.inventory

import android.util.Log
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
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.MachineViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import org.json.JSONObject

data class Position(val label: String, val value: Int)

@Composable
fun AddInventory(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel,
  machineSharedViewModel: MachineViewModel
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

  LaunchedEffect(machineSharedViewModel.machineState) {
    if (machineSharedViewModel.machineState.isEmpty()) {
      machineSharedViewModel.fetchMachine()
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.add_inventory),
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
    InventoryFormScreen(
      context = context,
      innerPadding = innerPadding,
      isLoading = isLoading,
      navController = navController,
      inventorySharedViewModel = inventorySharedViewModel,
      machineSharedViewModel = machineSharedViewModel,
      onSubmit = { formState ->
        if (isLoading) return@InventoryFormScreen true

        val isValid = formState.position != 0
                && formState.min != 0
                && formState.max != 0
                && formState.machineId.isNotBlank()

        if (!isValid) {
          errorMessage = completeFieldMessage
          isLoading = false
          return@InventoryFormScreen false
        }

        try {
          isLoading = true

          val response = ApiRepository.createInventory(
            context = context,
            position = formState.position,
            min = formState.min,
            max = formState.max,
            machineId = formState.machineId,
            status = formState.status,
            comment = formState.comment
          )

          return@InventoryFormScreen if (response.isSuccessful) {
            errorMessage = successMessage
            inventorySharedViewModel.fetchInventory()
            navController.popBackStack()
            true
          } else {
            val errorJson = response.errorBody()?.string()
            val message = try {
              JSONObject(errorJson ?: "").getString("message")
            } catch (_: Exception) {
              when (response.code()) {
                400 -> "Invalid request data"
                401 -> "Authentication required"
                403 -> "Access denied"
                404 -> "Prescription not found"
                500 -> "Server error, please try again later"
                else -> "HTTP Error ${response.code()}: ${response.message()}"
              }
            }
            errorMessage = message
            false
          }
        } catch (e: Exception) {
          errorMessage = when (e) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timeout, please try again"
            is java.net.ConnectException -> "Unable to connect to server"
            is javax.net.ssl.SSLException -> "Secure connection failed"
            is com.google.gson.JsonSyntaxException -> "Invalid response format"
            is java.io.IOException -> "Network error occurred"
            else -> {
              Log.e("AddUser", "Unexpected error", e)
              "Unexpected error occurred: $somethingWrongMessage"
            }
          }
          false
        } finally {
          isLoading = false
        }
      }
    )
  }
}