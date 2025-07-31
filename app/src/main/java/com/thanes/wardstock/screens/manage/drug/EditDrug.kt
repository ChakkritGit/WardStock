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
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.ImageUrl
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EditDrug(
  navController: NavHostController,
  drugSharedViewModel: DrugViewModel,
  context: Context,
  refillSharedViewModel: RefillViewModel,
  groupSharedViewModel: GroupViewModel
) {
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val successMessage = stringResource(R.string.successfully)

  val drug = drugSharedViewModel.selectedDrug

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
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
    if (drug != null) {
      val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

      val drugLotDate = LocalDate.parse(drug.drugLot, dateFormatter)
      val drugExpireDate = OffsetDateTime.parse(drug.drugExpire).toLocalDate()

      DrugFormScreen(
        context = context,
        innerPadding = innerPadding,
        isLoading = isLoading,
        navController = navController,
        drugSharedViewModel = drugSharedViewModel,
        initialData = DrugFormState(
          id = drug.id,
          picture = drug.picture.let { "${ImageUrl}${it}".toUri() },
          drugCode = drug.drugCode,
          drugName = drug.drugName,
          unit = drug.unit,
          weight = drug.weight,
          drugLot = drugLotDate,
          drugExpire = drugExpireDate,
          drugPriority = drug.drugPriority,
          status = drug.status,
          comment = drug.comment ?: ""
        ),
        onSubmit = { formState, uri ->
          if (isLoading) return@DrugFormScreen true

          try {
            isLoading = true

            val imagePart = uri?.let { uriToMultipartBodyPart(context, it) }

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formattedDrugLot = formState.drugLot.format(formatter)
            val formattedDrugExpire = formState.drugExpire.format(formatter)

            val response = if (imagePart != null) {
              ApiRepository.updateDrugWithImage(
                drugId = drug.id,
                imagePart = imagePart,
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
            } else {
              ApiRepository.updateDrugWithImage(
                drugId = drug.id,
                imagePart = null,
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
            }

            return@DrugFormScreen if (response.isSuccessful) {
              errorMessage = successMessage
              drugSharedViewModel.fetchDrug()
              refillSharedViewModel.fetchRefill()
              groupSharedViewModel.fetchGroup()
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
}