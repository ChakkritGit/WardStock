package com.thanes.wardstock.screens.manage.drug

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import java.time.format.DateTimeFormatter

@Composable
fun AddDrug(
  navController: NavHostController,
  drugSharedViewModel: DrugViewModel,
  context: Context
) {
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
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
        title = stringResource(R.string.add_drug),
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
    DrugFormScreen(
      context = context,
      innerPadding = innerPadding,
      isLoading = isLoading,
      navController = null,
      drugSharedViewModel = null,
      onSubmit = { formState, uri ->
        if (isLoading) return@DrugFormScreen true

        val isValid = formState.drugCode.isNotBlank()
                && formState.drugName.isNotBlank()
                && formState.unit.isNotBlank()
                && uri != null

        if (!isValid) {
          errorMessage = completeFieldMessage
          isLoading = false
          return@DrugFormScreen false
        }

        try {
          isLoading = true

          val imagePart = uriToMultipartBodyPart(context, uri)

          val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
          val formattedDrugLot = formState.drugLot.format(formatter)
          val formattedDrugExpire = formState.drugExpire.format(formatter)

          val response = ApiRepository.createDrugWithImage(
            imagePart = imagePart!!,
            drugCode = formState.drugCode,
            drugName = formState.drugName,
            unit = formState.unit,
            weight = formState.weight,
            drugLot = formattedDrugLot,
            drugExpire = formattedDrugExpire,
            drugPriority = formState.drugPriority,
            drugStatus = formState.status,
            comment = formState.comment
          )

          return@DrugFormScreen if (response.isSuccessful) {
            errorMessage = successMessage
            drugSharedViewModel.fetchDrug()
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
      }
    )
  }
}