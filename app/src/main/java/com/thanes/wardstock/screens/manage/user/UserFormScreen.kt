package com.thanes.wardstock.screens.manage.user

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch

data class UserFormState(
  val userId: String = "",
  val imageUri: Uri? = null,
  val username: String = "",
  val password: String = "",
  val display: String = "",
  val role: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
  context: Context,
  navController: NavHostController?,
  userSharedViewModel: UserViewModel?,
  initialData: UserFormState? = null,
  innerPadding: PaddingValues,
  isLoading: Boolean,
  showPasswordField: Boolean = true,
  onSubmit: suspend (UserFormState, Uri?) -> Boolean
) {
  var username by remember { mutableStateOf(initialData?.username ?: "") }
  var password by remember { mutableStateOf(initialData?.password ?: "") }
  var display by remember { mutableStateOf(initialData?.display ?: "") }
  var role by remember { mutableStateOf(initialData?.role ?: "") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  val focusRequesterUsername = remember { FocusRequester() }
  val focusRequesterPassword = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  var passwordVisible by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var isRemoving by remember { mutableStateOf(false) }
  val deleteMessage = stringResource(R.string.delete)
  val successMessage = stringResource(R.string.successfully)
  val scope = rememberCoroutineScope()

  val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      Log.d("PhotoPicker", "Selected URI: $uri")
    } else {
      Log.d("PhotoPicker", "No media selected")
    }
  }

  fun removeUser() {
    if (isRemoving) return

    scope.launch {
      try {
        isRemoving = true
        val response = ApiRepository.removeUser(userId = initialData?.userId ?: "")

        if (response.isSuccessful) {
          errorMessage = deleteMessage + successMessage
          userSharedViewModel?.fetchUser()
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
        val imagePainter = rememberAsyncImagePainter(selectedImageUri ?: initialData?.imageUri)

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
          if (selectedImageUri != null || initialData?.imageUri != null) {
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
        value = display,
        onValueChange = { display = it },
        label = { Text(stringResource(R.string.display_name)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RoundRadius.Large),
        textStyle = TextStyle(fontSize = 20.sp),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.badge_24px),
            contentDescription = "User Icon",
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
            focusRequesterUsername.requestFocus()
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
        value = username,
        onValueChange = { username = it },
        label = { Text(stringResource(R.string.username_field)) },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequesterUsername),
        shape = RoundedCornerShape(RoundRadius.Large),
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.person_24px),
            contentDescription = "User Icon",
            tint = Colors.BlueGrey40,
            modifier = Modifier.size(32.dp)
          )
        },
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
          onNext = {
            focusRequesterPassword.requestFocus()
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

      if (showPasswordField) {
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text(stringResource(R.string.password_field)) },
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequesterPassword),
          shape = RoundedCornerShape(RoundRadius.Large),
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.lock_24px),
              contentDescription = "Password Icon",
              tint = Colors.BlueGrey40,
              modifier = Modifier.size(32.dp)
            )
          },
          trailingIcon = {
            IconButton(
              onClick = { passwordVisible = !passwordVisible },
              modifier = Modifier.padding(end = 4.dp)
            ) {
              Icon(
                painter = painterResource(
                  if (!passwordVisible) R.drawable.visibility_24px else R.drawable.visibility_off_24px
                ),
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                tint = Colors.BlueGrey40,
                modifier = Modifier.size(32.dp)
              )
            }
          },
          singleLine = true,
          maxLines = 1,
          keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Next
          ),
          keyboardActions = KeyboardActions(
            onNext = {
              keyboardController?.hide()
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

      var expanded by remember { mutableStateOf(false) }
      val roles =
        listOf("SUPER", "SERVICE", "PHARMACIST", "PHARMACIST_ASSISTANCE", "HEAD_NURSE", "NURSE")

      ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
          value = role,
          onValueChange = {},
          readOnly = true,
          label = { Text(stringResource(R.string.user_role)) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.shield_toggle_24px),
              contentDescription = "User Icon",
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
          modifier = Modifier.background(
            Colors.BlueGrey100
          ),
          shape = RoundedCornerShape(RoundRadius.Large),
        ) {
          roles.forEach { option ->
            DropdownMenuItem(text = { Text(option) }, onClick = {
              role = option
              expanded = false
            })
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))

    GradientButton(
      onClick = {
        scope.launch {
          onSubmit(
            UserFormState(
              imageUri = selectedImageUri,
              username = username,
              password = password,
              display = display,
              role = role
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
              text = stringResource(R.string.confirm_delete_desc),
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
                removeUser()
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