package com.thanes.wardstock.ui.components.dispense

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

suspend fun showAuthDialogUntilVerified(context: Context): Boolean {
  val result = CompletableDeferred<Boolean>()

  val activity = context as? ComponentActivity ?: return false

  activity.runOnUiThread {
    activity.setContent {
      var showDialog by remember { mutableStateOf(true) }

      if (showDialog) {
        AuthConfirmDialog(
          onConfirm = { username, password ->
            val verified = verifyUser(username, password)
            if (verified) {
              showDialog = false
              result.complete(true)
            }
            verified
          },
          onDismiss = {
            showDialog = false
            result.complete(false)
          }
        )
      }
    }
  }

  return result.await()
}

@Composable
fun AuthConfirmDialog(
  onConfirm: suspend (String, String) -> Boolean,
  onDismiss: () -> Unit
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("ยืนยันตัวตน") },
    text = {
      Column {
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username") }
        )
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password") },
          visualTransformation = PasswordVisualTransformation()
        )
        errorMessage?.let {
          Spacer(Modifier.height(8.dp))
          Text(it, color = Colors.alert)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = {
        CoroutineScope(Dispatchers.Main).launch {
          val verified = onConfirm(username, password)
          if (!verified) {
            errorMessage = "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"
          }
        }
      }) {
        Text("อนุญาต")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("ปิด")
      }
    }
  )
}

suspend fun verifyUser(username: String, password: String): Boolean {
  return try {
    val response = ApiRepository.veryUser(username, password)
    if (response.isSuccessful) {
      response.body()?.data == "OVERRIDDEN"
    } else {
      val errorJson = response.errorBody()?.string()
      Log.e("VerifyUser", "Error: ${parseErrorMessage(response.code(), errorJson)}")
      false
    }
  } catch (e: Exception) {
    Log.e("VerifyUser", "Error: ${parseExceptionMessage(e)}")
    false
  }
}