package com.thanes.wardstock.screens.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.AuthViewModel
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.TokenHolder
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.screens.fvverify.MainDisplay
import com.thanes.wardstock.ui.components.keyboard.Keyboard
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.components.utils.GradientButton
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun LoginScreen(
  navController: NavHostController,
  authViewModel: AuthViewModel,
  fingerVienViewModel: FingerVeinViewModel,
  context: Context
) {
  val scope = rememberCoroutineScope()
  var userName by remember { mutableStateOf("") }
  var userPassword by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var isLoadingFinger by remember { mutableStateOf(false) }
  var showPass by remember { mutableStateOf(false) }
  var showDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val contextLang = LocalContext.current
  val isEnrolling by fingerVienViewModel.isEnrolling

  val focusRequesterPassword = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val hideKeyboard = Keyboard.hideKeyboard()

  val completeFieldMessage = stringResource(R.string.complete_field)
  val userDataInCompleteMessage = stringResource(R.string.userData_InComplete)

  fun handleLogin() {
    errorMessage = ""
    isLoading = true

    scope.launch {
      if (userName.isEmpty() || userPassword.isEmpty()) {
        errorMessage = completeFieldMessage
        isLoading = false
        return@launch
      }

      try {
        hideKeyboard()
        val response = ApiRepository.login(userName, userPassword)

        if (response.isSuccessful) {
          val userData = response.body()?.data

          if (userData != null) {
            val token = userData.token
            authViewModel.login(context, token, userData)
            TokenHolder.token = token
            navController.navigate(Routes.Home.route) {
              popUpTo(Routes.Login.route) { inclusive = true }
            }
          } else {
            errorMessage = userDataInCompleteMessage
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isLoading = false
      }
    }
  }

  fun handleLoginWithFinger(uid: String) {
    errorMessage = ""
    showDialog = false
    isLoadingFinger = true

    scope.launch {
      try {
        val response = ApiRepository.loginWithVein(uid)

        if (response.isSuccessful) {
          val userData = response.body()?.data

          if (userData != null) {
            val token = userData.token
            authViewModel.login(context, token, userData)
            TokenHolder.token = token
            navController.navigate(Routes.Home.route) {
              popUpTo(Routes.Login.route) { inclusive = true }
            }
          } else {
            errorMessage = userDataInCompleteMessage
          }
        } else {
          val errorJson = response.errorBody()?.string()
          val message = parseErrorMessage(response.code(), errorJson)
          errorMessage = message
        }
      } catch (e: Exception) {
        errorMessage = parseExceptionMessage(e)
      } finally {
        isLoadingFinger = false
      }
    }
  }

  fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
      if (context is Activity) return context
      context = context.baseContext
    }
    return null
  }

  LaunchedEffect(fingerVienViewModel.verifiedUid) {
    if (fingerVienViewModel.verifiedUid.value.isNotEmpty()) {
      handleLoginWithFinger(fingerVienViewModel.verifiedUid.value)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.linearGradient(
          colors = listOf(
            Colors.BlueSky.copy(alpha = 0.9f),
            Colors.BluePrimary,
          ), start = Offset(0.5f, 0f), end = Offset.Infinite
        )
      )
  ) {
    Box(
      modifier = Modifier
        .size(120.dp)
        .offset(x = 50.dp, y = 80.dp)
        .clip(RoundedCornerShape(60.dp))
        .background(Color.White.copy(alpha = 0.1f))
    )

    Box(
      modifier = Modifier
        .size(80.dp)
        .offset(x = 300.dp, y = 150.dp)
        .clip(RoundedCornerShape(40.dp))
        .background(Color.White.copy(alpha = 0.15f))
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 52.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
          .clip(RoundedCornerShape(RoundRadius.Large))
          .background(
            color = Colors.BlueGrey100.copy(alpha = 0.25f),
            shape = RoundedCornerShape(RoundRadius.Large)
          ),
        shape = RoundedCornerShape(RoundRadius.Large),
        colors = CardDefaults.cardColors(
          containerColor = Color.Transparent
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
        ) {
          Column {
            Image(
              painter = painterResource(R.drawable.login_banner),
              contentDescription = "LoginBanner",
              modifier = Modifier
                .width(290.dp)
                .height(290.dp),
              contentScale = ContentScale.Fit
            )
            Text(
              text = stringResource(R.string.app_title_login),
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Colors.BlueGrey120,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = stringResource(R.string.app_description_login),
              fontSize = 22.sp,
              fontWeight = FontWeight.Medium,
              color = Colors.BlueGrey100,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Spacer(modifier = Modifier.height(32.dp))

          OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text(stringResource(R.string.username_field)) },
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp),
            shape = RoundedCornerShape(RoundRadius.Large),
            textStyle = TextStyle(fontSize = 20.sp),
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.person_24px),
                contentDescription = "User Icon",
                tint = Colors.BlueGrey100,
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
              focusedTextColor = Colors.BlueGrey100,
              unfocusedTextColor = Colors.BlueGrey100,
              focusedIndicatorColor = Colors.BlueGrey100,
              unfocusedIndicatorColor = Colors.BlueGrey100.copy(alpha = 0.5f),
              focusedLabelColor = Colors.BlueGrey100,
              unfocusedLabelColor = Colors.BlueGrey100,
              cursorColor = Colors.BlueGrey100,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              errorContainerColor = Color.Transparent,
              focusedLeadingIconColor = Colors.BlueGrey100
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            label = { Text(stringResource(R.string.password_field)) },
            modifier = Modifier
              .fillMaxWidth()
              .height(60.dp)
              .focusRequester(focusRequesterPassword),
            shape = RoundedCornerShape(RoundRadius.Large),
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = TextStyle(fontSize = 20.sp),
            leadingIcon = {
              Icon(
                painter = painterResource(R.drawable.lock_24px),
                contentDescription = "Password Icon",
                tint = Colors.BlueGrey100,
                modifier = Modifier.size(32.dp)
              )
            },
            singleLine = true,
            maxLines = 1,
            trailingIcon = {
              IconButton(
                modifier = Modifier.padding(end = 4.dp), onClick = { showPass = !showPass }) {
                Icon(
                  painter = painterResource(
                    if (!showPass) R.drawable.visibility_24px else R.drawable.visibility_off_24px
                  ),
                  contentDescription = if (showPass) "Hide password" else "Show password",
                  tint = Colors.BlueGrey100,
                  modifier = Modifier
                    .size(32.dp)
                )
              }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
              onDone = {
                keyboardController?.hide()
                handleLogin()
              }),
            colors = TextFieldDefaults.colors(
              focusedTextColor = Colors.BlueGrey100,
              unfocusedTextColor = Colors.BlueGrey100,
              focusedIndicatorColor = Colors.BlueGrey100,
              unfocusedIndicatorColor = Colors.BlueGrey100.copy(alpha = 0.5f),
              focusedLabelColor = Colors.BlueGrey100,
              unfocusedLabelColor = Colors.BlueGrey100,
              cursorColor = Colors.BlueGrey100,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              errorContainerColor = Color.Transparent,
              focusedLeadingIconColor = Colors.BlueGrey100
            )
          )

          Spacer(modifier = Modifier.height(24.dp))

          GradientButton(
            onClick = {
              if (isLoading) return@GradientButton
              handleLogin()
            },
            shape = RoundedCornerShape(RoundRadius.Large),
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
                stringResource(R.string.sign_in),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Colors.BlueGrey100
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentSize(Alignment.CenterEnd)
          ) {
            TextButton(onClick = { /* TODO: Forgot password */ }) {
              Text(
                stringResource(R.string.forget_password),
                color = Colors.BlueGrey100,
                fontSize = 18.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
          ) {
            HorizontalDivider(
              modifier = Modifier
                .weight(0.5f)
                .height(1.dp),
              thickness = DividerDefaults.Thickness, color = Colors.BlueGrey80
            )
            Text(
              text = stringResource(R.string.or),
              modifier = Modifier.padding(horizontal = 8.dp),
              color = Colors.BlueGrey80,
              fontWeight = FontWeight.Medium
            )
            HorizontalDivider(
              modifier = Modifier
                .weight(0.5f)
                .height(1.dp),
              thickness = DividerDefaults.Thickness, color = Colors.BlueGrey80
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          GradientButton(
            onClick = {
//              fingerVienViewModel.toggleVerify()
              showDialog = true
            },
            shape = RoundedCornerShape(RoundRadius.Large),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .border(1.dp, Colors.BlueGrey100, RoundedCornerShape(RoundRadius.Large)),
            enabled = !isLoadingFinger,
            gradient = Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                Color.Transparent
              ),
            )
          ) {
            if (isLoadingFinger) {
              CircularProgressIndicator(
                color = Colors.BlueGrey100, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
              )
            } else {
              Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  painter = painterResource(R.drawable.fingerprint_24px),
                  contentDescription = "fingerprint_24px",
                  tint = Colors.BlueGrey100,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  stringResource(R.string.sign_in_with_finger),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Medium,
                  color = Colors.BlueGrey100
                )
              }
            }
          }
        }
      }
    }

    LaunchedEffect(showDialog) {
      if (showDialog) {
        delay(20)
        context.findActivity()?.let { activity ->
          HideSystemControll.manageSystemBars(activity, true)
        }
      }
    }

    if (showDialog) {
      AlertDialog(
        properties = DialogProperties(
          dismissOnBackPress = false,
          dismissOnClickOutside = false,
          usePlatformDefaultWidth = false
        ),
        icon = {},
        text = {
          Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.9f)
          ) {
            MainDisplay(
              bitmap = fingerVienViewModel.imageBitmap.value,
              isEnrolling = fingerVienViewModel.isEnrolling.value,
              isVerifying = fingerVienViewModel.isVerifying.value,
              lastLogMessage = fingerVienViewModel.logMessages.firstOrNull() ?: ""
            )
          }
        },
        onDismissRequest = {
          showDialog = false
        },
        confirmButton = {},
        dismissButton = {
          GradientButton(
            onClick = {
//              fingerVienViewModel.toggleVerify()
              showDialog = false
            },
            shape = RoundedCornerShape(RoundRadius.Medium),
            gradient = Brush.verticalGradient(
              colors = listOf(
                Colors.BlueGrey80,
                Colors.BlueGrey80
              ),
            ),
            modifier = Modifier
              .fillMaxWidth(0.9f)
              .height(56.dp)
          ) {
            Text(
              contextLang.getString(R.string.close),
              fontFamily = ibmpiexsansthailooped,
              color = Colors.BlueSecondary,
              fontSize = 20.sp,
            )
          }
        },
        containerColor = Colors.BlueGrey100,
      )
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }
}
