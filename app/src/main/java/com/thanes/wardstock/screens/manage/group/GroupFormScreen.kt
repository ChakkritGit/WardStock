package com.thanes.wardstock.screens.manage.group

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import com.thanes.wardstock.data.models.DrugModel
import com.thanes.wardstock.data.models.InventoryItem
import com.thanes.wardstock.data.models.InventoryModel
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

data class GroupFormState(
  val groupId: String = "",
  val drugId: String? = null,
  val groupMin: Int = 0,
  val groupMax: Int = 0,
  val inventories: List<InventoryItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFormScreen(
  context: Context,
  innerPadding: PaddingValues,
  isLoading: Boolean,
  navController: NavHostController?,
  inventorySharedViewModel: InventoryViewModel,
  groupSharedViewModel: GroupViewModel,
  refillSharedViewModel: RefillViewModel,
  drugSharedViewModel: DrugViewModel,
  initialData: GroupFormState? = null,
  onSubmit: suspend (GroupFormState) -> Boolean
) {
  var errorMessage by remember { mutableStateOf("") }
  var isRemoving by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)

  var drugId by remember { mutableStateOf(initialData?.drugId ?: "") }
  var drugIdFromEdit by remember { mutableStateOf(initialData?.drugId) }
  val inventories = remember { mutableStateListOf<InventoryItem>() }
  val inventoriesEdit = remember { mutableStateListOf<InventoryItem>() }
  var groupMin by remember { mutableIntStateOf(initialData?.groupMin ?: 0) }
  var groupMax by remember { mutableIntStateOf(initialData?.groupMax ?: 0) }
  val scope = rememberCoroutineScope()

  fun removeGroup() {
    if (isRemoving) return

    scope.launch {
      try {
        isRemoving = true
        val response = ApiRepository.removeGroup(groupId = initialData?.groupId ?: "")

        if (response.isSuccessful) {
          errorMessage = deleteMessage + successMessage
          groupSharedViewModel.fetchGroup()
          drugSharedViewModel.fetchDrugExits()
          inventorySharedViewModel.fetchInventoryExits()
          refillSharedViewModel.fetchRefill()
          navController?.popBackStack()
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isRemoving = false
      }
    }
  }

  fun getAvailableDrug(
    drugSharedViewModel: DrugViewModel,
    drugIdFromEdit: String? = null
  ): List<DrugModel> {
    val exitsDrug = drugSharedViewModel.drugExitsState.map { it.drugId }
    val allDrug = drugSharedViewModel.drugState

    if (drugIdFromEdit == null) {
      val availableDrug = allDrug.filter { it.id !in exitsDrug }
      return availableDrug
    } else {
      val editDrugId = exitsDrug.filter { it != drugIdFromEdit }
      val availableDrug = allDrug.filter { it.id !in editDrugId }
      return availableDrug
    }
  }

  fun getAvailableInventory(
    inventorySharedViewModel: InventoryViewModel,
    inventoriesEdit: List<InventoryItem> = emptyList()
  ): List<InventoryModel> {
    val exitsInventory = inventorySharedViewModel.inventoryExitsState.map { it.inventoryId }
    val allInventory = inventorySharedViewModel.inventoryState

    if (inventoriesEdit.isEmpty()) {
      val availableInventory = allInventory.filter { it.id !in exitsInventory }

      return availableInventory
    } else {
      val editInventoryIds = inventoriesEdit.map { it.inventoryId }
      val editInventory = exitsInventory.filter { it !in editInventoryIds }
      val availableInventory = allInventory.filter { it.id !in editInventory }
      return availableInventory
    }
  }

  LaunchedEffect(initialData) {
    drugIdFromEdit = if (initialData != null) {
      initialData.drugId ?: ""
    } else {
      null
    }
  }

  LaunchedEffect(initialData) {
    inventories.clear()
    inventoriesEdit.clear()
    inventories.addAll(initialData?.inventories ?: emptyList())
    inventoriesEdit.addAll(initialData?.inventories ?: emptyList())
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
      if (drugSharedViewModel.drugState.isNotEmpty() && drugSharedViewModel.drugExitsState.isNotEmpty()) {
        var expandedPosition by remember { mutableStateOf(false) }
        val availablePositions = getAvailableDrug(drugSharedViewModel, drugIdFromEdit)

        val selectedPosition = availablePositions.find { it.id == drugId }?.drugName ?: ""

        ExposedDropdownMenuBox(
          expanded = expandedPosition,
          onExpandedChange = { expandedPosition = !expandedPosition }) {
          OutlinedTextField(
            value = selectedPosition,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.medicines)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPosition) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.medication_24px),
                contentDescription = "medication_24px",
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
            if (availablePositions.isNotEmpty()) {
              availablePositions.forEach { positionOption ->
                DropdownMenuItem(text = { Text(positionOption.drugName) }, onClick = {
                  drugId = positionOption.id
                  expandedPosition = false
                })
              }
            } else {
              Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 14.dp)
              ) {
                Text(
                  stringResource(R.string.no_item),
                  fontSize = 18.sp
                )
              }
            }
          }
        }
      }

      if (inventorySharedViewModel.inventoryState.isNotEmpty() && inventorySharedViewModel.inventoryExitsState.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }
        val availablePositions = getAvailableInventory(inventorySharedViewModel, inventoriesEdit)
        val selectedIds = inventories.map { it.inventoryId }

        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded }
        ) {
          OutlinedTextField(
            value = inventorySharedViewModel.inventoryState
              .filter { it.id in selectedIds }
              .map { it.position }
              .joinToString(", "),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.tab_inventory)) },
            trailingIcon = {
              ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.box_24px),
                contentDescription = "box_24px",
                tint = Colors.BlueGrey40,
                modifier = Modifier.size(32.dp),
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
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
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shadowElevation = 6.dp,
            modifier = Modifier.background(Colors.BlueGrey100),
            shape = RoundedCornerShape(RoundRadius.Large)
          ) {
            if (availablePositions.isNotEmpty()) {
              availablePositions.forEach { item ->
                val isSelected = inventories.find { it.inventoryId == item.id } != null
                DropdownMenuItem(
                  text = {
                    Row(
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(Colors.BlueSecondary),
                        modifier = Modifier.clip(shape = RoundedCornerShape(RoundRadius.Large))
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(item.position.toString(), fontSize = 18.sp)
                    }
                  },
                  onClick = {
                    if (isSelected) {
                      inventories.removeIf { it.inventoryId == item.id }
                    } else {
                      inventories.add(InventoryItem(inventoryId = item.id))
                    }
                  }
                )
              }
            } else {
              Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 14.dp)
              ) {
                Text(
                  stringResource(R.string.no_item),
                  fontSize = 18.sp
                )
              }
            }

          }
        }
      }

      OutlinedTextField(
        value = groupMin.toString(),
        onValueChange = {
          val value = it.toIntOrNull()
          groupMin = when {
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
        value = groupMax.toString(),
        onValueChange = {
          val value = it.toIntOrNull()
          groupMax = when {
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
    }

    Spacer(modifier = Modifier.height(40.dp))

    GradientButton(
      onClick = {
        scope.launch {
          onSubmit(
            GroupFormState(
              drugId = drugId,
              inventories = inventories,
              groupMin = groupMin,
              groupMax = groupMax
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
              removeGroup()
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
