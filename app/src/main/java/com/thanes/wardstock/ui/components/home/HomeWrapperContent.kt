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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.thanes.wardstock.App
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.RabbitOrderMessage
import com.thanes.wardstock.data.models.UserRole
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.AuthState
import com.thanes.wardstock.data.viewModel.OrderViewModel
import com.thanes.wardstock.services.rabbit.RabbitMQPendingAck
import com.thanes.wardstock.services.rabbit.RabbitMQService
import com.thanes.wardstock.ui.components.BarcodeInputField
import com.thanes.wardstock.ui.components.dispense.showAuthDialogUntilVerified
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.objectStringToGson
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeWrapperContent(
  context: Context,
  orderSharedViewModel: OrderViewModel,
  authState: AuthState
) {
  val app = context.applicationContext as App
  val applicationScope = CoroutineScope(Dispatchers.IO)
  val viewModel = orderSharedViewModel
  var pullState by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = viewModel.isLoading,
    onRefresh = {
      viewModel.fetchOrderInitial()
      pullState = true
    }
  )

  LaunchedEffect(Unit) {
    applicationScope.launch {
      val rabbitMQ = RabbitMQService.getInstance()
      if (app.isInitialized) {
        rabbitMQ.listenToQueue("vdOrder") { consumerTag, envelope, properties, body, channel ->
          val message = String(body, charset("UTF-8"))
          val parsedMessage: RabbitOrderMessage? = objectStringToGson<RabbitOrderMessage>(message)
          val userData = authState.userData

          Log.d("RabbitMQ", "Received message: $message")

          RabbitMQPendingAck.channel = channel
          RabbitMQPendingAck.envelope = envelope

          parsedMessage?.let {
            if ((it.priority == 2 || it.priority == 3) && userData?.role == UserRole.NURSE) {
              CoroutineScope(Dispatchers.Main).launch {
                val verified = showAuthDialogUntilVerified(context)

                if (!verified) {
                  withContext(Dispatchers.IO) {
                    try {
                      channel?.basicReject(envelope.deliveryTag, true)
                      Log.d("RabbitMQ", "Rejected and re-queued message: ${it.id}")
                    } catch (e: Exception) {
                      Log.e("RabbitMQ", "Failed to reject message: ${e.message}")
                    }
                  }
                  return@launch
                }
              }
            }

            CoroutineScope(Dispatchers.Main).launch {
              try {
                ApiRepository.updateOrderToPending(it.id, it.presId)
                viewModel.fetchOrderInitial()

                app.dispenseService?.let { dispenseService ->
                  val continueReturn = withContext(Dispatchers.IO) {
                    try {
                      dispenseService.sendToMachine(
                        dispenseQty = it.qty,
                        position = it.position
                      )
                    } catch (e: Exception) {
                      Log.e("Dispense", "Error during dispensing: ${e.message}")
                      false
                    }
                  }

                  if (continueReturn) {
                    ApiRepository.updateOrderToReceive(it.id, it.presId)
                  } else {
                    ApiRepository.updateOrderToError(it.id, it.presId)
                  }
                  viewModel.fetchOrderInitial()
                } ?: run {
                  Log.e("Dispense", "Dispense service is not available")
                }
              } catch (e: Exception) {
                errorMessage = parseExceptionMessage(e)
              }
            }
          }
        }
      } else {
        Log.d("RabbitMQ", "RabbitMQ is not initialized yet.")
      }
    }
  }

  LaunchedEffect(viewModel.orderState) {
    if (viewModel.orderState == null) {
      viewModel.fetchOrderInitial()
    }
  }

  LaunchedEffect(viewModel.errorMessage) {
    if (viewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, viewModel.errorMessage, Toast.LENGTH_SHORT).show()
      viewModel.errorMessage = ""
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  BarcodeInputField { scanned ->
    if (scanned.length == 1 && viewModel.orderState == null) {
      viewModel.fetchOrder(scanned)
    } else if (scanned.length > 1 && viewModel.orderState != null) {
      val channel = RabbitMQPendingAck.channel
      val envelope = RabbitMQPendingAck.envelope

      if (channel != null && envelope != null) {
        CoroutineScope(Dispatchers.IO).launch {
          try {
            val response = ApiRepository.updateOrderToComplete(scanned, viewModel.orderState!!.id)
            if (response.isSuccessful) {
              viewModel.fetchOrderInitial()
              channel.basicAck(envelope.deliveryTag, false)
              RabbitMQPendingAck.reset()
              Log.d("RabbitMQ", "Acked from BarcodeInputField")
            } else {
              val errorJson = response.errorBody()?.string()
              val message = parseErrorMessage(response.code(), errorJson)
              errorMessage = message
            }
          } catch (e: Exception) {
            errorMessage = parseExceptionMessage(e)
          }
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .clip(
        RoundedCornerShape(
          topStart = RoundRadius.BorderRadius,
          topEnd = RoundRadius.BorderRadius
        )
      )
      .background(color = Colors.BlueGrey100)
      .border(
        width = 1.dp,
        color = Color.Transparent,
        shape = RoundedCornerShape(
          topStart = RoundRadius.BorderRadius,
          topEnd = RoundRadius.BorderRadius
        )
      )
  ) {
    if (viewModel.isLoading && viewModel.orderState == null && !pullState) {
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
      if (viewModel.orderState != null) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
        ) {
          viewModel.orderState?.let { state ->
            PrescriptionHeader(state)
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .clip(
                  RoundedCornerShape(
                    topStart = RoundRadius.BorderRadius,
                    topEnd = RoundRadius.BorderRadius
                  )
                ),
              contentPadding = PaddingValues(6.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              itemsIndexed(
                items = state.order.filter { it.status != "complete" }
              ) { index, item ->
                AnimatedCardItem(index, item)
              }
            }
          }
        }

        PullRefreshIndicator(
          refreshing = viewModel.isLoading,
          state = pullRefreshState,
          modifier = Modifier.align(Alignment.TopCenter),
          backgroundColor = Colors.BlueGrey120,
          contentColor = Colors.BluePrimary,
          scale = true,
        )
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
