package com.thanes.wardstock.screens.manage.drug

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.ImageUrl
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun EditDrug(navController: NavHostController, drugSharedViewModel: DrugViewModel) {
  val context = LocalContext.current
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val somethingWrongMessage = stringResource(R.string.something_wrong)
  val successMessage = stringResource(R.string.successfully)
  val completeFieldMessage = stringResource(R.string.complete_field)

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
          comment = drug.comment
        ),
        onSubmit = { formState, uri ->
          if (isLoading == true) return@DrugFormScreen true

          try {
            isLoading = true

            val imagePart = uri?.let { uriToMultipartBodyPart(context, it) }

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formattedDrugLot = formState.drugLot.format(formatter)
            val formattedDrugExpire = formState.drugExpire.format(formatter)

            val response = if (imagePart != null) {
              ApiRepository.updateDrugWithImage(
                context = context,
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
                context = context,
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
        })
    }
  }
}