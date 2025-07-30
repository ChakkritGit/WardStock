package com.thanes.wardstock.screens.home

// เพิ่ม import ที่จำเป็นสำหรับ Animation
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.MachineStatusViewModel
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.ui.components.appbar.HomeAppBar
import com.thanes.wardstock.ui.components.home.HomeMenu
import com.thanes.wardstock.ui.components.home.HomeSelectDispense
import com.thanes.wardstock.ui.components.home.HomeWrapperContent
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
  navController: NavHostController,
  context: Context,
  authViewModel: AuthViewModel,
  orderSharedViewModel: OrderViewModel,
  groupSharedViewModel: GroupViewModel,
  machineStatusViewModel: MachineStatusViewModel
) {
  val authState by authViewModel.authState.collectAsState()
  var errorMessage by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  val toggleDispense = remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Colors.BluePrimary,
            Colors.BlueSky,
          ),
          start = Offset(0f, 0f),
          end = Offset.Infinite
        )
      )
  ) {
    Scaffold(
      topBar = {
        HomeAppBar(
          navController,
          context,
          authState,
          authViewModel,
          orderSharedViewModel,
          machineStatusViewModel
        )
      },
      floatingActionButton = {
        if (toggleDispense.value) {
          ExtendedFloatingActionButton(
            onClick = {
              if (isLoading) return@ExtendedFloatingActionButton

              scope.launch {
                isLoading = true
                try {
                  val response = ApiRepository.clearPrescription()
                  if (response.isSuccessful) {
                    errorMessage = response.body()?.data ?: "Successfully"
                  } else {
                    val errorJson = response.errorBody()?.string()
                    val message = parseErrorMessage(response.code(), errorJson)
                    errorMessage = message
                  }
                } catch (e: Exception) {
                  errorMessage = parseExceptionMessage(e)
                } finally {
                  orderSharedViewModel.fetchOrderInitial()
                  isLoading = false
                }
              }
            },
            containerColor = Colors.BluePrimary,
            icon = {
              if (isLoading) {
                val infiniteTransition = rememberInfiniteTransition(label = "reset_icon_rotation")
                val angle by infiniteTransition.animateFloat(
                  initialValue = 0f,
                  targetValue = 360f,
                  animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing)
                  ),
                  label = "rotation_angle"
                )
                Icon(
                  painter = painterResource(R.drawable.autorenew_24px),
                  contentDescription = "Resetting prescription",
                  tint = Colors.BlueGrey80,
                  modifier = Modifier
                    .size(24.dp)
                    .rotate(angle)
                )
              } else {
                Icon(
                  painter = painterResource(R.drawable.autorenew_24px),
                  contentDescription = "autorenew_24px",
                  tint = Colors.BlueGrey80,
                  modifier = Modifier.size(24.dp)
                )
              }
            },
            text = {
              Text(
                stringResource(R.string.reset_prescription),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
              )
            },
            shape = RoundedCornerShape(RoundRadius.Medium),
            modifier = Modifier.height(54.dp)
          )
        }
      },
      containerColor = Color.Transparent
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .padding(innerPadding)
          .fillMaxHeight()
      ) {
        Column(
          modifier = Modifier
            .fillMaxHeight()
        ) {
          HomeMenu(navController, context, authState, orderSharedViewModel, toggleDispense)
          if (!toggleDispense.value) {
            HomeSelectDispense(context, groupSharedViewModel)
          } else {
            HomeWrapperContent(context, orderSharedViewModel, authState)
          }
        }
      }
    }
  }
}