package com.thanes.wardstock.screens.setting.dispense

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
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
import com.thanes.wardstock.ui.components.dialog.AlertDialog
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DispenseTestToolList(navController: NavHostController) {
  Box(
    modifier = Modifier
      .clickable(onClick = {
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
          shape = RoundedCornerShape(RoundRadius.Large),
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun DispenseTestTool(navController: NavHostController, context: Context) {
  val app = context.applicationContext as App
  var canClick by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.dispense_test_tool),
        onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Column(
        modifier = Modifier
          .padding(10.dp)
      ) {
        SlotGridWithBottomSheet(app, context)
      }
    }
  }
}

@SuppressLint("ContextCastToActivity")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotGridWithBottomSheet(app: App, context: Context) {
  val scope = rememberCoroutineScope()
  val numbers = (1..60).toList()
  val sheetState = rememberModalBottomSheetState()
  var showBottomSheet by remember { mutableStateOf(false) }
  var selectedNumber by remember { mutableIntStateOf(1) }
  val qty = remember { mutableIntStateOf(1) }
  var openAlertDialog by remember { mutableStateOf(false) }
  var isDispenseServiceReady by remember { mutableStateOf(false) }
  val contextLang = LocalContext.current

  LaunchedEffect(Unit) {
    while (!app.isInitialized) {
      delay(100)
    }
    isDispenseServiceReady = true
  }

  LazyVerticalGrid(
    columns = GridCells.Fixed(10),
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(numbers) { number ->
      Card(
        modifier = Modifier
          .aspectRatio(1f)
          .clip(RoundedCornerShape(RoundRadius.Large))
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
        elevation = CardDefaults.cardElevation(1.5.dp)
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

  if (!isDispenseServiceReady) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(contextLang.getString(R.string.connecting_system_dispense))
        Spacer(modifier = Modifier.height(10.dp))
        Text(contextLang.getString(R.string.please_wait), style = TextStyle(fontSize = 14.sp))
      }
    }
  }

  if (showBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = {
        showBottomSheet = false
      },
      sheetState = sheetState,
      containerColor = Colors.BlueGrey100
    ) {
      Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(10.dp)
      ) {
        Text(
          "${contextLang.getString(R.string.selected_position)} $selectedNumber",
          style = TextStyle(fontSize = 32.sp)
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(contextLang.getString(R.string.quantity), style = TextStyle(fontSize = 32.sp))
        Spacer(modifier = Modifier.height(15.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          GradientButton(
            onClick = {
              if (qty.intValue > 1) {
                qty.intValue = qty.intValue - 1
              }
            },
            shape = RoundedCornerShape(RoundRadius.Large),
            modifier = Modifier.height(58.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.remove_24px),
              contentDescription = "remove_24px",
              tint = Colors.BlueGrey100,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.width(32.dp))
          Text(qty.intValue.toString(), style = TextStyle(fontSize = 42.sp))
          Spacer(modifier = Modifier.width(32.dp))
          GradientButton(
            onClick = {
              if (qty.intValue < 10) {
                qty.intValue = qty.intValue + 1
              }
            },
            shape = RoundedCornerShape(RoundRadius.Large),
            modifier = Modifier.height(58.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.add_24px),
              contentDescription = "add_24px",
              tint = Colors.BlueGrey100,
              modifier = Modifier.size(32.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(30.dp))
        GradientButton(
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
          },
          shape = RoundedCornerShape(RoundRadius.Large),
          modifier = Modifier
            .fillMaxWidth(fraction = 0.65f)
            .height(58.dp)
        ) {
          if (isDispenseServiceReady) {
            Icon(
              painter = painterResource(R.drawable.send_24px),
              contentDescription = "send_24px",
              tint = Colors.BlueGrey100,
              modifier = Modifier.size(52.dp)
            )
          } else {
            Text(
              contextLang.getString(
                R.string.connecting_system_dispense
              ),
              color = Colors.BlueGrey100,
              fontWeight = FontWeight.Medium,
              fontFamily = ibmpiexsansthailooped,
              fontSize = 24.sp
            )
          }
        }
      }
    }
  }

  LaunchedEffect(openAlertDialog) {
    if (openAlertDialog) {
      delay(20)
      (context as? Activity)?.let { activity ->
        HideSystemControll.manageSystemBars(activity, true)
      }
    }
  }

  LaunchedEffect(showBottomSheet) {
    if (showBottomSheet) {
      delay(20)
      (context as? Activity)?.let { activity ->
        HideSystemControll.manageSystemBars(activity, true)
      }
    }
  }

  if (openAlertDialog) {
    AlertDialog(
      dialogTitle = contextLang.getString(R.string.dispensing),
      dialogText = contextLang.getString(R.string.dispensing_please_wait),
      icon = R.drawable.reading
    )
  }
}
