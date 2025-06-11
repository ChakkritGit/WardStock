package com.thanes.wardstock.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun HomeWrapperContent() {
  var isDispensing by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
      .background(color = Colors.BlueGrey100)
      .border(
        width = 1.dp,
        color = Color.Transparent,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
      )
  ) {
    if (isDispensing) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 12.dp, end = 12.dp, start = 12.dp)
          .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
          .verticalScroll(rememberScrollState())
      ) {
        repeat(30) {
          Text(
            "กำลังจัด",
            fontSize = 24.sp,
            color = Colors.BluePrimary,
            fontFamily = ibmpiexsansthailooped,
            modifier = Modifier.padding(vertical = 4.dp)
          )
        }
      }
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Image(
            painter = painterResource(R.drawable.barcode_banner),
            contentDescription = "ScanBanner",
            modifier = Modifier
              .width(320.dp)
              .height(320.dp),
            contentScale = ContentScale.Fit,
          )
          Text(
            stringResource(R.string.scan_to_dispense),
            fontSize = 24.sp,
            color = Colors.BluePrimary,
            fontWeight = FontWeight.Medium,
            fontFamily = ibmpiexsansthailooped
          )
        }
      }
    }
  }
}