package com.thanes.wardstock.ui.components.splashscreen

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onAnimationComplete: () -> Unit
) {
  var animationPhase by remember { mutableStateOf("initial") }

  val slowToFast = CubicBezierEasing(0.4f, 0f, 1f, 1f)

  val logoScale by animateFloatAsState(
    targetValue = when (animationPhase) {
      "initial" -> 1f
      "expanding", "fading" -> 500f
      else -> 1f
    },
    animationSpec = tween(
      durationMillis = 500,
      easing = if (animationPhase == "expanding") slowToFast else CubicBezierEasing(
        0.33f,
        0f,
        0.67f,
        1f
      )
    ),
    label = "logoScale"
  )

  val logoAlpha by animateFloatAsState(
    targetValue = when (animationPhase) {
      "fading" -> 0f
      else -> 1f
    },
    animationSpec = tween(
      durationMillis = 50,
      easing = slowToFast
    ),
    label = "logoAlpha"
  )

  val textAlpha by animateFloatAsState(
    targetValue = when (animationPhase) {
      "expanding", "fading" -> 0f
      else -> 1f
    },
    animationSpec = tween(
      durationMillis = 500,
      easing = slowToFast
    ),
    label = "textAlpha"
  )

  LaunchedEffect(Unit) {
    delay(300)
    animationPhase = "expanding"

    delay(400)
    animationPhase = "fading"

    delay(80)
    onAnimationComplete()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Colors.BlueGrey100),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .scale(logoScale)
        .alpha(logoAlpha),
      contentAlignment = Alignment.Center
    ) {
      Card(
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Colors.BluePrimary),
        elevation = CardDefaults.cardElevation(8.dp)
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "W",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Column(
      modifier = Modifier
        .alpha(textAlpha)
        .offset(y = 100.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(R.string.app_name),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Colors.BlueSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = stringResource(R.string.app_description_login),
        fontSize = 14.sp,
        color = Colors.BlueGrey40
      )
    }

    if (animationPhase == "initial") {
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(32.dp),
          color = Colors.BluePrimary,
          strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "${stringResource(R.string.is_Loading)}...",
          fontSize = 12.sp,
          color = Colors.BlueGrey40
        )
      }
    }
  }
}