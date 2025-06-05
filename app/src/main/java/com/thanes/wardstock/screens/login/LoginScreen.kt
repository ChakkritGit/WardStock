package com.thanes.wardstock.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.store.DataManager
import com.thanes.wardstock.navigation.Routes
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun LoginScreen(navController: NavHostController, context: Context) {
  val scope = rememberCoroutineScope()
  var userName by remember { mutableStateOf("") }
  var userPassword by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  fun handleLogin() {
    errorMessage = ""
    isLoading = true

    scope.launch {
      if (userName.isEmpty() || userPassword.isEmpty()) {
        errorMessage = "กรุณากรอกข้อมูลให้ครบ"
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
            errorMessage = "ข้อมูลผู้ใช้ไม่สมบูรณ์"
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val message = try {
            JSONObject(errorJson ?: "").getString("message")
          } catch (e: Exception) {
            "เกิดข้อผิดพลาดบางอย่าง"
          }
          errorMessage = message
        }
      } catch (_: Exception) {
        errorMessage = "ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์"
      } finally {
        isLoading = false
      }
    }
  }

  Box(
    modifier = Modifier
      .padding(10.dp)
      .fillMaxHeight()
  ) {
    Column(
//      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxHeight()
    ) {
      Text("Ward Stock", fontSize = 32.sp, fontWeight = FontWeight.Medium)
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = userName,
        onValueChange = { userName = it },
        label = { Text("ชื่อผู้ใช้") },
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp),
        shape = RoundedCornerShape(50.dp)
      )
      Spacer(modifier = Modifier.height(10.dp))
      OutlinedTextField(
        value = userPassword,
        onValueChange = { userPassword = it },
        label = { Text("รหัสผ่าน") },
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp),
        shape = RoundedCornerShape(50.dp),
        visualTransformation = PasswordVisualTransformation(),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = {
          if (isLoading) return@Button
          handleLogin()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(60.dp),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
          )
        } else {
          Text("เข้าสู่ระบบ")
        }
      }

      LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
          Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
}