package com.thanes.wardstock.screens.manage.machine

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
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage

@Composable
fun EditMachine(navController: NavHostController, machineSharedViewModel: MachineViewModel) {
  val context = LocalContext.current
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val successMessage = stringResource(R.string.successfully)
  val completeFieldMessage = stringResource(R.string.complete_field)

  val machine = machineSharedViewModel.selectedMachine

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.edit), onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        })
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    if (machine != null) {
      MachineFormScreen(
        context = context,
        innerPadding = innerPadding,
        isLoading = isLoading,
        navController = navController,
        machineSharedViewModel = machineSharedViewModel,
        initialData = MachineFormState(
          id = machine.id,
          machineName = machine.machineName,
          location = machine.location,
          capacity = machine.capacity,
          status = machine.status,
          comment = machine.comment ?: ""
        ),
        onSubmit = { formState ->
          if (isLoading) return@MachineFormScreen true

          try {
            isLoading = true

            val isValid =
              formState.machineName.isNotBlank() && formState.location.isNotBlank() && formState.capacity != 0

            if (!isValid) {
              errorMessage = completeFieldMessage
              isLoading = false
              return@MachineFormScreen false
            }

            val response = ApiRepository.updateMachine(
              id = machine.id,
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