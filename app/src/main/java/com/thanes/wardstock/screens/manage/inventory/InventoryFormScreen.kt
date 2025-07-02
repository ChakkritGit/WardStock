package com.thanes.wardstock.screens.manage.inventory

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.MachineViewModel
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.launch
import org.json.JSONObject

data class InventoryFormState(
  val id: String = "",
  val position: Int? = null,
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
  machineSharedViewModel: MachineViewModel,
  initialData: InventoryFormState? = null,
  onSubmit: suspend (InventoryFormState) -> Boolean
) {
  var errorMessage by remember { mutableStateOf("") }
  var isRemoving by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)
  val somethingWrongMessage = stringResource(R.string.something_wrong)

  var position by remember { mutableStateOf(initialData?.position) }
  var positionEdit by remember { mutableStateOf(initialData?.position) }
  var min by remember { mutableIntStateOf(initialData?.min ?: 0) }
  var max by remember { mutableIntStateOf(initialData?.max ?: 0) }
  var status by remember { mutableStateOf(initialData?.status != false) }
  var machineId by remember { mutableStateOf(initialData?.machineId ?: "") }
  var comment by remember { mutableStateOf(initialData?.comment ?: "") }
  val scope = rememberCoroutineScope()

  fun removeInventory() {
    if (isRemoving) return

    scope.launch {
      try {
        isRemoving = true
        val response = ApiRepository.removeInventory(context, inventoryId = initialData?.id ?: "")

        if (response.isSuccessful) {
          errorMessage = deleteMessage + successMessage
          inventorySharedViewModel.fetchInventory()
          navController?.popBackStack()
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
          is java.net.UnknownHostException -> "No internet connection"
          is java.net.SocketTimeoutException -> "Request timeout, please try again"
          is java.net.ConnectException -> "Unable to connect to server"
          is javax.net.ssl.SSLException -> "Secure connection failed"
          is com.google.gson.JsonSyntaxException -> "Invalid response format"
          is java.io.IOException -> "Network error occurred"
          else -> {
            Log.e("AddUser", "Unexpected error", e)
            "Unexpected error occurred: $somethingWrongMessage"
          }
        }
      } finally {
        isRemoving = false
      }
    }
  }

  fun getAvailablePositions(
    context: Context,
    inventorySharedViewModel: InventoryViewModel,
    capacity: Int = 60,
    positionFromEdit: Int? = null
  ): List<Position> {
    val labelPrefix = context.getString(R.string.drug_inventory_no)
    val usedPositions = inventorySharedViewModel.inventoryState.map { it.position }
    val allPositions = (1..capacity).map { pos ->
      Position(label = "$labelPrefix $pos", value = pos)
    }

    if (positionFromEdit == null) {
      val availablePositions = allPositions.filter { it.value !in usedPositions }
      return availablePositions
    } else {
      val editPosition = usedPositions.filter { it != positionFromEdit }
      val availablePositions = allPositions.filter { it.value !in editPosition }
      return availablePositions
    }
  }

  LaunchedEffect(initialData) {
    positionEdit = if (initialData != null) {
      initialData.position ?: 1
    } else {
      null
    }
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
      val availablePositions =
        getAvailablePositions(context, inventorySharedViewModel, 60, positionEdit)

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

      OutlinedTextField(
        value = min.toString(),
        onValueChange = {
          val value = it.toIntOrNull()
          min = when {
            value == null -> 0
            value <= 0 -> 0
            value > 60 -> 60
            else -> value
          }
        },
        label = { Text("Min") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.arrow_drop_down_24px),
            contentDescription = "arrow_drop_down_24px",
            tint = Colors.BlueGrey40,
            modifier = Modifier.size(32.dp),
          )
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next,
          keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
          onNext = {
//            focusRequesterUsername.requestFocus()
          }),
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

      OutlinedTextField(
        value = max.toString(),
        onValueChange = {
          val value = it.toIntOrNull()
          max = when {
            value == null -> 0
            value <= 0 -> 0
            value > 60 -> 60
            else -> value
          }
        },
        label = { Text("Max") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.arrow_drop_up_24px),
            contentDescription = "arrow_drop_up_24px",
            tint = Colors.BlueGrey40,
            modifier = Modifier.size(32.dp),
          )
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next,
          keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
          onNext = {
//            focusRequesterUsername.requestFocus()
          }),
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

      var expandedMachine by remember { mutableStateOf(false) }
      val machineList = machineSharedViewModel.machineState

      val selectedMachine: String = machineList.find { it.id == machineId }?.machineName ?: ""

      ExposedDropdownMenuBox(
        expanded = expandedMachine,
        onExpandedChange = { expandedMachine = !expandedMachine }
      ) {
        OutlinedTextField(
          value = selectedMachine,
          onValueChange = {},
          readOnly = true,
          label = { Text(stringResource(R.string.machine)) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMachine) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.precision_manufacturing_24px),
              contentDescription = "precision_manufacturing_24px",
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
          expanded = expandedMachine,
          onDismissRequest = { expandedMachine = false },
          shadowElevation = 6.dp,
          modifier = Modifier.background(Colors.BlueGrey100),
          shape = RoundedCornerShape(RoundRadius.Large)
        ) {
          machineList.forEach { machine ->
            DropdownMenuItem(
              text = { Text(machine.machineName) },
              onClick = {
                machineId = machine.id
                expandedMachine = false
              }
            )
          }
        }
      }

      if (initialData != null) {
        var expandedActive by remember { mutableStateOf(false) }
        val activeOptions = listOf(
          true to R.string.active_true,
          false to R.string.active_false
        )

        val selectedActiveLabel = stringResource(
          id = if (status) R.string.active_true else R.string.active_false
        )

        ExposedDropdownMenuBox(
          expanded = expandedActive,
          onExpandedChange = { expandedActive = !expandedActive }
        ) {
          OutlinedTextField(
            value = selectedActiveLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.active_status)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActive) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.mode_standby_24px),
                contentDescription = "active icon",
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
            expanded = expandedActive,
            onDismissRequest = { expandedActive = false },
            shadowElevation = 6.dp,
            modifier = Modifier.background(Colors.BlueGrey100),
            shape = RoundedCornerShape(RoundRadius.Large)
          ) {
            activeOptions.forEach { (value, stringResId) ->
              DropdownMenuItem(
                text = { Text(stringResource(id = stringResId)) },
                onClick = {
                  status = value
                  expandedActive = false
                }
              )
            }
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