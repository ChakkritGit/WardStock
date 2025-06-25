package com.thanes.wardstock.ui.components.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun LoadingDialog(isRemoving: Boolean) {
  if (isRemoving) {
    AlertDialog(
      properties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = true
      ),
      icon = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
          CircularProgressIndicator(
            color = Colors.BlueGrey100,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
          )
          Text(
            stringResource(R.string.is_Loading),
            color = Colors.BlueGrey100,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = ibmpiexsansthailooped
          )
        }
      },
      text = {},
      onDismissRequest = {},
      confirmButton = {},
      dismissButton = {},
      containerColor = Color.Transparent
    )
  }
}