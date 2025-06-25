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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.thanes.wardstock.R
import com.thanes.wardstock.data.viewModel.DrugViewModel
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius

data class DrugFormState(
  val id: String = "",
  val drugCode: String = "",
  val drugName: String = "",
  val unit: String = "",
  val drugLot: String = "",
  val drugExpire: String = "",
  val drugPriority: Int = 1,
  val weight: Int = 0,
  val status: String = "",
  val picture: String = "",
  val comment: String = ""
)

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

  var drugCode by remember { mutableStateOf(initialData?.drugCode ?: "") }
  var drugName by remember { mutableStateOf(initialData?.drugName ?: "") }
  var unit by remember { mutableStateOf(initialData?.unit ?: "") }
  var drugLot by remember { mutableStateOf(initialData?.drugLot ?: "") }
  var drugExpire by remember { mutableStateOf(initialData?.drugExpire ?: "") }
  var drugPriority by remember { mutableIntStateOf(initialData?.drugPriority ?: 1) }
  var weight by remember { mutableIntStateOf(initialData?.weight ?: 0) }
  var status by remember { mutableStateOf(initialData?.status ?: "") }
  var picture by remember { mutableStateOf(initialData?.picture ?: "") }
  var comment by remember { mutableStateOf(initialData?.comment ?: "") }

  val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      Log.d("PhotoPicker", "Selected URI: $uri")
    } else {
      Log.d("PhotoPicker", "No media selected")
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
      modifier = Modifier.size(164.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clip(CircleShape)
          .background(Colors.BlueGrey100)
          .border(2.dp, Colors.BlueGrey80, CircleShape)
          .clickable {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
          }, contentAlignment = Alignment.Center
      ) {
        val imagePainter = rememberAsyncImagePainter(selectedImageUri ?: initialData?.picture)

        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(Colors.BlueGrey100)
            .border(2.dp, Colors.BlueGrey80, CircleShape)
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
                .clip(CircleShape),
              contentScale = ContentScale.Crop
            )
          } else {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Colors.BlueGrey40,
                modifier = Modifier.size(40.dp)
              )
              Text(
                text = stringResource(R.string.add_image),
                fontSize = 16.sp,
                color = Colors.BlueGrey40,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(Colors.BlueSecondary)
          .align(Alignment.BottomEnd)
          .clickable {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
          }, contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(R.drawable.add_24px),
          contentDescription = null,
          tint = Colors.BlueGrey100,
          modifier = Modifier.size(24.dp)
        )
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
    }

  }
}