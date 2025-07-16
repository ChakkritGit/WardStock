package com.thanes.wardstock.screens.manage.user

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
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
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.ImageUrl
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun EditUser(
  navController: NavHostController,
  userSharedViewModel: UserViewModel,
  fingerVeinViewModel: FingerVeinViewModel
) {
  var canClick by remember { mutableStateOf(true) }
  val context = LocalContext.current
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val successMessage = stringResource(R.string.successfully)

  val user = userSharedViewModel.selectedUser

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
    if (user != null) {
      UserFormScreen(
        context = context,
        navController = navController,
        userSharedViewModel = userSharedViewModel,
        fingerVeinViewModel = fingerVeinViewModel,
        innerPadding = innerPadding,
        isLoading = isLoading,
        initialData = UserFormState(
          userId = user.id,
          username = user.username,
          display = user.display,
          imageUri = user.picture.let { "${ImageUrl}${it}".toUri() },
          role = user.role.toString()
        ),
        showPasswordField = false,
        onSubmit = { formState, uri ->
          if (isLoading) return@UserFormScreen true

          try {
            isLoading = true

            val imagePart = uri?.let { uriToMultipartBodyPart(context, it) }

            val response = if (imagePart != null) {
              ApiRepository.updateUserWithImage(
                userId = user.id,
                imagePart = imagePart,
                username = formState.username,
                display = formState.display,
                role = UserRole.valueOf(formState.role)
              )
            } else {
              ApiRepository.updateUserWithImage(
                userId = user.id,
                imagePart = null,
                username = formState.username,
                display = formState.display,
                role = UserRole.valueOf(formState.role)
              )
            }

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
}
