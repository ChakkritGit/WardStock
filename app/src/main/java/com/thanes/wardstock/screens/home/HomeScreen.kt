package com.thanes.wardstock.screens.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.ui.components.appbar.HomeAppBar
import com.thanes.wardstock.ui.components.home.HomeMenu
import com.thanes.wardstock.ui.components.home.HomeWrapperContent
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun HomeScreen(
  navController: NavHostController,
  context: Context,
  authViewModel: AuthViewModel,
  orderSharedViewModel: OrderViewModel,
) {
  val authState by authViewModel.authState.collectAsState()

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
        HomeAppBar(navController, context, authState, authViewModel, orderSharedViewModel)
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
          HomeMenu(navController, context, authState, orderSharedViewModel)
          HomeWrapperContent(context, orderSharedViewModel, authState)
        }
      }
    }
  }
}

//@Composable
//fun SlideToConfirm(
//  modifier: Modifier = Modifier,
//  width: Dp = 300.dp,
//  height: Dp = 56.dp,
//  onConfirm: () -> Unit
//) {
//  var sliderPosition by remember { mutableFloatStateOf(0f) }
//  val maxPositionPx = with(LocalDensity.current) { (width - height).toPx() }
//
//  var confirmed by remember { mutableStateOf(false) }
//
//  Box(
//    modifier = modifier
//      .width(width)
//      .height(height)
//      .clip(RoundedCornerShape(28.dp))
//      .background(Color.LightGray)
//  ) {
//    // background text
//    if (!confirmed) {
//      Text(
//        "Slide to confirm",
//        modifier = Modifier
//          .align(Alignment.Center),
//        color = Color.DarkGray,
//        fontWeight = FontWeight.Bold
//      )
//    } else {
//      Text(
//        "Confirmed!",
//        modifier = Modifier.align(Alignment.Center),
//        color = Color.Green,
//        fontWeight = FontWeight.Bold
//      )
//    }
//
//    Box(
//      modifier = Modifier
//        .offset { IntOffset(sliderPosition.roundToInt(), 0) }
//        .size(height)
//        .clip(RoundedCornerShape(28.dp))
//        .background(Color.Green)
//        .pointerInput(Unit) {
//          detectDragGestures(
//            onDragEnd = {
//              if (sliderPosition >= maxPositionPx * 0.9f) {
//                confirmed = true
//                onConfirm()
//              } else {
//                sliderPosition = 0f
//              }
//            },
//            onDrag = { change, dragAmount ->
//              change.consume()
//              val newPosition = (sliderPosition + dragAmount.x).coerceIn(0f, maxPositionPx)
//              sliderPosition = newPosition
//            }
//          )
//        }
//    )
//  }
//}
