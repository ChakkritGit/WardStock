package com.thanes.wardstock.screens.manage.drug

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.ui.components.datePicker.DatePickerField
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DrugFormState(
  val id: String = "",
  val drugCode: String = "",
  val drugName: String = "",
  val unit: String = "",
  val drugLot: LocalDate = LocalDate.now(),
  val drugExpire: LocalDate = LocalDate.now(),
  val drugPriority: Int = 1,
  val weight: Int = 0,
  val status: Boolean = true,
  val picture: Uri? = null,
  val comment: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugFormScreen(
  context: Context,
  innerPadding: PaddingValues,
  isLoading: Boolean,
  navController: NavHostController?,
  drugSharedViewModel: DrugViewModel?,
  initialData: DrugFormState? = null,
  onSubmit: suspend (DrugFormState, Uri?) -> Boolean
) {
  var errorMessage by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var isRemoving by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)

  var drugCode by remember { mutableStateOf(initialData?.drugCode ?: "") }
  var drugName by remember { mutableStateOf(initialData?.drugName ?: "") }
  var unit by remember { mutableStateOf(initialData?.unit ?: "") }
  var drugLot by remember { mutableStateOf(initialData?.drugLot ?: LocalDate.now()) }
  var drugExpire by remember { mutableStateOf(initialData?.drugExpire ?: LocalDate.now()) }
  var drugPriority by remember { mutableIntStateOf(initialData?.drugPriority ?: 1) }
  var weight by remember { mutableIntStateOf(initialData?.weight ?: 0) }
  var drugStatus by remember { mutableStateOf(initialData?.status != false) }
  var comment by remember { mutableStateOf(initialData?.comment ?: "") }
  val scope = rememberCoroutineScope()

  val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      Log.d("PhotoPicker", "Selected URI: $uri")
    } else {
      Log.d("PhotoPicker", "No media selected")
    }
  }

  fun removeDrug() {
    if (isRemoving) return

    scope.launch {
      try {
        isRemoving = true
        val response = ApiRepository.removeDrug(drugId = initialData?.id ?: "")

        if (response.isSuccessful) {
          errorMessage = deleteMessage + successMessage
          drugSharedViewModel?.fetchDrug()
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
      .verticalScroll(rememberScrollState())
      .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape((RoundRadius.Large)))
          .background(Colors.BlueGrey100)
          .border(2.dp, Colors.BlueGrey80, RoundedCornerShape((RoundRadius.Large)))
          .clickable {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
          }, contentAlignment = Alignment.Center
      ) {
        val imagePainter = rememberAsyncImagePainter(selectedImageUri ?: initialData?.picture)

        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape((RoundRadius.Large)))
            .background(Colors.BlueGrey100)
            .border(2.dp, Colors.BlueGrey80, RoundedCornerShape((RoundRadius.Large)))
            .clickable {
              pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }, contentAlignment = Alignment.Center
        ) {
          if (selectedImageUri != null || initialData?.picture != null) {
            Image(
              painter = imagePainter,
              contentDescription = null,
              modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape((RoundRadius.Large))),
              contentScale = ContentScale.Crop
            )
          } else {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                painter = painterResource(R.drawable.medication_24px),
                contentDescription = "medication_24px",
                tint = Colors.BlueGrey40,
                modifier = Modifier.size(48.dp)
              )
              Text(
                text = stringResource(R.string.add_image),
                fontSize = 18.sp,
                color = Colors.BlueGrey40,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      Box(
        modifier = Modifier
          .padding(12.dp)
          .align(Alignment.BottomEnd)
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape((RoundRadius.Large)))
            .background(Colors.BlueSecondary)
            .clickable {
              pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }, contentAlignment = Alignment.Center
        ) {
          Icon(
            painter = painterResource(R.drawable.add_24px),
            contentDescription = "add_24px",
            tint = Colors.BlueGrey100,
            modifier = Modifier.size(32.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Column(
      modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      OutlinedTextField(
        value = drugCode,
        onValueChange = { drugCode = it },
        label = { Text(stringResource(R.string.drug_code)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.barcode_24px),
            contentDescription = "barcode_24px",
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
        value = drugName,
        onValueChange = { drugName = it },
        label = { Text(stringResource(R.string.drug_name)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.pill_24px),
            contentDescription = "barcode_24px",
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
        value = unit,
        onValueChange = { unit = it },
        label = { Text(stringResource(R.string.drug_unit)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.mixture_med_24px),
            contentDescription = "barcode_24px",
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
        value = weight.toString(),
        onValueChange = {
          val value = it.toIntOrNull()
          weight = when {
            value == null -> 0
            value <= 0 -> 0
            value > 1000 -> 1000
            else -> value
          }
        },
        label = { Text(stringResource(R.string.drug_weight)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.weight_24px),
            contentDescription = "barcode_24px",
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

      DatePickerField(
        context,
        selectedDate = drugLot,
        label = R.string.drug_lot,
        onDateSelected = { newDate -> drugLot = newDate }
      )

      DatePickerField(
        context,
        selectedDate = drugExpire,
        label = R.string.drug_expire,
        onDateSelected = { newDate -> drugExpire = newDate }
      )

      val drugPriorityOptions = listOf(
        R.string.normal_drug,
        R.string.Had_drug,
        R.string.Narcotic_drug
      )

      var expanded by remember { mutableStateOf(false) }

      val selectedLabel =
        drugPriorityOptions.getOrNull(drugPriority - 1)?.let { stringResource(it) } ?: ""

      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
      ) {
        OutlinedTextField(
          value = selectedLabel,
          onValueChange = {},
          readOnly = true,
          label = { Text(stringResource(R.string.drug_priority)) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.emergency_24px),
              contentDescription = "asterisk_24px",
              tint = Colors.BlueGrey40,
              modifier = Modifier.size(32.dp)
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
          expanded = expanded,
          onDismissRequest = { expanded = false },
          shadowElevation = 6.dp,
          modifier = Modifier.background(Colors.BlueGrey100),
          shape = RoundedCornerShape(RoundRadius.Large)
        ) {
          drugPriorityOptions.forEachIndexed { index, stringResId ->
            DropdownMenuItem(
              text = { Text(stringResource(stringResId)) },
              onClick = {
                drugPriority = index + 1
                expanded = false
              }
            )
          }
        }
      }

      var expandedActive by remember { mutableStateOf(false) }
      val activeOptions = listOf(
        true to R.string.active_true,
        false to R.string.active_false
      )

      val selectedActiveLabel = stringResource(
        id = if (drugStatus) R.string.active_true else R.string.active_false
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
                drugStatus = value
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
            DrugFormState(
              picture = selectedImageUri,
              drugCode = drugCode,
              drugName = drugName,
              unit = unit,
              weight = weight,
              drugLot = drugLot,
              drugExpire = drugExpire,
              drugPriority = drugPriority,
              status = drugStatus,
              comment = comment
            ), selectedImageUri
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
                removeDrug()
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
}