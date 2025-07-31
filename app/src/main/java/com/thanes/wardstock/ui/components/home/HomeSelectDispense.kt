package com.thanes.wardstock.ui.components.home

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thanes.wardstock.App
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.GroupInventoryModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.dialog.AlertDialogCustom
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.ImageUrl
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeSelectDispense(
  context: Context, groupSharedViewModel: GroupViewModel, refillSharedViewModel: RefillViewModel
) {
  val app = context.applicationContext as App
  var pullState by remember { mutableStateOf(false) }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = groupSharedViewModel.isLoading,
    onRefresh = {
      groupSharedViewModel.fetchGroup()
      pullState = true
    }
  )

  LaunchedEffect(groupSharedViewModel.groupInventoryState) {
    if (groupSharedViewModel.groupInventoryState.isEmpty()) {
      groupSharedViewModel.fetchGroup()
    }
  }

  LaunchedEffect(groupSharedViewModel.errorMessage) {
    if (groupSharedViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, groupSharedViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      groupSharedViewModel.errorMessage = ""
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .clip(
        RoundedCornerShape(
          topStart = RoundRadius.BorderRadius, topEnd = RoundRadius.BorderRadius
        )
      )
      .background(color = Colors.BlueGrey100)
      .border(
        width = 1.dp, color = Color.Transparent, shape = RoundedCornerShape(
          topStart = RoundRadius.BorderRadius, topEnd = RoundRadius.BorderRadius
        )
      )
  ) {
    when {
      groupSharedViewModel.isLoading && groupSharedViewModel.groupInventoryState.isEmpty() && !pullState -> {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CircularProgressIndicator(
            color = Colors.BluePrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            stringResource(R.string.is_Loading),
            fontSize = 20.sp,
            color = Colors.BluePrimary,
            fontWeight = FontWeight.Medium,
            fontFamily = ibmpiexsansthailooped
          )
        }
      }

      groupSharedViewModel.groupInventoryState.isEmpty() -> {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Image(
            painter = painterResource(R.drawable.empty),
            contentDescription = "ScanBanner",
            modifier = Modifier
              .width(320.dp)
              .height(320.dp),
            contentScale = ContentScale.Fit,
          )
          Text(
            stringResource(R.string.empty_data),
            fontSize = 24.sp,
            color = Colors.BluePrimary,
            fontWeight = FontWeight.Medium,
            fontFamily = ibmpiexsansthailooped
          )
        }
      }

      else -> {
        val itemsToDisplay: List<GroupInventoryModel> = groupSharedViewModel.groupInventoryState
        val horizontalScrollState = rememberScrollState()

        Box(
          modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(horizontalScrollState)
              .padding(12.dp)
          ) {
            repeat(10) { columnIndex ->
              Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                repeat(6) { rowIndex ->
                  val itemIndex = (rowIndex * 10) + columnIndex
                  if (itemIndex < itemsToDisplay.size) {
                    val item = itemsToDisplay[itemIndex]

                    AnimatedGridItem(
                      index = itemIndex,
                      item = item,
                      viewModel = groupSharedViewModel,
                      app = app, context = context, refillSharedViewModel
                    )
                  } else {
                    Spacer(
                      modifier = Modifier
                        .width(180.dp)
                        .height(200.dp)
                    )
                  }
                }
              }
              if (columnIndex < 9) {
                Spacer(modifier = Modifier.width(10.dp))
              }
            }
          }

          PullRefreshIndicator(
            refreshing = groupSharedViewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Colors.BlueGrey120,
            contentColor = Colors.BluePrimary,
            scale = true,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedGridItem(
  index: Int,
  item: GroupInventoryModel,
  viewModel: GroupViewModel,
  app: App, context: Context, refillSharedViewModel: RefillViewModel
) {
  var visible by remember { mutableStateOf(false) }
  var isDispenseServiceReady by remember { mutableStateOf(false) }
  var showBottomSheet by remember { mutableStateOf(false) }
  var openAlertDialog by remember { mutableStateOf(false) }
  val qty = remember { mutableIntStateOf(1) }
  var orderItem by remember { mutableStateOf<GroupInventoryModel?>(null) }
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  val contextLang = LocalContext.current

  var showConfirmationDialog by remember { mutableStateOf(false) }
  var confirmationDialogMessage by remember { mutableStateOf("") }
  var confirmationDeferred by remember { mutableStateOf<CompletableDeferred<Unit>?>(null) }

  val animatedScale by animateFloatAsState(
    targetValue = if (visible) 1f else 0.85f,
    animationSpec = tween(
      durationMillis = 300,
      easing = FastOutSlowInEasing
    ),
    label = "scale"
  )

  val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(
      durationMillis = 250,
      easing = LinearOutSlowInEasing
    ),
    label = "alpha"
  )

  val animatedOffsetX by animateIntAsState(
    targetValue = if (visible) 0 else 100,
    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    label = "offsetX"
  )

  LaunchedEffect(Unit) {
    delay(index * 60L)
    visible = true
  }

  LaunchedEffect(Unit) {
    while (!app.isInitialized) {
      delay(100)
    }
    isDispenseServiceReady = true
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

  LaunchedEffect(showConfirmationDialog) {
    if (showBottomSheet) {
      delay(40)
      (context as? Activity)?.let { activity ->
        HideSystemControll.manageSystemBars(activity, true)
      }
    }
  }

  val totalQty = item.inventoryList.sumOf { it.inventoryQty }

  fun dispenseOrder() {
    scope.launch {
      app.dispenseService?.let { dispenseService ->
        orderItem?.let { currentItem ->
          val availableInventories = currentItem.inventoryList
            .filter { it.inventoryQty > 0 }
            .sortedBy { it.inventoryQty }
          var remainingQtyToDispense = qty.intValue

          Log.d("DispenseDebug", "--- เริ่มกระบวนการ ---")
          Log.d("DispenseDebug", "ต้องการจ่ายทั้งหมด: $remainingQtyToDispense ชิ้น")
          Log.d(
            "DispenseDebug",
            "ช่องที่พร้อมจ่าย: ${availableInventories.map { "ช่อง ${it.inventoryPosition} (มี ${it.inventoryQty})" }}"
          )

          withContext(Dispatchers.IO) {
            var isFirstRound = true

            for ((index, inventory) in availableInventories.withIndex()) {
              Log.d("DispenseDebug", "เข้า Loop รอบที่ ${index + 1} / ${availableInventories.size}")

              if (remainingQtyToDispense <= 0) {
                Log.d("DispenseDebug", "จ่ายครบแล้ว, ออกจาก Loop")
                break
              }

              if (!isFirstRound) {
                val deferred = CompletableDeferred<Unit>()
                withContext(Dispatchers.Main) {
                  confirmationDeferred = deferred
                  confirmationDialogMessage =
                    "กรุณาหยิบยาออกจากช่องก่อนหน้า แล้วกดยืนยันเพื่อจ่ายต่อ"
                  showConfirmationDialog = true
                  Log.d("DispenseDebug", "กำลังแสดง Popup และรอการยืนยัน...")
                }
                deferred.await()
                Log.d("DispenseDebug", "ผู้ใช้กดยืนยัน, ทำงานต่อ")
              }

              val qtyToDispenseFromThisSlot = minOf(remainingQtyToDispense, inventory.inventoryQty)

              withContext(Dispatchers.Main) { openAlertDialog = true }
              Log.d(
                "DispenseDebug",
                "กำลังจ่าย $qtyToDispenseFromThisSlot ชิ้น จากช่อง ${inventory.inventoryPosition}"
              )

              val success = try {
                dispenseService.sendToMachine(
                  dispenseQty = qtyToDispenseFromThisSlot,
                  position = inventory.inventoryPosition
                )
              } catch (e: Exception) {
                Log.e("DispenseDebug", "เกิด Exception ตอนจ่ายยา: ${e.message}")
                false
              }

              withContext(Dispatchers.Main) { openAlertDialog = false }

              if (success) {
                Log.d("DispenseDebug", "จ่ายสำเร็จ!")
                remainingQtyToDispense -= qtyToDispenseFromThisSlot
                isFirstRound = false
                Log.d("DispenseDebug", "เหลือต้องจ่ายอีก: $remainingQtyToDispense")
                try {
                  val response =
                    ApiRepository.refillDrug(inventory.inventoryId, qtyToDispenseFromThisSlot)
                  if (response.isSuccessful) {
                    viewModel.fetchGroup()
                    refillSharedViewModel.fetchRefill()
                  } else {
                    val errorJson = response.errorBody()?.string()
                    val message = try {
                      JSONObject(errorJson ?: "").getString("message")
                    } catch (_: Exception) {
                      val errorJson = response.errorBody()?.string()
                      parseErrorMessage(response.code(), errorJson)
                    }
                    Log.e("DispenseDebug", "เกิดข้อผิดพลาด: $message")
                  }
                } catch (e: Exception) {
                  val errorMessage = parseExceptionMessage(e)
                  Log.e("DispenseDebug", "เกิดข้อผิดพลาด: $errorMessage")
                }
              } else {
                Log.e("DispenseDebug", "จ่ายไม่สำเร็จ, หยุด Loop")
                withContext(Dispatchers.Main) {
                  Toast.makeText(
                    context,
                    "จ่ายยาจากช่อง ${inventory.inventoryPosition} ไม่สำเร็จ",
                    Toast.LENGTH_LONG
                  ).show()
                }
                break
              }
            }

            withContext(Dispatchers.Main) {
              Log.d("DispenseDebug", "--- จบกระบวนการ ---")
              if (remainingQtyToDispense > 0) {
                Log.d("DispenseDebug", "ผลลัพธ์: จ่ายไม่ครบ")
                Toast.makeText(context, "จ่ายยาไม่ครบ ของในสต็อกไม่พอ", Toast.LENGTH_LONG).show()
              } else {
                Log.d("DispenseDebug", "ผลลัพธ์: จ่ายครบแล้ว")
                Toast.makeText(context, "จ่ายยาครบแล้ว", Toast.LENGTH_SHORT).show()
              }
//                      viewModel.fetchRefill()
              Log.d("DispenseDebug", "กำลังดึงข้อมูลใหม่...")
            }
          }

        } ?: run { Log.e("DispenseDebug", "orderItem is null") }
      } ?: run { Log.e("DispenseDebug", "dispenseService is null") }
    }
  }

  Box(
    modifier = Modifier
      .width(180.dp)
      .height(200.dp)
      .graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
        alpha = animatedAlpha
        translationX = animatedOffsetX.toFloat()
      }
      .clip(RoundedCornerShape(RoundRadius.Medium))
      .clickable(
        enabled = totalQty > 0, onClick = {
          if (totalQty > 0) {
            showBottomSheet = true
            orderItem = item
          }
        }),
  ) {
    RefillItemGrid(item)
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
          orderItem?.drugname ?: "Unknown",
          style = TextStyle(fontSize = 24.sp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.widthIn(max = 500.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
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
              val totalQty = orderItem?.inventoryList?.sumOf { it.inventoryQty }
              if (qty.intValue < (totalQty ?: 10)) {
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
            dispenseOrder()

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

  if (showConfirmationDialog) {
    AlertDialog(
      onDismissRequest = {
      },
      title = { Text("ดำเนินการต่อ") },
      text = { Text(confirmationDialogMessage) },
      confirmButton = {
        Button(
          onClick = {
            confirmationDeferred?.complete(Unit)
            showConfirmationDialog = false
          }
        ) {
          Text("ยืนยัน (หยิบยาแล้ว)")
        }
      },
      dismissButton = {
        Button(
          colors = ButtonDefaults.buttonColors(Colors.BluePrimary),
          onClick = {
            confirmationDeferred?.cancel()
            showConfirmationDialog = false
            Toast.makeText(context, "ยกเลิกกระบวนการจ่ายยา", Toast.LENGTH_SHORT).show()
//                  viewModel.fetchRefill()
          }
        ) {
          Text("ยกเลิกทั้งหมด")
        }
      }
    )
  }

  if (openAlertDialog) {
    AlertDialogCustom(
      dialogTitle = contextLang.getString(R.string.dispensing),
      dialogText = contextLang.getString(R.string.dispensing_please_wait),
      icon = R.drawable.reading
    )
  }
}

@Composable
fun RefillItemGrid(item: GroupInventoryModel) {
  val totalQty = item.inventoryList.sumOf { it.inventoryQty }
  val stockQty = totalQty
  val stockMin = item.groupmin
  val (bg, border, text) = when {
    stockQty == 0 -> Triple(Color(0xFFFFCDD2), Color(0xFFD32F2F), Color(0xFFD32F2F))
    stockQty <= stockMin -> Triple(Color(0xFFFFF9C4), Color(0xFFFFA000), Color(0xFFFFA000))
    else -> Triple(Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color.Black)
  }

  Card(
    shape = RoundedCornerShape(RoundRadius.Medium),
    colors = CardDefaults.cardColors(Colors.BlueGrey120),
    border = BorderStroke(1.dp, Colors.BlueGrey80.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(2.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          if (item.drugimage.isNotBlank()) {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(ImageUrl + item.drugimage)
                .crossfade(true)
                .build(),
              contentDescription = item.drugname,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(RoundRadius.Small)),
            )
          } else {
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(Colors.BlueGrey40),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = painterResource(R.drawable.medication_24px),
                contentDescription = "medication_24px",
                tint = Colors.BlueGrey80,
                modifier = Modifier
                  .size(64.dp)
              )
            }
          }
        }

        Text(
          text = item.drugname,
          fontSize = 14.sp,
          color = Colors.black,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.widthIn(max = 170.dp)
        )

        Text(
          text = "${stringResource(R.string.remaining_stock)} $stockQty",
          fontSize = 12.sp,
          color = text,
          modifier = Modifier
            .border(1.dp, border, RoundedCornerShape(RoundRadius.Small))
            .background(bg, RoundedCornerShape(RoundRadius.Small))
            .padding(horizontal = 6.dp, vertical = 1.dp)
        )

        Text(
          text = "Min: ${item.groupmin} | Max: ${item.groupmax} / ${
            item.inventoryList.map { it.inventoryPosition }
              .joinToString(separator = ", ")
          }",
          fontSize = 12.sp,
          color = Colors.BlueGrey40
        )

//        ExpireText(item.drugExpire, 12.sp)
      }

      if (item.drugpriority > 0) {
        val drugLabel =
          when (item.drugpriority) {
            1 -> stringResource((R.string.normal_drug))
            2 -> stringResource(
              R.string.Had_drug
            )

            else -> stringResource(R.string.Narcotic_drug)
          }
        val drugColor =
          when (item.drugpriority) {
            1 -> Color(0xFF78909C)
            2 -> Color(
              0xFFFF9800
            )

            else -> Color(0xFFE91E63)
          }

        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .background(drugColor, RoundedCornerShape(RoundRadius.Small))
            .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
          Text(
            text = drugLabel,
            fontSize = 12.sp,
            color = Color.White
          )
        }
      }

      if (stockQty == 0) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(RoundRadius.Small))
            .background(Colors.BlueGrey120.copy(alpha = 0.9f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = stringResource(R.string.drug_empty),
            fontSize = 20.sp,
            color = Colors.blackGrey.copy(0.8f),
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}