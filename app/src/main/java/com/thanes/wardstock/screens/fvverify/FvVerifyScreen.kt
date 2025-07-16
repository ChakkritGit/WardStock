package com.thanes.wardstock.screens.fvverify

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun FvVerifyScreen(navController: NavHostController, fingerVienViewModel: FingerVeinViewModel) {
  var canClick by remember { mutableStateOf(true) }
  var userId by remember { mutableStateOf("") }
  val verifiedUid by fingerVienViewModel.verifiedUid
  var showLogPanel by remember { mutableStateOf(false) }

  LaunchedEffect(verifiedUid) {
    if (verifiedUid.isNotEmpty()) {
      userId = verifiedUid
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.finger_print),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontFamily = ibmpiexsansthailooped,
            modifier = Modifier.width(500.dp)
          )
        },
        actions = {
          IconButton(onClick = { showLogPanel = !showLogPanel }) {
            Icon(
              painter = painterResource(R.drawable.info_24px),
              contentDescription = "info_24px",
              modifier = Modifier.size(24.dp),
              tint = Colors.BluePrimary
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              if (canClick) {
                canClick = false
                navController.popBackStack()
              }
            }, modifier = Modifier
              .size(54.dp)
              .padding(4.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.chevron_left_24px),
              contentDescription = "chevron_left_24px",
              tint = Colors.BluePrimary,
              modifier = Modifier.fillMaxSize()
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Colors.BlueGrey100,
          titleContentColor = Colors.BluePrimary,
        ),
      )
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp)
        .animateContentSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      MainDisplay(
        modifier = Modifier.weight(1f),
        bitmap = fingerVienViewModel.imageBitmap.value,
        isEnrolling = fingerVienViewModel.isEnrolling.value,
        isVerifying = fingerVienViewModel.isVerifying.value,
        lastLogMessage = fingerVienViewModel.logMessages.firstOrNull() ?: "..."
      )

      MinimalControlPanel(
        userId = userId, onUserIdChange = { userId = it }, viewModel = fingerVienViewModel
      )

      AnimatedVisibility(visible = showLogPanel) {
        LogPanel(
          logs = fingerVienViewModel.logMessages,
          modifier = Modifier
            .height(300.dp)
            .padding(top = 16.dp)
        )
      }
    }
  }
}

@Composable
fun MainDisplay(
  modifier: Modifier = Modifier,
  bitmap: Bitmap?,
  isEnrolling: Boolean,
  isVerifying: Boolean,
  lastLogMessage: String
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .aspectRatio(1f)
        .clip(RoundedCornerShape(RoundRadius.Large))
        .border(
          width = 2.dp, color = when {
            isEnrolling -> Colors.BluePrimary
            isVerifying -> Colors.BlueTertiary
            else -> Colors.BlueSecondary
          }, shape = RoundedCornerShape(RoundRadius.Large)
        )
        .background(Colors.BlueGrey80.copy(alpha = 0.5f)), contentAlignment = Alignment.Center
    ) {
      if (bitmap != null) {
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = "ภาพสแกนเส้นเลือด",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Surface(
            modifier = Modifier
              .clip(shape = CircleShape),
            color = Colors.BlueGrey80.copy(alpha = 0.5f)
          ) {
            Icon(
              painter = painterResource(R.drawable.fingerprint_24px),
              contentDescription = "fingerprint_24px",
              modifier = Modifier
                .size(48.dp)
                .padding(6.dp),
              tint = Colors.black.copy(alpha = 0.8f)
            )
          }
          Text(
            stringResource(R.string.place_your_finger_on_the_scanner),
            color = Colors.black.copy(alpha = 0.8f),
            fontSize = 18.sp,
            fontFamily = ibmpiexsansthailooped
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (lastLogMessage.isNotEmpty()) {
      Text(
        text = lastLogMessage,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }
  }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MinimalControlPanel(
  userId: String, onUserIdChange: (String) -> Unit, viewModel: FingerVeinViewModel
) {
  val isEnrolling by viewModel.isEnrolling
  val isVerifying by viewModel.isVerifying

  Column(
    horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
  ) {
    OutlinedTextField(
      value = userId,
      onValueChange = onUserIdChange,
      label = { Text("รหัสผู้ใช้ (UID)") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(RoundRadius.Medium),
      singleLine = true,
      trailingIcon = {
        if (userId.isNotEmpty()) {
          TextButton(onClick = { viewModel.deleteUser(userId) }) {
            Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                painter = painterResource(R.drawable.person_cancel_24px),
                contentDescription = "person_cancel_24px",
                tint = Colors.alert
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("ลบผู้ใช้", color = Colors.alert)
            }
          }
        }
      })

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
      Button(
        onClick = { viewModel.enroll(userId) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(RoundRadius.Medium),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isEnrolling) Colors.alertBackground else Colors.BlueTertiary.copy(
            alpha = 0.5f
          ), contentColor = if (isEnrolling) Colors.alert else Colors.BluePrimary
        )
      ) {
        Text(if (isEnrolling) "หยุด" else "ลงทะเบียน", fontFamily = ibmpiexsansthailooped)
      }

      Button(
        onClick = { viewModel.toggleVerify() },
        enabled = !isEnrolling,
        shape = RoundedCornerShape(RoundRadius.Medium),
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isVerifying) Colors.alertBackground else Colors.BluePrimary,
          contentColor = if (isVerifying) Colors.alert else Colors.white,
        )
      ) {
        Text(if (isVerifying) "หยุด" else "ยืนยันตัวตน", fontFamily = ibmpiexsansthailooped)
      }
    }
  }
}

@Composable
fun LogPanel(logs: List<String>, modifier: Modifier = Modifier) {
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      coroutineScope.launch {
        listState.animateScrollToItem(0)
      }
    }
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(RoundRadius.Medium),
    colors = CardDefaults.cardColors(Colors.BlueGrey80.copy(alpha = 0.5f))
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
    ) {
      Text(
        "Log ข้อความ", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp)
      )
      HorizontalDivider(
        Modifier.padding(bottom = 4.dp), DividerDefaults.Thickness, DividerDefaults.color
      )

      LazyColumn(
        state = listState, modifier = Modifier.fillMaxSize()
      ) {
        items(logs) { log ->
          Text(text = log, style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(4.dp))
        }
      }
    }
  }
}