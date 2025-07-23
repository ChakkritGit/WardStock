package com.thanes.wardstock.screens.manage.user

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.screens.fvverify.MainDisplay
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

data class BiometricData(
  val featureData: String,
  val description: String? = "Fingerprint"
)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AddFingerprint(
  navController: NavHostController,
  context: Context,
  userSharedViewModel: UserViewModel,
  fingerVeinViewModel: FingerVeinViewModel
) {
  var canClick by remember { mutableStateOf(true) }
  val isLockedOut by fingerVeinViewModel.isLockedOut
  val lockoutCountdown by fingerVeinViewModel.lockoutCountdown
  var enrolledBiometrics by remember { mutableStateOf<List<BiometricData>>(emptyList()) }

  LaunchedEffect(fingerVeinViewModel.lastEnrolledTemplate) {
    fingerVeinViewModel.lastEnrolledTemplate.value?.let { templateData ->
      enrolledBiometrics = listOf(BiometricData(featureData = templateData))

      fingerVeinViewModel.clearLastEnrolledTemplate()
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.back_button),
        onBack = {
          if (canClick) {
            canClick = false
//            fingerVeinViewModel.toggleVerify()
            navController.popBackStack()
          }
        })
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 30.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MainDisplay(
          bitmap = fingerVeinViewModel.imageBitmap.value,
          isEnrolling = fingerVeinViewModel.isEnrolling.value,
          isVerifying = fingerVeinViewModel.isVerifying.value,
          lastLogMessage = fingerVeinViewModel.logMessages.firstOrNull() ?: "",
          isLockedOut = isLockedOut,
          lockoutCountdown = lockoutCountdown
        )

        GradientButton(
          onClick = {
            if (canClick) {
              canClick = false
//              fingerVeinViewModel.toggleVerify()
              navController.popBackStack()
            }
          }, shape = RoundedCornerShape(RoundRadius.Medium), gradient = Brush.verticalGradient(
            colors = listOf(
              Colors.BlueGrey80, Colors.BlueGrey80
            ),
          ), modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
        ) {
          Text(
            stringResource(R.string.cancel),
            fontFamily = ibmpiexsansthailooped,
            color = Colors.BlueSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}