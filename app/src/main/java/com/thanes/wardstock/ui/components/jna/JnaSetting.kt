package com.thanes.wardstock.ui.components.jna

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius

@Composable
fun JnaSetting(navController: NavHostController) {
  Box(modifier = Modifier.clickable(onClick = {
    navController.navigate(Routes.FingerPrintVein.route)
  })) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(RoundRadius.Large),
          color = Colors.BlueGrey80.copy(alpha = 0.7f),
          modifier = Modifier
            .size(42.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.fingerprint_24px),
            contentDescription = "fingerprint_24px",
            tint = Colors.BluePrimary,
            modifier = Modifier
              .size(14.dp)
              .padding(8.dp)
          )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
          stringResource(R.string.finger_print),
          fontSize = 24.sp,
          fontWeight = FontWeight.Medium,
        )
      }
      Box(
        modifier = Modifier
          .wrapContentSize(Alignment.TopEnd)
          .padding(end = 16.dp)
      ) {
        Surface(
          modifier = Modifier
            .clip(RoundedCornerShape(RoundRadius.Large)),
          color = Colors.BlueGrey80.copy(alpha = 0.5f)
        ) {
          Icon(
            painter = painterResource(R.drawable.chevron_right_24px),
            contentDescription = "NextOpen",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(36.dp)
          )
        }
      }
    }
  }
}