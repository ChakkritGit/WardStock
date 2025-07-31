package com.thanes.wardstock.screens.setting.dispense

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thanes.wardstock.App
import com.thanes.wardstock.R
import com.thanes.wardstock.navigation.LiftTabs
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.components.dialog.AlertDialogCustom
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
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.Top
        ) {
          CardLift(app)
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Racks(app)
              Door(app)

            }
            Spring(app)
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Colors.BlueGrey80
          )
          Text(
            stringResource(R.string.dispense_test_tool_position),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Colors.BlueGrey40
          )
          HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Colors.BlueGrey80
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        SlotGridWithBottomSheet(app, context)
      }
    }
  }
}

@Composable
fun Racks(app: App) {
  val scope = rememberCoroutineScope()

  fun sendCommand(command: String) {
    if (command.isEmpty()) return

    scope.launch {
      app.dispenseService?.let { dispenseService ->

        val continueReturn = withContext(Dispatchers.IO) {
          try {
            dispenseService.sendTestModuleStty2(command)
          } catch (e: Exception) {
            Log.e("Dispense", "Error during dispensing: ${e.message}")
            false
          }
        }
        Log.d("Dispense", "continue: $continueReturn")
      } ?: run {
        Log.e("Dispense", "Dispense service is not available")
      }
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.Start,
    modifier = Modifier
      .fillMaxWidth(.5f)
      .border(
        shape = RoundedCornerShape(RoundRadius.Large), width = 1.dp, color = Colors.BlueGrey80
      )
      .clip(RoundedCornerShape(RoundRadius.Large))
      .background(Colors.BlueGrey120)
      .padding(12.dp)
  ) {
    Text(
      stringResource(R.string.test_rack),
      fontSize = 20.sp,
      fontWeight = FontWeight.Medium,
      color = Colors.BlueGrey40,
      modifier = Modifier.padding(start = 10.dp, top = 4.dp, end = 0.dp, bottom = 2.dp)
    )
    GradientButton(
      onClick = { sendCommand("# 1 1 3 1 6") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        stringResource(R.string.Lock_rack),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 3 0 5") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      gradient = Brush.verticalGradient(
        colors = listOf(
          Colors.BlueGrey80,
          Colors.BlueGrey80
        )
      )
    ) {
      Text(
        stringResource(R.string.unlock_rack),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BluePrimary
      )
    }
  }
}

@Composable
fun Door(app: App) {
  val scope = rememberCoroutineScope()

  fun sendCommand(command: String) {
    if (command.isEmpty()) return

    scope.launch {
      app.dispenseService?.let { dispenseService ->

        val continueReturn = withContext(Dispatchers.IO) {
          try {
            dispenseService.sendTestModuleStty2(command)
          } catch (e: Exception) {
            Log.e("Dispense", "Error during dispensing: ${e.message}")
            false
          }
        }
        Log.d("Dispense", "continue: $continueReturn")
      } ?: run {
        Log.e("Dispense", "Dispense service is not available")
      }
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.Start,
    modifier = Modifier
      .fillMaxWidth()
      .border(
        shape = RoundedCornerShape(RoundRadius.Large), width = 1.dp, color = Colors.BlueGrey80
      )
      .clip(RoundedCornerShape(RoundRadius.Large))
      .background(Colors.BlueGrey120)
      .padding(12.dp)
  ) {
    Text(
      stringResource(R.string.test_door),
      fontSize = 20.sp,
      fontWeight = FontWeight.Medium,
      color = Colors.BlueGrey40,
      modifier = Modifier.padding(start = 10.dp, top = 4.dp, end = 0.dp, bottom = 2.dp)
    )
    GradientButton(
      onClick = { sendCommand("# 1 1 5 10 17") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        stringResource(R.string.door_open),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 6 10 18") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      gradient = Brush.verticalGradient(
        colors = listOf(
          Colors.BlueGrey80,
          Colors.BlueGrey80
        )
      )
    ) {
      Text(
        stringResource(R.string.door_close),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BluePrimary
      )
    }
  }
}

@Composable
fun Spring(app: App) {
  val scope = rememberCoroutineScope()
  val numbers = (1..60).toList()

  fun sendCommand(position: Int) {
    scope.launch {
      app.dispenseService?.let { dispenseService ->

        val continueReturn = withContext(Dispatchers.IO) {
          try {
            dispenseService.sendTestModuleStty1(position)
          } catch (e: Exception) {
            Log.e("Dispense", "Error during dispensing: ${e.message}")
            false
          }
        }
        Log.d("Dispense", "continue: $continueReturn")
      } ?: run {
        Log.e("Dispense", "Dispense service is not available")
      }
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.Start,
    modifier = Modifier
      .fillMaxWidth()
      .border(
        shape = RoundedCornerShape(RoundRadius.Large), width = 1.dp, color = Colors.BlueGrey80
      )
      .clip(RoundedCornerShape(RoundRadius.Large))
      .background(Colors.BlueGrey120)
      .padding(12.dp)
  ) {
    Text(
      stringResource(R.string.test_spring),
      fontSize = 20.sp,
      fontWeight = FontWeight.Medium,
      color = Colors.BlueGrey40,
      modifier = Modifier.padding(start = 10.dp, top = 4.dp, end = 0.dp, bottom = 2.dp)
    )
    LazyVerticalGrid(
      columns = GridCells.Fixed(10),
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      items(numbers) { number ->
        Card(
          modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(RoundRadius.Medium))
            .clickable { sendCommand(number) },
          colors = CardDefaults.cardColors(
            containerColor = Colors.BlueGrey80
          ),
          elevation = CardDefaults.cardElevation(1.2.dp)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardLift(app: App) {
  val navController = rememberNavController()
  val startDestination = LiftTabs.Static
  var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.Start,
    modifier = Modifier
      .fillMaxWidth(.4f)
      .border(
        shape = RoundedCornerShape(RoundRadius.Large), width = 1.dp, color = Colors.BlueGrey80
      )
      .clip(RoundedCornerShape(RoundRadius.Large))
      .background(Colors.BlueGrey120)
      .padding(vertical = 8.dp)
      .animateContentSize(
        animationSpec = tween(
          durationMillis = 300, easing = FastOutSlowInEasing
        )
      )
  ) {
    Text(
      stringResource(R.string.test_lift),
      fontSize = 20.sp,
      fontWeight = FontWeight.Medium,
      color = Colors.BlueGrey40,
      modifier = Modifier.padding(start = 18.dp, top = 12.dp, end = 0.dp, bottom = 2.dp)
    )
    PrimaryTabRow(
      selectedTabIndex = selectedDestination,
      containerColor = Colors.BlueGrey120,
      divider = {
        HorizontalDivider(color = Colors.BlueGrey80)
      }
    ) {
      LiftTabs.Companion.entries.forEachIndexed { index, destination ->
        Tab(
          selected = selectedDestination == index,
          onClick = {
            navController.navigate(route = destination.route)
            selectedDestination = index
          },
          text = {
            Text(
              text = stringResource(destination.labelRes),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              fontSize = 18.sp,
              fontWeight = if (selectedDestination == index) FontWeight.Medium else FontWeight.Normal,
              color = if (selectedDestination == index) Colors.BluePrimary else Colors.BlueGrey40,
              fontFamily = ibmpiexsansthailooped
            )
          }
        )
      }
    }

    NavHost(
      navController = navController,
      startDestination = startDestination.route,
      enterTransition = { EnterTransition.None },
      exitTransition = { ExitTransition.None },
      popEnterTransition = { EnterTransition.None },
      popExitTransition = { ExitTransition.None },
      modifier = Modifier.padding(12.dp)
    ) {
      composable(LiftTabs.Static.route) {
        LiftStatic(app)
      }
      composable(LiftTabs.Dynamic.route) {
        LiftPosition(app)
      }
    }
  }
}

@Composable
fun LiftStatic(app: App) {
  val scope = rememberCoroutineScope()

  fun sendCommand(command: String) {
    if (command.isEmpty()) return

    scope.launch {
      app.dispenseService?.let { dispenseService ->

        val continueReturn = withContext(Dispatchers.IO) {
          try {
            dispenseService.sendTestModuleStty2(command)
          } catch (e: Exception) {
            Log.e("Dispense", "Error during dispensing: ${e.message}")
            false
          }
        }
        Log.d("Dispense", "continue: $continueReturn")
      } ?: run {
        Log.e("Dispense", "Dispense service is not available")
      }
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    GradientButton(
      onClick = { sendCommand("# 1 1 1 1400 1403") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        "${stringResource(R.string.floor)} 6",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 1210 1213") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        "${stringResource(R.string.floor)} 5",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 1010 1013") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        "${stringResource(R.string.floor)} 4",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 790 793") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        "${stringResource(R.string.floor)} 3",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 580 583") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        "${stringResource(R.string.floor)} 2",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 360 363") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        "${stringResource(R.string.floor)} 1",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 -1 2") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      gradient = Brush.verticalGradient(
        colors = listOf(
          Colors.BlueGrey80,
          Colors.BlueGrey80
        )
      )
    ) {
      Text(
        stringResource(R.string.return_home),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BluePrimary
      )
    }
  }
}

@Composable
fun LiftPosition(app: App) {
  val scope = rememberCoroutineScope()
  var liftPosition by remember { mutableStateOf("") }

  fun sendCommand(command: String) {
    if (command.isEmpty()) return

    scope.launch {
      app.dispenseService?.let { dispenseService ->

        val continueReturn = withContext(Dispatchers.IO) {
          try {
            dispenseService.sendTestModuleStty2(command)
          } catch (e: Exception) {
            Log.e("Dispense", "Error during dispensing: ${e.message}")
            false
          }
        }
        Log.d("Dispense", "continue: $continueReturn")
      } ?: run {
        Log.e("Dispense", "Dispense service is not available")
      }
    }
  }

  Column(
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    OutlinedTextField(
      value = liftPosition,
      onValueChange = { newValue ->
        if (newValue.isEmpty()) {
          liftPosition = ""
          return@OutlinedTextField
        }

        if (newValue == "-") {
          liftPosition = "-"
          return@OutlinedTextField
        }

        val filteredText = newValue.filterIndexed { index, char ->
          char.isDigit() || (index == 0 && char == '-')
        }

        val number = filteredText.toIntOrNull()

        liftPosition = number?.coerceIn(-1, 1400)?.toString() ?: ""
      },
      label = { Text(stringResource(R.string.location_value)) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(RoundRadius.Medium),
      textStyle = TextStyle(fontSize = 20.sp),
      singleLine = true,
      maxLines = 1,
      keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Number
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          if (liftPosition.isEmpty()) return@KeyboardActions

          sendCommand("# 1 1 1 $liftPosition ${liftPosition + 3}")
        }),
      colors = TextFieldDefaults.colors(
        focusedTextColor = Colors.BlueSecondary,
        focusedIndicatorColor = Colors.BlueSecondary,
        unfocusedIndicatorColor = Colors.BlueSecondary.copy(alpha = 0.3f),
        focusedLabelColor = Colors.BlueSecondary,
        unfocusedLabelColor = Colors.BlueGrey40,
        cursorColor = Colors.BlueSecondary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedLeadingIconColor = Colors.BlueSecondary
      )
    )
    GradientButton(
      onClick = {
        if (liftPosition.isEmpty()) return@GradientButton

        sendCommand("# 1 1 1 $liftPosition ${liftPosition + 3}")
      },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    ) {
      Text(
        stringResource(R.string.sent),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BlueGrey100
      )
    }
    GradientButton(
      onClick = { sendCommand("# 1 1 1 -1 2") },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
      gradient = Brush.verticalGradient(
        colors = listOf(
          Colors.BlueGrey80,
          Colors.BlueGrey80
        )
      )
    ) {
      Text(
        stringResource(R.string.return_home),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Colors.BluePrimary
      )
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

                Log.d("Dispense", "continue: $continueReturn")
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
    AlertDialogCustom(
      dialogTitle = contextLang.getString(R.string.dispensing),
      dialogText = contextLang.getString(R.string.dispensing_please_wait),
      icon = R.drawable.reading
    )
  }
}
