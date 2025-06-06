package com.thanes.wardstock.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // Material 3 components are used
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFF6C63FF),
            Color(0xFF4A47E8)
          )
        )
      )
  ) {
    // Decorative elements
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

    // Main content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Login Form Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = Color.White
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
          // Title
          Text(
            text = "Sign in",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(32.dp))

          // Email Field - Updated to TextField
          TextField( // <<<< CHANGED from OutlinedTextField
            value = userName,
            onValueChange = { userName = it },
            label = {
              Text("Email") // Color.Gray removed, handled by TextFieldDefaults
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp), // Keep height for label animation
            // shape removed, not needed for bottom-line style
            colors = TextFieldDefaults.colors( // ใช้ TextFieldDefaults.colors สำหรับ Material 3
              focusedTextColor = Color(0xFF6C63FF),
              focusedIndicatorColor = Color(0xFF6C63FF),
              unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f),
              focusedLabelColor = Color(0xFF6C63FF),
              unfocusedLabelColor = Color.Gray,
              cursorColor = Color(0xFF6C63FF),
              // ระบุสี container อย่างชัดเจนเพื่อให้โปร่งใส
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              // ตัวเลือกเพิ่มเติม: ตั้งค่าสำหรับสถานะ disabled และ error หากต้องการ
              disabledContainerColor = Color.Transparent,
              errorContainerColor = Color.Transparent
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Password Field - Updated to TextField
          TextField( // <<<< CHANGED from OutlinedTextField
            value = userPassword,
            onValueChange = { userPassword = it },
            label = {
              Text("Password") // Color.Gray removed, handled by TextFieldDefaults
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp), // Keep height for label animation
            visualTransformation = PasswordVisualTransformation(),
            // shape removed, not needed for bottom-line style
            colors = TextFieldDefaults.colors( // ใช้ TextFieldDefaults.colors สำหรับ Material 3
              focusedTextColor = Color(0xFF6C63FF),
              focusedIndicatorColor = Color(0xFF6C63FF),
              unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.3f),
              focusedLabelColor = Color(0xFF6C63FF),
              unfocusedLabelColor = Color.Gray,
              cursorColor = Color(0xFF6C63FF),
              // ระบุสี container อย่างชัดเจนเพื่อให้โปร่งใส
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              // ตัวเลือกเพิ่มเติม: ตั้งค่าสำหรับสถานะ disabled และ error หากต้องการ
              disabledContainerColor = Color.Transparent,
              errorContainerColor = Color.Transparent
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Sign In Button
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
              containerColor = Color(0xFF6C63FF),
              disabledContainerColor = Color(0xFF6C63FF).copy(alpha = 0.6f)
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
                "Sign In",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Forgot Password Link
          TextButton(
            onClick = { /* TODO: Handle forgot password */ }
          ) {
            Text(
              "Forgot your password?",
              color = Color(0xFF6C63FF),
              fontSize = 14.sp
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Don't have account text
          Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Don't have an account? ",
              color = Color.Gray,
              fontSize = 14.sp
            )
            TextButton(
              onClick = { /* TODO: Navigate to sign up */ },
              contentPadding = PaddingValues(0.dp)
            ) {
              Text(
                "Sign up",
                color = Color(0xFF6C63FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
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
    }
  }
}