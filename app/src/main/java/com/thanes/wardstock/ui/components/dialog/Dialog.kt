package com.thanes.wardstock.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun AlertDialog(
  dialogTitle: String,
  dialogText: String,
  icon: Int,
) {
  AlertDialog(
    containerColor = Colors.BlueGrey100,
    icon = {
      Surface(
        modifier = Modifier
          .clip(shape = CircleShape),
        color = Colors.BlueGrey80.copy(alpha = 0.5f)
      ) {
        Icon(
          painter = painterResource(icon),
          contentDescription = "schedule_24px",
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
          dialogTitle,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ibmpiexsansthailooped
        )
        Text(
          dialogText,
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    },
    onDismissRequest = {},
    confirmButton = {},
    dismissButton = {}
  )
}