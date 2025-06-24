package com.thanes.wardstock.screens.manage.user

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import org.json.JSONObject

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddUser(navController: NavHostController, userSharedViewModel: UserViewModel) {
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
      innerPadding = innerPadding,
      isLoading = isLoading,
      showPasswordField = true,
      onSubmit = { formState, uri ->
        isLoading = true

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
          val imagePart = uriToMultipartBodyPart(context, uri)

          val response = ApiRepository.createUserWithImage(
            context = context,
            imagePart = imagePart!!,
            username = formState.username,
            password = formState.password,
            display = formState.display,
            role = UserRole.valueOf(formState.role)
          )

          return@UserFormScreen if (response.isSuccessful) {
            errorMessage = successMessage
            userSharedViewModel.fetchUser()
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
