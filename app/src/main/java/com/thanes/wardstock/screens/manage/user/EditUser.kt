package com.thanes.wardstock.screens.manage.user

import android.util.Log
import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.ImageUrl
import androidx.core.net.toUri
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.data.repositories.ApiRepository
import org.json.JSONObject

@Composable
fun EditUser(navController: NavHostController, userSharedViewModel: UserViewModel) {
  var canClick by remember { mutableStateOf(true) }
  val context = LocalContext.current
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val somethingWrongMessage = stringResource(R.string.something_wrong)
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
        innerPadding = innerPadding,
        isLoading = isLoading,
        initialData = UserFormState(
          userId = user.id,
          username = user.username,
          display = user.display,
          imageUri = user.picture.let { "${ImageUrl}${it}".toUri() },
          role = user.role.toString(),
        ),
        showPasswordField = false,
        onSubmit = { formState, uri ->
          if (isLoading == true) return@UserFormScreen true

          try {
            isLoading = true

            val imagePart = uri?.let { uriToMultipartBodyPart(context, it) }

            val response = if (imagePart != null) {
              ApiRepository.updateUserWithImage(
                context = context,
                userId = user.id,
                imagePart = imagePart,
                username = formState.username,
                display = formState.display,
                role = UserRole.valueOf(formState.role)
              )
            } else {
              ApiRepository.updateUserWithImage(
                context = context,
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
