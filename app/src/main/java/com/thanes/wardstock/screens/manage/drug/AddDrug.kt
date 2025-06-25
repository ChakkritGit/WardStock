package com.thanes.wardstock.screens.manage.drug

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
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun AddDrug(navController: NavHostController, drugSharedViewModel: DrugViewModel) {
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
        if (isLoading == true) return@DrugFormScreen true

        val isValid = formState.drugCode.isNotBlank()
                && formState.drugName.isNotBlank()

        if (!isValid) {
          errorMessage = completeFieldMessage
          isLoading = false
          return@DrugFormScreen false
        }

        true
      }
    )
  }
}