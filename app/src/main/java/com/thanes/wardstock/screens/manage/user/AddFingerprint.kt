package com.thanes.wardstock.screens.manage.user

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.screens.fvverify.MainDisplay
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch
data class BiometricData(
  val featureData: String,
  val description: String? = "Fingerprint"
)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFingerprint(
  navController: NavHostController,
  context: Context,
  userSharedViewModel: UserViewModel,
  fingerVeinViewModel: FingerVeinViewModel
) {
  var description by remember { mutableStateOf("") }
  var featureData by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val scope = rememberCoroutineScope()

  val isEnrolling by fingerVeinViewModel.isEnrolling

  fun handleAddFingerprint() {
    if (isLoading) return
    if (description.isBlank() || featureData.isNullOrBlank()) {
      Toast.makeText(context, "กรุณาสแกนลายนิ้วมือและใส่คำอธิบาย", Toast.LENGTH_SHORT).show()
      return
    }

    isLoading = true
    scope.launch {
      try {
        val response = ApiRepository.addFingerprint(
          userId = userSharedViewModel.selectedUser?.id ?: "",
          featureData = featureData!!,
          description = description
        )

        if (response.isSuccessful) {
          Toast.makeText(context, "เพิ่มลายนิ้วมือสำเร็จ", Toast.LENGTH_SHORT).show()
          fingerVeinViewModel.reloadAllBiometrics()
          userSharedViewModel.fetchUserFingerprint(userSharedViewModel.selectedUser?.id ?: "")
          navController.popBackStack()
        } else {
          val errorJson = response.errorBody()?.string()
          errorMessage = parseErrorMessage(response.code(), errorJson)
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isLoading = false
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      if (fingerVeinViewModel.isEnrolling.value) {
        fingerVeinViewModel.enroll(uid = "", uname = "")
      }
    }
  }

  LaunchedEffect(fingerVeinViewModel.lastEnrolledTemplate.value) {
    fingerVeinViewModel.lastEnrolledTemplate.value?.let { templateData ->
      featureData = templateData
      fingerVeinViewModel.clearLastEnrolledTemplate()
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LoadingDialog(isRemoving = isLoading)

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.add_finger_vein)) },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(
              painter = painterResource(R.drawable.arrow_back_ios_new_24px),
              contentDescription = "Back"
            )
          }
        },
        actions = {
          TextButton(
            onClick = { handleAddFingerprint() },
            enabled = !description.isBlank() && !featureData.isNullOrBlank() && !isLoading
          ) {
            Text(stringResource(R.string.done_button))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Colors.BlueGrey100)
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      MainDisplay(
        bitmap = fingerVeinViewModel.imageBitmap.value,
        isVerifying = isEnrolling,
        lastLogMessage = fingerVeinViewModel.logMessages.firstOrNull()
          ?: "กด 'เริ่มสแกน' เพื่อลงทะเบียน",
        isLockedOut = false,
        lockoutCountdown = 0
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("คำอธิบาย (เช่น นิ้วชี้ขวา)") },
        singleLine = true,
        shape = RoundedCornerShape(RoundRadius.Large),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Colors.BlueGrey120,
          unfocusedContainerColor = Colors.BlueGrey120,
          focusedBorderColor = Colors.BluePrimary,
        )
      )

      if (!featureData.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .background(Colors.success.copy(0.1f), RoundedCornerShape(RoundRadius.Large))
            .padding(16.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.check_24px),
            contentDescription = "Success",
            tint = Colors.success
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("สแกนลายนิ้วมือสำเร็จแล้ว", color = Colors.success)
        }
      }
    }
  }
}