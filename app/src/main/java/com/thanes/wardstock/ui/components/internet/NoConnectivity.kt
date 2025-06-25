package com.thanes.wardstock.ui.components.internet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun NoInternetComposable() {
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
      Surface(
        modifier = Modifier.clip(CircleShape), color = Color(0xFFDE3C30).copy(alpha = 0.15f)
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

      Text(
        stringResource(R.string.no_connection),
        color = Color(0xFFDE3C30),
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = ibmpiexsansthailooped
      )
    }
  }
}