package com.thanes.wardstock.ui.components.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.ui.components.BarcodeInputField
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun HomeWrapperContent(context: Context) {
  val scope = rememberCoroutineScope()
  var errorMessage by remember { mutableStateOf("") }
  var orderState by remember { mutableStateOf<OrderModel?>(null) }
  var isLoading by remember { mutableStateOf<Boolean>(false) }

  fun fetchOrder(prescriptionId: String) {
    errorMessage = ""
    isLoading = true

    scope.launch {
      try {
        val response = ApiRepository.orderWithPresId(context, prescriptionId)
        if (response.isSuccessful) {
          orderState = response.body()?.data
        } else {
          val errorJson = response.errorBody()?.string()
          val message = try {
            JSONObject(errorJson ?: "").getString("message")
          } catch (_: Exception) {
            "Something went wrong"
          }
          errorMessage = message
        }
      } catch (_: Exception) {
        errorMessage = "Something went wrong"
      } finally {
        isLoading = false
      }
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  BarcodeInputField { scanned ->
    if (scanned.length == 1 && orderState == null) {
      fetchOrder(scanned)
    } else if (scanned.length > 1 && orderState != null) {
      Log.d("Barcode", "Scanned text: $scanned")
    }
    Log.d("Barcode", "Scanned: $scanned")
  }

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
    if (isLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator(
            color = Colors.BluePrimary, strokeWidth = 3.dp, modifier = Modifier.size(36.dp)
          )
          Text(
            stringResource(R.string.is_dispensing),
            fontSize = 20.sp,
            color = Colors.BluePrimary,
            fontWeight = FontWeight.Medium,
            fontFamily = ibmpiexsansthailooped
          )
        }
      }
    } else {
      if (orderState != null) {
        Column(modifier = Modifier.fillMaxSize()) {
          orderState?.let { state ->
            PrescriptionHeader(state)
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, end = 12.dp, start = 12.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
              contentPadding = PaddingValues(6.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              itemsIndexed(
                items = state.order.filter { it.status != "complete" }
              ) { index, item ->
                CardItem(index, item)
              }
            }
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
}