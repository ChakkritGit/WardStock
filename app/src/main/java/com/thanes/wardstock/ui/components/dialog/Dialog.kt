package com.thanes.wardstock.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun AlertDialogCustom(
  dialogTitle: String,
  dialogText: String,
  icon: Int,
) {
  val imageLoader = ImageLoader.Builder(LocalContext.current)
    .components {
      add(GifDecoder.Factory())
    }
    .build()

  AlertDialog(
    containerColor = Colors.BlueGrey100,
    icon = {
      Surface(
        modifier = Modifier
          .clip(shape = CircleShape),
        color = Colors.BlueGrey100
      ) {
        AsyncImage(
          model = icon,
          contentDescription = "Reading_gif",
          imageLoader = imageLoader,
          modifier = Modifier
            .size(256.dp)
            .padding(6.dp)
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.7f)
      ) {
        Text(
          dialogTitle,
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = ibmpiexsansthailooped
        )
        Text(
          dialogText,
          fontSize = 22.sp,
          fontFamily = ibmpiexsansthailooped
        )
      }
    },
    onDismissRequest = {},
    confirmButton = {},
    dismissButton = {}
  )
}
