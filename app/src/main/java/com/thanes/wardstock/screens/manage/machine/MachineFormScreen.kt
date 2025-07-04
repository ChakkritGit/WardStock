package com.thanes.wardstock.screens.manage.machine

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
import androidx.compose.foundation.layout.offset
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
import com.thanes.wardstock.data.viewModel.MachineViewModel
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

data class MachineFormState(
  val id: String = "",
  val machineName: String = "",
  val location: String = "",
  val capacity: Int = 1,
  val status: Boolean = true,
  val comment: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineFormScreen(
  context: Context,
  innerPadding: PaddingValues,
  isLoading: Boolean,
  navController: NavHostController?,
  machineSharedViewModel: MachineViewModel?,
  initialData: MachineFormState? = null,
  onSubmit: suspend (MachineFormState) -> Boolean
) {
  var errorMessage by remember { mutableStateOf("") }
  var isRemoving by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)

  var machineName by remember { mutableStateOf(initialData?.machineName ?: "") }
  var location by remember { mutableStateOf(initialData?.location ?: "") }
  var capacity by remember { mutableIntStateOf(initialData?.capacity ?: 60) }
  var machineStatus by remember { mutableStateOf(initialData?.status != false) }
  var comment by remember { mutableStateOf(initialData?.comment ?: "") }
  val scope = rememberCoroutineScope()

  fun removeMachine() {
    if (isRemoving) return

    scope.launch {
      try {
        isRemoving = true
        val response = ApiRepository.removeMachine(machineId = initialData?.id ?: "")

        if (response.isSuccessful) {
          errorMessage = deleteMessage + successMessage
          machineSharedViewModel?.fetchMachine()
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
      OutlinedTextField(
        value = machineName,
        onValueChange = { machineName = it },
        label = { Text(stringResource(R.string.machine_name)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.precision_manufacturing_24px),
            contentDescription = "precision_manufacturing_24px",
            tint = Colors.BlueGrey40,
            modifier = Modifier.size(32.dp),
          )
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next
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
        value = location,
        onValueChange = { location = it },
        label = { Text(stringResource(R.string.machine_location)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.location_on_24px),
            contentDescription = "location_on_24px",
            tint = Colors.BlueGrey40,
            modifier = Modifier.size(32.dp),
          )
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next
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
        value = capacity.toString(),
        onValueChange = {
          val value = it.toIntOrNull()
          capacity = when {
            value == null -> 0
            value <= 0 -> 0
            value > 180 -> 180
            else -> value
          }
        },
        label = { Text(stringResource(R.string.machine_capacity)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.shelf_position_24px),
            contentDescription = "shelf_position_24px",
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

      var expandedActive by remember { mutableStateOf(false) }
      val activeOptions = listOf(
        true to R.string.active_true,
        false to R.string.active_false
      )

      val selectedActiveLabel = stringResource(
        id = if (machineStatus) R.string.active_true else R.string.active_false
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
                machineStatus = value
                expandedActive = false
              }
            )
          }
        }
      }

      OutlinedTextField(
        value = comment,
        onValueChange = { comment = it },
        label = { Text(stringResource(R.string.comment)) },
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        singleLine = false,
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.edit_note_24px),
            contentDescription = "edit_note_24px",
            tint = Colors.BlueGrey40,
            modifier = Modifier
              .size(32.dp)
              .offset(y = (-63).dp)
          )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Default
        ),
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
            MachineFormState(
              machineName = machineName,
              location = location,
              capacity = capacity,
              status = machineStatus,
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
              removeMachine()
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