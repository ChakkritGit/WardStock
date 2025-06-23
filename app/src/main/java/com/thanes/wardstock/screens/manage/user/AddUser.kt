package com.thanes.wardstock.screens.manage.user

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thanes.wardstock.data.viewModel.UserViewModel
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.RoundRadius

@Composable
fun AddUser(navController: NavHostController, userSharedViewModel: UserViewModel) {
  var canClick by remember { mutableStateOf(true) }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

  val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      Log.d("PhotoPicker", "Selected URI: $uri")
    } else {
      Log.d("PhotoPicker", "No media selected")
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.add_user),
        onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Column {
        Button(onClick = { pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)) }) {
          Text("Choose Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
          val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
              .data(uri)
              .crossfade(true)
              .build()
          )
          Image(
            painter = painter,
            contentDescription = "Selected Image",
            modifier = Modifier
              .width(256.dp)
              .height(256.dp)
              .padding(8.dp)
              .clip(RoundedCornerShape(RoundRadius.Medium)),
            contentScale = ContentScale.Crop
          )
        }
      }
    }
  }
}