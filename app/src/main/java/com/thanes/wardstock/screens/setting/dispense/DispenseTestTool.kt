package com.thanes.wardstock.screens.setting.dispense

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.App
import com.thanes.wardstock.R
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DispenseTestToolList(navController: NavHostController) {
  Box(modifier = Modifier.clickable(onClick = {
    navController.navigate(Routes.DispenseTestTool.route)
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
          shape = RoundedCornerShape(24.dp),
          color = Colors.BlueGrey80.copy(alpha = 0.7f),
          modifier = Modifier
            .size(42.dp)
        ) {
          Icon(
            painter = painterResource(R.drawable.handyman_24px),
            contentDescription = "NextOpen",
            tint = Colors.BluePrimary,
            modifier = Modifier
              .size(14.dp)
              .padding(8.dp)
          )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
          stringResource(R.string.dispense_test_tool),
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
            .clip(RoundedCornerShape(8.dp)), color = Colors.BlueGrey80.copy(alpha = 0.5f)
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DispenseTestTool(navController: NavHostController, context: Context) {
  val app = context.applicationContext as App

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.dispense_test_tool),
        onBack = { navController.popBackStack() }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Column(
        modifier = Modifier
          .padding(10.dp)
      ) {
        SlotGridWithBottomSheet(app)
      }
    }
  }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotGridWithBottomSheet(app: App) {
  val numbers = (1..60).toList()
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  var showBottomSheet by remember { mutableStateOf(false) }
  var selectedNumber by remember { mutableIntStateOf(1) }
  val qty = remember { mutableIntStateOf(1) }
  var openAlertDialog by remember { mutableStateOf(false) }
  var isDispenseServiceReady by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    while (!app.isInitialized) {
      kotlinx.coroutines.delay(100)
    }
    isDispenseServiceReady = true
  }

  Column {
    for (row in 0 until 6) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        for (col in 0 until 10) {
          val index = row * 10 + col
          if (index < numbers.size) {
            val number = numbers[index]
            Card(
              modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clickable {
                  if (isDispenseServiceReady) {
                    qty.intValue = 1
                    selectedNumber = number
                    showBottomSheet = true
                  }
                },
              colors = CardDefaults.cardColors(
                containerColor = Colors.BlueGrey80
              ),
              elevation = CardDefaults.cardElevation(3.dp)
            ) {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
              ) {
                Text(text = number.toString(), fontFamily = ibmpiexsansthailooped)
              }
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }
  }

  if (!isDispenseServiceReady) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("กำลังเชื่อมต่อระบบ...")
        Spacer(modifier = Modifier.height(10.dp))
        Text("โปรดรอสักครู่", style = TextStyle(fontSize = 14.sp))
      }
    }
  }

  if (showBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = {
        showBottomSheet = false
      },
      sheetState = sheetState
    ) {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
      ) {
        Text(
          "ช่องที่เลือก: $selectedNumber",
          style = TextStyle(fontSize = 32.sp)
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text("จำนวน", style = TextStyle(fontSize = 32.sp))
        Spacer(modifier = Modifier.height(15.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Button(onClick = {
            if (qty.intValue > 1) {
              qty.intValue = qty.intValue - 1
            }
          }) {
            Text("-", style = TextStyle(fontSize = 32.sp))
          }
          Spacer(modifier = Modifier.width(24.dp))
          Text(qty.intValue.toString(), style = TextStyle(fontSize = 42.sp))
          Spacer(modifier = Modifier.width(24.dp))
          Button(onClick = {
            if (qty.intValue < 10) {
              qty.intValue = qty.intValue + 1
            }
          }) {
            Text("+", style = TextStyle(fontSize = 32.sp))
          }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(
          modifier = Modifier.fillMaxWidth(fraction = 0.75f),
          enabled = isDispenseServiceReady,
          onClick = {
            scope.launch {
              app.dispenseService?.let { dispenseService ->
                openAlertDialog = true

                val continueReturn = withContext(Dispatchers.IO) {
                  try {
                    dispenseService.sendToMachine(
                      dispenseQty = qty.intValue,
                      position = selectedNumber
                    )
                  } catch (e: Exception) {
                    Log.e("Dispense", "Error during dispensing: ${e.message}")
                    false
                  }
                }

                Log.d("sendToMachine", "continue: $continueReturn")
                openAlertDialog = false
              } ?: run {
                Log.e("Dispense", "Dispense service is not available")
              }
            }

            scope.launch { sheetState.hide() }.invokeOnCompletion {
              if (!sheetState.isVisible) {
                showBottomSheet = false
              }
            }
          }) {
          Text(if (isDispenseServiceReady) "สั่ง" else "กำลังเชื่อมต่อ...")
        }
      }
    }
  }

  if (openAlertDialog) {
    AlertDialogExample(
      dialogTitle = "กำลังหยิบ",
      dialogText = "โปรดรอจนกว่าจะหยิบเสร็จ",
      icon = Icons.Default.Info
    )
  }
}

@Composable
fun AlertDialogExample(
  dialogTitle: String,
  dialogText: String,
  icon: ImageVector,
) {
  AlertDialog(
    icon = {
      Icon(icon, contentDescription = "Example Icon")
    },
    title = {
      Text(text = dialogTitle)
    },
    text = {
      Text(text = dialogText)
    },
    onDismissRequest = {},
    confirmButton = {},
    dismissButton = {}
  )
}
