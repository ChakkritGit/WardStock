package com.thanes.wardstock.screens.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.navigation.Routes
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.ui.components.keyboard.Keyboard
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius

@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel, context: Context) {
  val scope = rememberCoroutineScope()
  var userName by remember { mutableStateOf("") }
  var userPassword by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var showPass by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  val focusRequesterPassword = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val hideKeyboard = Keyboard.hideKeyboard()

  val completeFieldMessage = stringResource(R.string.complete_field)
  val userDataInCompleteMessage = stringResource(R.string.userData_InComplete)
  val somethingWrongMessage = stringResource(R.string.something_wrong)

  fun handleLogin() {
    errorMessage = ""
    isLoading = true

    scope.launch {
      if (userName.isEmpty() || userPassword.isEmpty()) {
        errorMessage = completeFieldMessage
        isLoading = false
        return@launch
      }

      try {
        hideKeyboard()
        val response = ApiRepository.login(userName, userPassword)

        if (response.isSuccessful) {
          val userData = response.body()?.data

          if (userData != null) {
            authViewModel.login(context, userData.token, userData)
            navController.navigate(Routes.Home.route) {
              popUpTo(Routes.Login.route) { inclusive = true }
            }
          } else {
            errorMessage = userDataInCompleteMessage
          }
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
        }
      } catch (e: Exception) {
        errorMessage = when (e) {
          is java.net.UnknownHostException -> {
            "No internet connection"
          }

          is java.net.SocketTimeoutException -> {
            "Request timeout, please try again"
          }

          is java.net.ConnectException -> {
            "Unable to connect to server"
          }

          is javax.net.ssl.SSLException -> {
            "Secure connection failed"
          }

          is com.google.gson.JsonSyntaxException -> {
            "Invalid response format"
          }

          is java.io.IOException -> {
            "Network error occurred"
          }

          else -> {
            Log.e("OrderAPI", "Unexpected error: ${e.javaClass.simpleName}", e)
            "Unexpected error occurred: $somethingWrongMessage"
          }
        }
      } finally {
        isLoading = false
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Colors.BlueSecondary, Colors.BluePrimary
          )
        )
      )
  ) {
    Box(
      modifier = Modifier
        .size(120.dp)
        .offset(x = 50.dp, y = 80.dp)
        .clip(RoundedCornerShape(60.dp))
        .background(Color.White.copy(alpha = 0.1f))
    )

    Box(
      modifier = Modifier
        .size(80.dp)
        .offset(x = 300.dp, y = 150.dp)
        .clip(RoundedCornerShape(40.dp))
        .background(Color.White.copy(alpha = 0.15f))
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 52.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(RoundRadius.Large),
        colors = CardDefaults.cardColors(
          containerColor = Colors.BlueGrey100
        ),
        elevation = CardDefaults.cardElevation(
          defaultElevation = 8.dp
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
        ) {
          Column {
            Image(
              painter = painterResource(R.drawable.login_banner),
              contentDescription = "LoginBanner",
              modifier = Modifier
                .width(290.dp)
                .height(290.dp),
              contentScale = ContentScale.Fit
            )
            Text(
              text = stringResource(R.string.app_title_login),
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Colors.BlueSecondary,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = stringResource(R.string.app_description_login),
              fontSize = 22.sp,
              fontWeight = FontWeight.Medium,
              color = Colors.BlueGrey40,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.height(32.dp))

          OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text(stringResource(R.string.username_field)) },
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp),
            shape = RoundedCornerShape(RoundRadius.Large),
            textStyle = TextStyle(fontSize = 20.sp),
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.person_24px),
                contentDescription = "User Icon",
                tint = Colors.BlueGrey40,
                modifier = Modifier.size(32.dp)
              )
            },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = {
                focusRequesterPassword.requestFocus()
              }),
            colors = TextFieldDefaults.colors(
              focusedTextColor = Colors.BlueSecondary,
              focusedIndicatorColor = Colors.BlueSecondary,
              unfocusedIndicatorColor = Colors.BlueSecondary.copy(alpha = 0.3f),
              focusedLabelColor = Colors.BlueSecondary,
              unfocusedLabelColor = Colors.BlueGrey40,
              cursorColor = Colors.BlueSecondary,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              errorContainerColor = Color.Transparent
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            label = { Text(stringResource(R.string.password_field)) },
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .focusRequester(focusRequesterPassword),
            shape = RoundedCornerShape(RoundRadius.Large),
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = TextStyle(fontSize = 20.sp),
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.lock_24px),
                contentDescription = "Password Icon",
                tint = Colors.BlueGrey40,
                modifier = Modifier.size(32.dp)
              )
            },
            singleLine = true,
            maxLines = 1,
            trailingIcon = {
              IconButton(
                modifier = Modifier.padding(end = 4.dp), onClick = { showPass = !showPass }) {
                Icon(
                  painter = painterResource(
                    if (!showPass) R.drawable.visibility_24px else R.drawable.visibility_off_24px
                  ),
                  contentDescription = if (showPass) "Hide password" else "Show password",
                  tint = Colors.BlueGrey40,
                  modifier = Modifier.size(32.dp)
                )
              }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                keyboardController?.hide()
                handleLogin()
              }),
            colors = TextFieldDefaults.colors(
              focusedTextColor = Colors.BlueSecondary,
              focusedIndicatorColor = Colors.BlueSecondary,
              unfocusedIndicatorColor = Colors.BlueSecondary.copy(alpha = 0.3f),
              focusedLabelColor = Colors.BlueSecondary,
              unfocusedLabelColor = Colors.BlueGrey40,
              cursorColor = Colors.BlueSecondary,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              errorContainerColor = Color.Transparent
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          GradientButton(
            onClick = {
              if (isLoading) return@GradientButton
              handleLogin()
            },
            shape = RoundedCornerShape(RoundRadius.Large),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
              )
            } else {
              Text(
                stringResource(R.string.sign_in),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Colors.BlueGrey100
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentSize(Alignment.CenterEnd)
          ) {
            TextButton(onClick = { /* TODO: Forgot password */ }) {
              Text(
                stringResource(R.string.forget_password),
                color = Colors.BlueSecondary,
                fontSize = 18.sp
              )
            }
          }
        }
      }
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }
}
