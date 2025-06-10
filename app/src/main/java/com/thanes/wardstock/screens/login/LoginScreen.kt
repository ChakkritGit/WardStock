package com.thanes.wardstock.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.navigation.Routes
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun LoginScreen(navController: NavHostController, context: Context) {
  val scope = rememberCoroutineScope()
  var userName by remember { mutableStateOf("") }
  var userPassword by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  val completeFieldMessage = stringResource(R.string.complete_field)
  val userDataInCompleteMessage = stringResource(R.string.userData_InComplete)
  val somethingWrongMessage = stringResource(R.string.something_wrong)
  val cannotConnectToServerMessage = stringResource(R.string.something_wrong)

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
        val response = ApiRepository.login(userName, userPassword)

        if (response.isSuccessful) {
          val userData = response.body()?.data

          if (userData != null) {
            DataManager.saveToken(context, userData.token)
            DataManager.saveUserData(context, userData)
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
            somethingWrongMessage
          }
          errorMessage = message
        }
      } catch (_: Exception) {
        errorMessage = cannotConnectToServerMessage
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
            Colors.BlueSecondary,
            Colors.BluePrimary
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
        shape = RoundedCornerShape(24.dp),
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
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = stringResource(R.string.sign_in),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(32.dp))

          OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = {
              Text(stringResource(R.string.username_field))
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
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
            label = {
              Text(stringResource(R.string.password_field))
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
            visualTransformation = PasswordVisualTransformation(),
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

          Button(
            onClick = {
              if (isLoading) return@Button
              handleLogin()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Colors.BlueSecondary,
              disabledContainerColor = Colors.BlueSecondary.copy(alpha = 0.6f)
            ),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
              )
            } else {
              Text(
                stringResource(R.string.sign_in),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Colors.BlueGrey100
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          TextButton(
            onClick = { /* TODO: Handle forgot password */ }
          ) {
            Text(
              stringResource(R.string.forget_password),
              color = Colors.BlueSecondary,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
  }
}