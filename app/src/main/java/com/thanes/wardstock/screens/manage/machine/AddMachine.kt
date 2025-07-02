package com.thanes.wardstock.screens.manage.machine

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
import com.thanes.wardstock.data.viewModel.MachineViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import org.json.JSONObject

@Composable
fun AddMachine(navController: NavHostController, machineSharedViewModel: MachineViewModel) {
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

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.add_machine),
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
    MachineFormScreen(
      context = context,
      innerPadding = innerPadding,
      isLoading = isLoading,
      navController = null,
      machineSharedViewModel = null,
      onSubmit = { formState ->
        if (isLoading) return@MachineFormScreen true

        val isValid = formState.machineName.isNotBlank()
                && formState.location.isNotBlank()
                && formState.capacity > 0

        if (!isValid) {
          errorMessage = completeFieldMessage
          isLoading = false
          return@MachineFormScreen false
        }

        try {
          isLoading = true

          val response = ApiRepository.createMachine(
            machineName = formState.machineName,
            location = formState.location,
            capacity = formState.capacity,
            status = formState.status,
            comment = formState.comment
          )

          return@MachineFormScreen if (response.isSuccessful) {
            errorMessage = successMessage
            machineSharedViewModel.fetchMachine()
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