package com.thanes.wardstock.screens.manage.user

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.ui.components.keyboard.Keyboard
import com.thanes.wardstock.ui.components.loading.LoadingDialog
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFingerprint(
  navController: NavHostController,
  context: Context,
  userSharedViewModel: UserViewModel,
  fingerVeinViewModel: FingerVeinViewModel
) {
  var canClick by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  val isFocus = remember { FocusRequester() }
  val scope = rememberCoroutineScope()
  var description by remember {
    mutableStateOf(
      userSharedViewModel.fingerprintObject?.description ?: ""
    )
  }
  val completeFieldMessage = stringResource(R.string.complete_field)
  val somethingWrong = stringResource(R.string.something_wrong)
  val hideKeyboard = Keyboard.hideKeyboard()

  fun handleUpdate() {
    errorMessage = ""
    isLoading = true

    scope.launch {
      if (description.isEmpty()) {
        errorMessage = completeFieldMessage
        isLoading = false
        return@launch
      }

      if (userSharedViewModel.fingerprintObject == null) {
        errorMessage = somethingWrong
        isLoading = false
        return@launch
      }

      userSharedViewModel.fingerprintObject.let {
        try {
          hideKeyboard()
          val response = ApiRepository.updateFingerprint(it?.id ?: "", description)

          if (response.isSuccessful) {
            val message = response.body()?.data
            errorMessage = message ?: "Successfully"
            userSharedViewModel.fetchUserFingerprint(it?.userId ?: "")
          } else {
            val errorJson = response.errorBody()?.string()
            val message = parseErrorMessage(response.code(), errorJson)
            errorMessage = message
          }
        } catch (e: Exception) {
          errorMessage = parseExceptionMessage(e)
        } finally {
          isLoading = false
          delay(650)
          userSharedViewModel.clearFingerObject()
          navController.popBackStack()
        }
      }
    }
  }

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LoadingDialog(isRemoving = isLoading)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .padding(end = 18.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(RoundRadius.Large))
                .clickable(onClick = {
                  if (canClick) {
                    canClick = false
                    navController.popBackStack()
                  }
                })
            ) {
              Icon(
                painter = painterResource(R.drawable.arrow_back_ios_new_24px),
                contentDescription = "arrow_back_ios_new_24px",
                tint = Colors.BluePrimary,
                modifier = Modifier
                  .size(44.dp)
                  .padding(6.dp)
              )
              Text(
                text = stringResource(R.string.back_button),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                fontFamily = ibmpiexsansthailooped,
                fontSize = 22.sp,
                modifier = Modifier
                  .widthIn(max = 500.dp)
                  .padding(end = 14.dp)
              )
            }
            TextButton(
              onClick = { handleUpdate() },
              enabled = description.isNotEmpty()
            ) {
              Text(
                stringResource(R.string.done_button),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = ibmpiexsansthailooped
              )
            }
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Colors.BlueGrey100,
          titleContentColor = Colors.BluePrimary,
        ),
      )
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 30.dp, vertical = 12.dp)
      ) {
        userSharedViewModel.fingerprintObject?.let {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 20.dp)
          ) {
            OutlinedTextField(
              value = description,
              onValueChange = { description = it },
              modifier = Modifier
                .fillMaxWidth()
                .focusRequester(isFocus),
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 20.sp,
                fontFamily = ibmpiexsansthailooped
              ),
              shape = RoundedCornerShape(RoundRadius.Large),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Colors.BlueGrey120,
                unfocusedContainerColor = Colors.BlueGrey120,
                focusedBorderColor = Colors.BlueGrey80,
                unfocusedBorderColor = Colors.BlueGrey80,
                cursorColor = Color.Black
              ),
              trailingIcon = {
                if (description.isNotEmpty()) {
                  Surface(
                    modifier = Modifier
                      .padding(end = 16.dp)
                      .size(32.dp)
                      .clip(CircleShape)
                      .clickable {
                        description = ""
                        isFocus.requestFocus()
                      },
                    color = Colors.BlueGrey80.copy(alpha = 0.5f)
                  ) {
                    Icon(
                      painter = painterResource(id = R.drawable.close_24px),
                      contentDescription = "close_24px",
                      modifier = Modifier
                        .size(28.dp)
                        .padding(6.dp),
                      tint = Color.DarkGray
                    )
                  }
                }
              }
            )
          }
        }
      }
    }
  }
}