package com.thanes.wardstock.screens.refill

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.thanes.wardstock.data.viewModel.RefillSharedViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors

@Composable
fun RefillDrug(
  navController: NavHostController,
  context: Context,
  viewModel: RefillSharedViewModel
) {
  var canClick by remember { mutableStateOf(true) }
  val item = viewModel.selectedDrug

  Scaffold(
    topBar = {
      AppBar(
        title = item?.drugName ?: "ไม่พบชื่อยา",
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
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
      ) {
        item?.let {
          Text("ชื่อยา: ${it.drugName}")
          Text("จำนวนคงเหลือ: ${it.inventoryQty}")
        } ?: run {
          Text("ไม่พบข้อมูลยา")
        }
      }
    }
  }
}