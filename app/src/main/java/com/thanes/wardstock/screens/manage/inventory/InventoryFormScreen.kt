package com.thanes.wardstock.screens.manage.inventory

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.launch

data class InventoryFormState(
  val id: String = "",
  val position: Int = 1,
  val qty: Int = 0,
  val min: Int = 0,
  val max: Int = 0,
  val status: Boolean = true,
  val machineId: String = "",
  val comment: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryFormScreen(
  context: Context,
  innerPadding: PaddingValues,
  isLoading: Boolean,
  navController: NavHostController?,
  inventorySharedViewModel: InventoryViewModel,
  initialData: InventoryFormState? = null,
  onSubmit: suspend (InventoryFormState) -> Boolean
) {
  var errorMessage by remember { mutableStateOf("") }
  var isRemoving by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)
  val somethingWrongMessage = stringResource(R.string.something_wrong)

  var position by remember { mutableIntStateOf(initialData?.position ?: 1) }
  var qty by remember { mutableIntStateOf(initialData?.qty ?: 0) }
  var min by remember { mutableIntStateOf(initialData?.min ?: 0) }
  var max by remember { mutableIntStateOf(initialData?.max ?: 0) }
  var status by remember { mutableStateOf(initialData?.status != false) }
  var machineId by remember { mutableStateOf(initialData?.machineId ?: "") }
  var comment by remember { mutableStateOf(initialData?.comment ?: "") }
  val scope = rememberCoroutineScope()

  fun removeInventory() {}

  fun getAvailablePositions(
    context: Context,
    inventorySharedViewModel: InventoryViewModel,
    capacity: Int = 60
  ): List<Position> {
    val labelPrefix = context.getString(R.string.drug_inventory_no)
    val usedPositions = inventorySharedViewModel.inventoryState.map { it.position }
    val allPositions = (1..capacity).map { pos ->
      Position(label = "$labelPrefix $pos", value = pos)
    }
    val availablePositions = allPositions.filter { it.value !in usedPositions }

    return availablePositions
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  Column(
    modifier = Modifier
      .padding(innerPadding)
      .fillMaxSize()
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      var expandedPosition by remember { mutableStateOf(false) }
      val  availablePositions = getAvailablePositions(context, inventorySharedViewModel, 60)

      val selectedPosition = availablePositions.find { it.value == position }?.label ?: ""

      ExposedDropdownMenuBox(
        expanded = expandedPosition,
        onExpandedChange = { expandedPosition = !expandedPosition }
      ) {
        OutlinedTextField(
          value = selectedPosition,
          onValueChange = {},
          readOnly = true,
          label = { Text(stringResource(R.string.tab_inventory)) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPosition) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.box_24px),
              contentDescription = "box_24px",
              tint = Colors.BlueGrey40,
              modifier = Modifier.size(32.dp),
            )
          },
          shape = RoundedCornerShape(RoundRadius.Large),
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

        ExposedDropdownMenu(
          expanded = expandedPosition,
          onDismissRequest = { expandedPosition = false },
          shadowElevation = 6.dp,
          modifier = Modifier.background(Colors.BlueGrey100),
          shape = RoundedCornerShape(RoundRadius.Large)
        ) {
          availablePositions.forEach { positionOption ->
            DropdownMenuItem(
              text = { Text(positionOption.label) },
              onClick = {
                position = positionOption.value
                expandedPosition = false
              }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))

    GradientButton(
      onClick = {
        scope.launch {
          onSubmit(
            InventoryFormState(
              position = position,
              qty = qty,
              min = min,
              max = max,
              status = status,
              machineId = machineId,
              comment = comment
            )
          )
        }
      },
      shape = RoundedCornerShape(RoundRadius.Medium),
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
      enabled = !isLoading
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          color = Colors.BlueGrey100, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
        )
      } else {
        Text(
          stringResource(if (initialData == null) R.string.submit else R.string.update),
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Colors.BlueGrey100
        )
      }
    }

    if (initialData != null) {
      Spacer(modifier = Modifier.height(16.dp))

      GradientButton(
        onClick = { showDeleteDialog = true },
        shape = RoundedCornerShape(RoundRadius.Medium),
        gradient = Brush.verticalGradient(
          colors = listOf(Colors.BlueGrey100, Colors.BlueGrey100),
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
      ) {
        Text(
          stringResource(R.string.delete_user),
          color = Color(0xFFD32F2F),
          fontWeight = FontWeight.Medium,
          fontSize = 20.sp
        )
      }
    }
  }
  LoadingDialog(isRemoving = isRemoving)

  if (showDeleteDialog) {
    AlertDialog(
      properties = DialogProperties(
        dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = true
      ), icon = {
        Surface(
          modifier = Modifier.clip(CircleShape), color = Color(0xFFD32F2F).copy(alpha = 0.3f)
        ) {
          Icon(
            painter = painterResource(R.drawable.delete_24px),
            contentDescription = "delete_user",
            modifier = Modifier
              .size(56.dp)
              .padding(6.dp),
            tint = Color(0xFFD32F2F)
          )
        }
      }, text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(0.7f)
        ) {
          Text(
            text = stringResource(R.string.delete_user),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ibmpiexsansthailooped
          )
          Text(
            text = stringResource(R.string.confirm_delete_desc_drug),
            fontSize = 20.sp,
            fontFamily = ibmpiexsansthailooped
          )
        }
      }, onDismissRequest = {
        showDeleteDialog = false
      }, confirmButton = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          GradientButton(
            onClick = {
              removeInventory()
              showDeleteDialog = false
            },
            text = stringResource(R.string.delete),
            fontWeight = FontWeight.Medium,
            shape = RoundedCornerShape(RoundRadius.Medium),
            textSize = 20.sp,
            modifier = Modifier
              .fillMaxWidth(0.7f)
              .height(56.dp),
            gradient = Brush.verticalGradient(
              colors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
            )
          )

          GradientButton(
            onClick = {
              showDeleteDialog = false
            },
            shape = RoundedCornerShape(RoundRadius.Medium),
            gradient = Brush.verticalGradient(
              colors = listOf(Colors.BlueGrey80, Colors.BlueGrey80),
            ),
            modifier = Modifier
              .fillMaxWidth(0.7f)
              .height(56.dp)
          ) {
            Text(
              stringResource(R.string.cancel), color = Colors.BlueSecondary, fontSize = 20.sp
            )
          }
        }
      }, dismissButton = {}, containerColor = Colors.BlueGrey100
    )
  }
}