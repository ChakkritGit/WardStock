package com.thanes.wardstock.screens.fvverify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun FvVerifyScreen(
  navController: NavHostController,
  fingerVeinSharedViewModel: FingerVeinViewModel
) {
  val context = LocalContext.current
  val verifyResult by fingerVeinSharedViewModel.verifyResult.collectAsState()

  Column(
    Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.Center
  ) {
    Button(onClick = { fingerVeinSharedViewModel.verify(context) }) {
      Text("สแกนเส้นเลือดนิ้วมือ")
    }

    Spacer(modifier = Modifier.height(24.dp))

    when (verifyResult) {
      true -> Text("✅ ยืนยันตัวตนสำเร็จ", color = Colors.BluePrimary)
      false -> Text("❌ ยืนยันตัวตนล้มเหลว", color = Colors.alert)
      null -> {}
    }
  }
}