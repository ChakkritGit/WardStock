package com.thanes.wardstock.screens.refill

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun RefillDrug(
  navController: NavHostController, context: Context, viewModel: RefillViewModel
) {
  val scope = rememberCoroutineScope()
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var quality by remember { mutableIntStateOf(1) }
  val item = viewModel.selectedDrug
  var errorMessage by remember { mutableStateOf("") }
  val somethingWrongMessage = stringResource(R.string.something_wrong)
  val successSubmit = stringResource(R.string.successfully)

  fun handleSubmit() {
    isLoading = true

    scope.launch {
      if (item?.inventoryId == null) {
        errorMessage = somethingWrongMessage
        return@launch
      }

      try {
        val response = ApiRepository.addDrug(context, item.inventoryId.toString(), quality)

        if (response.isSuccessful) {
          errorMessage = successSubmit
        } else {
          val errorJson = response.errorBody()?.string()
          val message = try {
            JSONObject(errorJson ?: "").getString("message")
          } catch (_: Exception) {
            when (response.code()) {
              400 -> "Invalid request data"
              401 -> "Authentication required"
              403 -> "Access denied"
              404 -> "Prescription not found"
              500 -> "Server error, please try again later"
              else -> "HTTP Error ${response.code()}: ${response.message()}"
            }
          }
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = when (e) {
          is java.net.UnknownHostException -> {
            "No internet connection"
          }

          is java.net.SocketTimeoutException -> {
            "Request timeout, please try again"
          }

          is java.net.ConnectException -> {
            "Unable to connect to server"
          }

          is javax.net.ssl.SSLException -> {
            "Secure connection failed"
          }

          is com.google.gson.JsonSyntaxException -> {
            "Invalid response format"
          }

          is java.io.IOException -> {
            "Network error occurred"
          }

          else -> {
            Log.e("OrderAPI", "Unexpected error: ${e.javaClass.simpleName}", e)
            "Unexpected error occurred: $somethingWrongMessage"
          }
        }
      } finally {
        isLoading = false
        viewModel.fetchRefill()
        navController.popBackStack()
      }
    }
  }

  LaunchedEffect(item?.inventoryQty ?: 1) {
    quality = item?.inventoryQty ?: 1
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = "${item?.drugName ?: stringResource(R.string.no_drug_name)} | ${stringResource(R.string.drug_inventory_no)}: ${item?.inventoryPosition ?: "—"}",
        onBack = {
          if (canClick && !isLoading) {
            canClick = false
            navController.popBackStack()
          }
        })
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(stringResource(R.string.quantity), fontSize = 28.sp, fontWeight = FontWeight.Medium)
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          GradientButton(
            onClick = {
              if (quality > (item?.inventoryMin ?: 0)) {
                quality -= 1
              }
            }, shape = RoundedCornerShape(RoundRadius.Medium), gradient = Brush.verticalGradient(
              colors = listOf(
                Colors.BlueGrey80, Colors.BlueGrey80
              ),
            ), contentPadding = PaddingValues(8.dp), modifier = Modifier
              .width(64.dp)
              .height(64.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.remove_24px),
              contentDescription = "Remove",
              tint = Colors.BlueGrey40,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.width(48.dp))

          Text(quality.toString(), fontSize = 42.sp, fontWeight = FontWeight.Medium)

          Spacer(modifier = Modifier.width(48.dp))

          GradientButton(
            onClick = {
              if (quality < (item?.inventoryMAX ?: 15)) {
                quality += 1
              }
            }, shape = RoundedCornerShape(RoundRadius.Medium), gradient = Brush.verticalGradient(
              colors = listOf(
                Colors.BlueGrey80, Colors.BlueGrey80
              ),
            ), contentPadding = PaddingValues(8.dp), modifier = Modifier
              .width(64.dp)
              .height(64.dp)
          ) {
            Icon(
              painter = painterResource(R.drawable.add_24px),
              contentDescription = "Add",
              tint = Colors.BlueGrey40,
              modifier = Modifier.size(32.dp)
            )
          }
        }

        GradientButton(
          onClick = {
            if (isLoading) return@GradientButton
            handleSubmit()
          },
          shape = RoundedCornerShape(RoundRadius.Medium),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
          enabled = !isLoading
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
            )
          } else {
            Text(
              stringResource(R.string.add),
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
              color = Colors.BlueGrey100
            )
          }
        }
      }
    }
  }
}