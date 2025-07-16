package com.thanes.wardstock.screens.manage.user

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun EnrollFingerprintDialog(
  viewModel: FingerVeinViewModel,
  onDismiss: () -> Unit
) {
  val isEnrolling by viewModel.isEnrolling

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isEnrolling) "กำลังลงทะเบียนลายนิ้วมือ..." else "ลงทะเบียนลายนิ้วมือ",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    },
    text = {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(
              width = 2.dp,
              color = if (isEnrolling) MaterialTheme.colorScheme.primary else Color.Gray,
              shape = RoundedCornerShape(16.dp)
            )
            .background(Color.Black),
          contentAlignment = Alignment.Center
        ) {
          viewModel.imageBitmap.value?.let { bitmap ->
            Image(
              bitmap = bitmap.asImageBitmap(),
              contentDescription = "ภาพสแกนเส้นเลือด",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          } ?: Text("วางนิ้วบนเครื่องสแกน", color = Color.White)
        }

        Text(
          text = viewModel.logMessages.firstOrNull() ?: "กรุณากด 'เริ่ม' เพื่อลงทะเบียน",
          textAlign = TextAlign.Center,
          minLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          viewModel.enroll(uid = "temp_enroll_id", uname = "")
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isEnrolling) Color.Red else MaterialTheme.colorScheme.primary
        )
      ) {
        Text(if (isEnrolling) "หยุด" else "เริ่มสแกน")
      }
    },
    dismissButton = {
      Button(
        onClick = {
          if (isEnrolling) {
            viewModel.enroll(uid = "", uname = "")
          }
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.secondary
        )
      ) {
        Text("ปิด")
      }
    }
  )
}