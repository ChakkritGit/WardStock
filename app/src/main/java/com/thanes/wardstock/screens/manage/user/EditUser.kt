package com.thanes.wardstock.screens.manage.user

import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.services.upload.uriToMultipartBodyPart
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.ImageUrl
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@Composable
fun EditUser(navController: NavHostController, userSharedViewModel: UserViewModel) {
  var canClick by remember { mutableStateOf(true) }
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

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
        title = user?.display ?: "—",
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
        innerPadding = innerPadding,
        isLoading = isLoading,
        initialData = UserFormState(
          username = user.username,
          display = user.display,
          imageUri = user.picture.let { "${ImageUrl}${it}".toUri() },
          role = user.role.toString(),
        ),
        showPasswordField = false,
        onSubmit = { formState, uri ->
          isLoading = true

          val result = try {
            scope.launch {
              val imagePart = if (uri != null) {
                uriToMultipartBodyPart(context, uri)
              } else null

//              val response = ApiRepository.updateUserWithImage(
//                context = context,
//                userId = user.id,
//                imagePart = imagePart,
//                username = formState.username,
//                password = formState.password.takeIf { it.isNotBlank() },
//                display = formState.display,
//                role = UserRole.valueOf(formState.role)
//              )

//              if (response.isSuccessful) {
//                userSharedViewModel.fetchUser()
//                navController.popBackStack()
//              } else {
//                val errorJson = response.errorBody()?.string()
//                val message = try {
//                  JSONObject(errorJson ?: "").getString("message")
//                } catch (_: Exception) {
//                  "HTTP Error ${response.code()}: ${response.message()}"
//                }
//                errorMessage = message
//              }
            }
            true
          } catch (e: Exception) {
            errorMessage = "ไม่สามารถบันทึกการแก้ไขได้ (${e.message})"
            false
          } finally {
            isLoading = false
          }

          result
        }
      )
    }
  }
}
