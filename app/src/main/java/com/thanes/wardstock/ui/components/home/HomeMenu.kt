package com.thanes.wardstock.ui.components.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun HomeMenu(navController: NavHostController, context: Context) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 14.dp)
      .horizontalScroll(rememberScrollState())
  ) {
    Button(
      onClick = {
        navController.navigate(Routes.Refill.route)
      },
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.add_box_24px),
          contentDescription = "add_box_24px",
          tint = Colors.BlueGrey100,
          modifier = Modifier
            .size(48.dp)
        )
        Text(
          stringResource(R.string.refill_medicine),
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    }
    Spacer(modifier = Modifier.width(20.dp))
    Button(
      onClick = {},
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.edit_document_24px),
          contentDescription = "edit_document_24px",
          tint = Colors.BlueGrey100,
          modifier = Modifier
            .size(48.dp)
        )
        Text(
          stringResource(R.string.drug_report),
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    }
    Spacer(modifier = Modifier.width(20.dp))
    Button(
      onClick = {},
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.manage_accounts_24px),
          contentDescription = "manage_accounts_24px",
          tint = Colors.BlueGrey100,
          modifier = Modifier
            .size(48.dp)
        )
        Text(
          stringResource(R.string.all_manage),
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    }
    Spacer(modifier = Modifier.width(20.dp))
    Button(
      onClick = { navController.navigate(Routes.Setting.route) },
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.settings_24px),
          contentDescription = "settings_24px",
          tint = Colors.BlueGrey100,
          modifier = Modifier
            .size(48.dp)
        )
        Text(
          stringResource(R.string.settings),
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    }
  }
}