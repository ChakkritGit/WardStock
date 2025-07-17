package com.thanes.wardstock.ui.components.internet

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.screens.setting.dispense.DispenseTestTool
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NoInternetComposable(navController: NavHostController, context: Context) {
  var showDispenseTestTool by remember { mutableStateOf(false) }
  var mode by remember { mutableStateOf(false) }

  if (!showDispenseTestTool) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFDE3C30).copy(alpha = 0.1f), Colors.BlueGrey100),
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
      ) {
        TextButton(onClick = {
          mode = !mode
        }) {
          Surface(
            modifier = Modifier.clip(CircleShape),
            color = Color(0xFFDE3C30).copy(alpha = 0.15f)
          ) {
            Icon(
              painter = painterResource(R.drawable.signal_disconnected_24px),
              contentDescription = stringResource(R.string.no_connection),
              modifier = Modifier
                .size(76.dp)
                .padding(12.dp),
              tint = Color(0xFFDE3C30)
            )
          }
        }

        Text(
          stringResource(R.string.no_connection),
          color = Color(0xFFDE3C30),
          fontSize = 20.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = ibmpiexsansthailooped
        )

        GradientButton(
          onClick = {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (context is Activity) {
              context.finishAffinity()
            }
            context.startActivity(intent)
            exitProcess(0)
          },
          gradient = Brush.verticalGradient(
            colors = listOf(Colors.alert.copy(0.25f), Colors.alert.copy(0.25f)),
          ),
          shape = RoundedCornerShape(RoundRadius.Medium),
          modifier = Modifier
            .height(56.dp)
            .padding(horizontal = 24.dp),
        ) {
          Text(
            stringResource(R.string.re_launch),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Colors.alert
          )
        }

        if (mode) {
          HorizontalDivider(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 128.dp, vertical = 12.dp),
            color = Colors.alert.copy(0.15f)
          )

          GradientButton(
            onClick = {
              showDispenseTestTool = true
            },
            gradient = Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color.Transparent),
            ),
            shape = RoundedCornerShape(RoundRadius.Medium),
            modifier = Modifier
              .padding(horizontal = 24.dp),
          ) {
            Text(
              stringResource(R.string.dispense_test_tool),
              fontSize = 20.sp,
              fontWeight = FontWeight.Medium,
              color = Colors.BluePrimary
            )
          }
        }
      }
    }
  } else {
    DispenseTestTool(navController, context)
  }
}
