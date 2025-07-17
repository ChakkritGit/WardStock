package com.thanes.wardstock.ui.components.appbar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.AuthState
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.ImageUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

fun getGreetingMessage(): Int {
  val hour = LocalDateTime.now().hour
  return when (hour) {
    in 5..11 -> R.string.good_morning
    in 12..16 -> R.string.good_afternoon
    in 17..19 -> R.string.good_evening
    else -> R.string.good_night
  }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomeAppBar(
  navController: NavHostController,
  context: Context,
  authState: AuthState,
  authViewModel: AuthViewModel,
  orderSharedViewModel: OrderViewModel
) {
  val scope = rememberCoroutineScope()
  var greetingMessage by remember { mutableIntStateOf(getGreetingMessage()) }
  var openAlertDialog by remember { mutableStateOf(false) }
  val isOrderActive = orderSharedViewModel.orderState != null
  var alertMessage by remember { mutableStateOf("") }
  val waitForDispenseMessage = stringResource(R.string.wait_for_dispensing)
  val userData = authState.userData
  val contextTwo = LocalContext.current

  fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
      if (context is Activity) return context
      context = context.baseContext
    }
    return null
  }

  LaunchedEffect(Unit) {
    while (true) {
      delay(60 * 60 * 1000L)
      greetingMessage = getGreetingMessage()
    }
  }

  LaunchedEffect(alertMessage) {
    if (alertMessage.isNotEmpty()) {
      Toast.makeText(context, alertMessage, Toast.LENGTH_SHORT).show()
      alertMessage = ""
    }
  }

  Row(verticalAlignment = Alignment.CenterVertically) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(68.dp)
            .background(color = Colors.BluePrimary, shape = CircleShape)
            .border(
              width = 2.dp,
              color = Colors.BlueGrey100,
              shape = CircleShape
            )
        ) {
          AsyncImage(
            model = ImageUrl + userData?.picture,
            contentDescription = "ProfilePicture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
          )
        }
        Column {
          Text(stringResource(id = greetingMessage), fontSize = 22.sp, color = Colors.BlueGrey80)
          Text(
            userData?.display ?: "-",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Colors.white
          )
        }
      }
      Button(
        onClick = {
          if (!isOrderActive) {
            openAlertDialog = true
          } else {
            alertMessage = waitForDispenseMessage
          }
        },
        modifier = Modifier.alpha(if (isOrderActive) 0.5f else 1f),
        colors = ButtonDefaults.buttonColors(Color.Transparent)
      ) {
        Icon(
          painter = painterResource(R.drawable.logout_24px),
          contentDescription = "Logout",
          tint = Colors.BlueGrey100,
          modifier = Modifier
            .size(48.dp)
        )
      }
    }
  }

  LaunchedEffect(openAlertDialog) {
    if (openAlertDialog) {
      delay(20)
      (context as? Activity)?.let { activity ->
        HideSystemControll.manageSystemBars(activity, true)
      }
    }
  }

  if (openAlertDialog) {
    AlertDialog(
      properties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = true
      ),
      icon = {
        Surface(
          modifier = Modifier
            .clip(shape = CircleShape),
          color = Colors.BlueGrey80.copy(alpha = 0.5f)
        ) {
          Icon(
            painter = painterResource(R.drawable.lock_24px),
            contentDescription = "lock_24px",
            modifier = Modifier
              .size(56.dp)
              .padding(6.dp)
          )
        }
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(0.7f)
        ) {
          Text(
            contextTwo.getString(R.string.logout),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ibmpiexsansthailooped
          )
          Text(
            contextTwo.getString(R.string.logout_description),
            fontSize = 20.sp,
            fontFamily = ibmpiexsansthailooped
          )
        }
      },
      onDismissRequest = {
        openAlertDialog = false
      },
      confirmButton = {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          GradientButton(
            onClick = {
              scope.launch {
                authViewModel.logout(context)
                navController.navigate(Routes.Login.route) {
                  popUpTo(Routes.Home.route) { inclusive = true }
                }
              }
              openAlertDialog = false
            },
            text = contextTwo.getString(R.string.logout),
            fontWeight = FontWeight.Medium,
            shape = RoundedCornerShape(RoundRadius.Medium),
            textSize = 20.sp,
            modifier = Modifier
              .fillMaxWidth(0.7f)
              .height(56.dp)
          )

          GradientButton(
            onClick = {
              openAlertDialog = false
            },
            shape = RoundedCornerShape(RoundRadius.Medium),
            gradient = Brush.verticalGradient(
              colors = listOf(
                Colors.BlueGrey80,
                Colors.BlueGrey80
              ),
            ),
            modifier = Modifier
              .fillMaxWidth(0.7f)
              .height(56.dp)
          ) {
            Text(
              contextTwo.getString(R.string.cancel),
              fontFamily = ibmpiexsansthailooped,
              color = Colors.BlueSecondary,
              fontSize = 20.sp,
            )
          }
        }
      },
      dismissButton = {},
      containerColor = Colors.BlueGrey100
    )
  }
}
