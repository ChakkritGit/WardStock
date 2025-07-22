package com.thanes.wardstock.screens.manage.user

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.screens.fvverify.MainDisplay
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.delay

data class BiometricData(
  val featureData: String
)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ManageFinger(
  navController: NavHostController,
  context: Context,
  userSharedViewModel: UserViewModel,
  fingerVeinViewModel: FingerVeinViewModel
) {
  val isLockedOut by fingerVeinViewModel.isLockedOut
  val lockoutCountdown by fingerVeinViewModel.lockoutCountdown
  var canClick by remember { mutableStateOf(true) }
  var showEnrollDialog by remember { mutableStateOf(false) }
  var enrolledBiometrics by remember { mutableStateOf<List<BiometricData>>(emptyList()) }

  LaunchedEffect(fingerVeinViewModel.lastEnrolledTemplate) {
    fingerVeinViewModel.lastEnrolledTemplate.value?.let { templateData ->
      enrolledBiometrics = listOf(BiometricData(featureData = templateData))

      showEnrollDialog = false

      fingerVeinViewModel.clearLastEnrolledTemplate()
    }
  }

  LaunchedEffect(showEnrollDialog) {
    if (showEnrollDialog) {
      delay(20)
      (context as? Activity)?.let { activity ->
        HideSystemControll.manageSystemBars(activity, true)
      }
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.back_button), onBack = {
          if (canClick) {
            canClick = false
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
        Text(
          stringResource(R.string.add_finger_vein),
          fontSize = 22.sp,
          fontWeight = FontWeight.Medium,
          color = Colors.BlueGrey40,
          modifier = Modifier.padding(start = 4.dp, top = 12.dp)
        )
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          items(userSharedViewModel.fingerprintList ?: emptyList()) { fingerprint ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RoundRadius.Large))
                .clickable(onClick = {
                  userSharedViewModel.setFingerObject(fingerprint)
                  navController.navigate(Routes.EditFingerprint.route)
                }),
              colors = CardDefaults.cardColors(Colors.BlueGrey120),
              shape = RoundedCornerShape(RoundRadius.Large),
              border = BorderStroke(1.dp, color = Colors.BlueGrey80)
            ) {
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .padding(horizontal = 18.dp, vertical = 12.dp)
                  .height(50.dp)
                  .fillMaxWidth()
              ) {
                Text(
                  fingerprint.description,
                  fontSize = 20.sp,
                  modifier = Modifier
                    .widthIn(max = 500.dp)
                    .padding(start = 16.dp),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Icon(
                  painter = painterResource(id = R.drawable.chevron_right_24px),
                  contentDescription = "chevron_right_24px",
                  tint = Colors.BlueGrey40,
                  modifier = Modifier
                    .padding(end = 6.dp)
                    .size(32.dp)
                )
              }
            }
          }
        }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RoundRadius.Large))
            .clickable(onClick = {}, enabled = userSharedViewModel.fingerprintList?.size!! < 5)
            .alpha(if (userSharedViewModel.fingerprintList?.size!! < 5) 1f else 0.5f),
          colors = CardDefaults.cardColors(Colors.BlueGrey120),
          shape = RoundedCornerShape(RoundRadius.Large),
          border = BorderStroke(1.dp, color = Colors.BlueGrey80),
        ) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .padding(horizontal = 18.dp, vertical = 12.dp)
              .height(50.dp)
              .fillMaxWidth()
          ) {
            Text(
              stringResource(R.string.add_finger_vein_button),
              fontSize = 20.sp,
              color = Colors.BlueSky,
              modifier = Modifier.padding(start = 16.dp)
            )
          }
        }
        Text(
          stringResource(R.string.add_finger_vein_description),
          fontSize = 16.sp,
          color = Colors.BlueGrey40,
          modifier = Modifier.padding(start = 4.dp)
        )
      }
    }
  }

  if (showEnrollDialog) {
    AlertDialog(
      properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false
      ),
      icon = {},
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(0.9f)
        ) {
          MainDisplay(
            bitmap = fingerVeinViewModel.imageBitmap.value,
            isEnrolling = fingerVeinViewModel.isEnrolling.value,
            isVerifying = fingerVeinViewModel.isVerifying.value,
            lastLogMessage = fingerVeinViewModel.logMessages.firstOrNull() ?: "",
            isLockedOut = isLockedOut,
            lockoutCountdown = lockoutCountdown
          )
        }
      },
      onDismissRequest = {
        showEnrollDialog = false
      },
      confirmButton = {},
      dismissButton = {
        GradientButton(
          onClick = {
//              fingerVienViewModel.toggleVerify()
            showEnrollDialog = false
          }, shape = RoundedCornerShape(RoundRadius.Medium), gradient = Brush.verticalGradient(
            colors = listOf(
              Colors.BlueGrey80, Colors.BlueGrey80
            ),
          ), modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(56.dp)
        ) {
          Text(
            stringResource(R.string.close),
            fontFamily = ibmpiexsansthailooped,
            color = Colors.BlueSecondary,
            fontSize = 20.sp,
          )
        }
      },
      containerColor = Colors.BlueGrey100,
    )
  }
}