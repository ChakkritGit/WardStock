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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.screens.home.AnimatedCounter
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
  val isLockedOut by fingerVienViewModel.isLockedOut
  val lockoutCountdown by fingerVienViewModel.lockoutCountdown
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
        bitmap = fingerVienViewModel.imageBitmap.value,
        isVerifying = fingerVienViewModel.isVerifying.value,
        lastLogMessage = fingerVienViewModel.logMessages.first(),
        isLockedOut = isLockedOut,
        lockoutCountdown = lockoutCountdown
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
  bitmap: Bitmap?,
  isVerifying: Boolean,
  lastLogMessage: String,
  isLockedOut: Boolean,
  lockoutCountdown: Int
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "ยืนยันตัวตนด้วยลายนิ้วมือ",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold
    )

    Box(
      modifier = Modifier
        .fillMaxWidth(0.8f)
        .aspectRatio(1f)
        .clip(RoundedCornerShape(16.dp))
        .border(
          width = 2.dp,
          color = if (isVerifying && !isLockedOut) MaterialTheme.colorScheme.primary else Color.Gray,
          shape = RoundedCornerShape(16.dp)
        )
        .background(Color.Black),
      contentAlignment = Alignment.Center
    ) {
      if (bitmap != null) {
        Image(
          bitmap = bitmap.asImageBitmap(),
          contentDescription = "ภาพสแกนเส้นเลือด",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          painter = painterResource(id = R.drawable.fingerprint_24px),
          contentDescription = "fingerprint icon",
          tint = Color.White.copy(alpha = 0.5f),
          modifier = Modifier.size(64.dp)
        )
      }

      if (isLockedOut) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              painter = painterResource(id = R.drawable.lock_24px),
              contentDescription = "Locked",
              tint = Color.Red,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "ระบบถูกล็อก",
              color = Color.Red,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Row(
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val countdownStyle = MaterialTheme.typography.displayMedium.copy(
                color = Color.Red,
                fontWeight = FontWeight.Bold
              )

              AnimatedCounter(count = lockoutCountdown / 10, style = countdownStyle)
              AnimatedCounter(count = lockoutCountdown % 10, style = countdownStyle)
            }
          }
        }
      }
    }
    Text(
      text = lastLogMessage,
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      minLines = 2,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
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
        onClick = { viewModel.enroll(userId, "Test User") },
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
        onClick = {
          if (isVerifying) {
            viewModel.toggleVerify()
          }
        },
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