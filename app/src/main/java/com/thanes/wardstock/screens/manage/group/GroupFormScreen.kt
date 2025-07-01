package com.thanes.wardstock.screens.manage.group

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.DrugModel
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.data.viewModel.GroupViewModel
import com.thanes.wardstock.data.viewModel.InventoryViewModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius

data class GroupFormState(
  val drugId: String = "",
  val groupMin: Int = 0,
  val groupMax: Int = 0,
  val inventories: List<InventoryList> = emptyList()
)

data class InventoryList(
  val inventoryId: String = ""
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
  onSubmit: suspend (InventoryList) -> Boolean
) {
  var errorMessage by remember { mutableStateOf("") }
  var isRemoving by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)
  val somethingWrongMessage = stringResource(R.string.something_wrong)

  var drugId by remember { mutableStateOf(initialData?.drugId ?: "") }

  fun removeGroup() {

  }

  fun getAvailableDrug(
    drugSharedViewModel: DrugViewModel
  ): List<DrugModel> {
    val exitsDrug = drugSharedViewModel.drugExitsState.map { it.drugId }
    val allDrug = drugSharedViewModel.drugState

    val availableDrug = allDrug.filter { it.id !in exitsDrug }

    return availableDrug
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
        val availablePositions = getAvailableDrug(drugSharedViewModel)

        val selectedPosition = availablePositions.find { it.id == drugId }?.drugName ?: ""

        ExposedDropdownMenuBox(
          expanded = expandedPosition,
          onExpandedChange = { expandedPosition = !expandedPosition }) {
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
                Text("No item", fontSize = 18.sp, fontWeight = FontWeight.Medium)
              }
            }
          }
        }
      }
    }
  }
}