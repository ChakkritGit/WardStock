package com.thanes.wardstock.screens.manage.group

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
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.components.loadifempty.LoadIfEmpty
import com.thanes.wardstock.ui.theme.Colors
import org.json.JSONObject

@Composable
fun EditGroup(
  navController: NavHostController,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel,
  refillSharedViewModel: RefillViewModel,
  drugSharedViewModel: DrugViewModel,
) {
  val context = LocalContext.current
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val somethingWrongMessage = stringResource(R.string.something_wrong)
  val successMessage = stringResource(R.string.successfully)
  val completeFieldMessage = stringResource(R.string.complete_field)

  val group = groupSharedViewModel.selectedGroupInventory

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LoadIfEmpty(inventorySharedViewModel.inventoryState) { inventorySharedViewModel.fetchInventory() }
  LoadIfEmpty(inventorySharedViewModel.inventoryExitsState) { inventorySharedViewModel.fetchInventoryExits() }
  LoadIfEmpty(drugSharedViewModel.drugState) { drugSharedViewModel.fetchDrug() }
  LoadIfEmpty(drugSharedViewModel.drugExitsState) { drugSharedViewModel.fetchDrugExits() }

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
    if (group != null) {
      GroupFormScreen(
        context = context,
        innerPadding = innerPadding,
        isLoading = isLoading,
        navController = navController,
        inventorySharedViewModel = inventorySharedViewModel,
        groupSharedViewModel = groupSharedViewModel,
        refillSharedViewModel = refillSharedViewModel,
        drugSharedViewModel = drugSharedViewModel,
        initialData = GroupFormState(
          groupId = group.groupid,
          drugId = group.drugid,
          groupMin = group.groupmin,
          groupMax = group.groupmax,
          inventories = group.inventoryList
        ),
        onSubmit = { formState ->
          if (isLoading) return@GroupFormScreen true

          try {
            isLoading = true

            val isValid =
              formState.drugId != null && formState.groupMin != 0 && formState.groupMax != 0 && formState.inventories != null

            if (!isValid) {
              errorMessage = completeFieldMessage
              isLoading = false
              return@GroupFormScreen false
            }

            val response = ApiRepository.updateGroup(
              context = context,
              groupId = group.groupid,
              drugId = formState.drugId,
              groupMin = formState.groupMin,
              groupMax = formState.groupMax,
              inventories = formState.inventories
            )

            return@GroupFormScreen if (response.isSuccessful) {
              errorMessage = successMessage
              groupSharedViewModel.fetchGroup()
              drugSharedViewModel.fetchDrugExits()
              inventorySharedViewModel.fetchInventoryExits()
              refillSharedViewModel.fetchRefill()
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
}