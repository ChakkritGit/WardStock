package com.thanes.wardstock.screens.manage.user

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddUser(
  navController: NavHostController,
  userSharedViewModel: UserViewModel,
  fingerVeinViewModel: FingerVeinViewModel
) {
  val context = LocalContext.current
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
        title = stringResource(R.string.add_user),
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
    UserFormScreen(
      context = context,
      navController = null,
      userSharedViewModel = null,
      fingerVeinViewModel = fingerVeinViewModel,
      innerPadding = innerPadding,
      isLoading = isLoading,
      showPasswordField = true,
      onSubmit = { formState, uri ->
        if (isLoading) return@UserFormScreen true

        val isValid = formState.username.isNotBlank()
                && formState.password.isNotBlank()
                && formState.display.isNotBlank()
                && formState.role.isNotBlank()
                && uri != null

        if (!isValid) {
          errorMessage = completeFieldMessage
          isLoading = false
          return@UserFormScreen false
        }

        try {
          isLoading = true

          val imagePart = uriToMultipartBodyPart(context, uri)

          val response = ApiRepository.createUserWithImage(
            imagePart = imagePart!!,
            username = formState.username,
            password = formState.password,
            display = formState.display,
            role = UserRole.valueOf(formState.role),
            biometrics = formState.biometrics
          )

          return@UserFormScreen if (response.isSuccessful) {
            errorMessage = successMessage
            userSharedViewModel.fetchUser()
            fingerVeinViewModel.reloadAllBiometrics()
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
