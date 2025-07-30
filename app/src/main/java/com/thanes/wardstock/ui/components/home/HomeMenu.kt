package com.thanes.wardstock.ui.components.home

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.AuthState
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun HomeMenu(
  navController: NavHostController,
  context: Context,
  authState: AuthState,
  orderSharedViewModel: OrderViewModel,
  toggleDispense: MutableState<Boolean>
) {
  val isOrderActive = orderSharedViewModel.orderState != null
  var alertMessage by remember { mutableStateOf("") }
  val waitForDispenseMessage = stringResource(R.string.wait_for_dispensing)


  LaunchedEffect(alertMessage) {
    if (alertMessage.isNotEmpty()) {
      Toast.makeText(context, alertMessage, Toast.LENGTH_SHORT).show()
      alertMessage = ""
    }
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp, end = 8.dp, start = 8.dp, bottom = 14.dp)
      .horizontalScroll(rememberScrollState())
      .alpha(if (isOrderActive) 0.5f else 1f)
  ) {
    Button(
      onClick = {
        toggleDispense.value = !toggleDispense.value
      },
      colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(if (!toggleDispense.value) R.drawable.prescriptions_24px else R.drawable.grid_view_24px),
          contentDescription = "dispense_icon",
          tint = Colors.BlueGrey100,
          modifier = Modifier
            .size(48.dp)
        )
        Text(
          stringResource(if (!toggleDispense.value) R.string.prescription_dispense else R.string.select_dispense),
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    }
    Spacer(modifier = Modifier.width(20.dp))
    Button(
      onClick = {
        if (!isOrderActive) {
          navController.navigate(Routes.Refill.route)
        } else {
          alertMessage = waitForDispenseMessage
        }
      },
      colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.medical_services_24px),
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
      onClick = {
        if (!isOrderActive) {
          navController.navigate(Routes.ManageReport.route)
        } else {
          alertMessage = waitForDispenseMessage
        }
      },
      colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.assignment_24px),
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
      onClick = {
        if (!isOrderActive) {
          navController.navigate(Routes.Manage.route)
        } else {
          alertMessage = waitForDispenseMessage
        }
      },
      colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          painter = painterResource(R.drawable.inventory_2_24px),
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
      onClick = {
        if (!isOrderActive) {
          navController.navigate(Routes.Setting.route)
        } else {
          alertMessage = waitForDispenseMessage
        }
      },
      colors = ButtonDefaults.buttonColors(Color.Transparent)
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