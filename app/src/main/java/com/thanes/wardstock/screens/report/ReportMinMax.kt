package com.thanes.wardstock.screens.report

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.InventoryMinMax
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ReportMinMax(navController: NavHostController) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var canClick by remember { mutableStateOf(true) }
  var inventoryListState by remember { mutableStateOf<List<InventoryMinMax>>(emptyList()) }
  var pdfFile by remember { mutableStateOf<File?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }

  fun generatePdf(data: List<InventoryMinMax>?): File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    var y = 50f

    paint.textSize = 14f
    canvas.drawText("Inventory Report", 200f, y, paint)
    y += 30f

    data?.forEach {
      canvas.drawText(
        "Pos: ${it.inventoryPosition} | Drug: ${it.drugName} | Qty: ${it.inventoryQty}",
        20f,
        y,
        paint
      )
      y += 20f
    }

    pdfDocument.finishPage(page)

    val file =
      File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "inventory_report.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    return file
  }

  fun fetchAndGeneratePdf() {
    errorMessage = ""
    isLoading = true

    scope.launch {
      try {
        val response = ApiRepository.getReportAlertMinMax()
        if (response.isSuccessful) {
          inventoryListState = response.body()?.data ?: emptyList()
          pdfFile = generatePdf(response.body()?.data)
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

  LaunchedEffect(errorMessage) {
    if (errorMessage.isNotEmpty()) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
      errorMessage = ""
    }
  }

  LaunchedEffect(inventoryListState) {
    if (inventoryListState.isEmpty()) {
      fetchAndGeneratePdf()
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.min_max),
        onBack = {
          if (canClick) {
            canClick = false
            navController.popBackStack()
          }
        })
    }, containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {
        when {
          isLoading -> {
            CircularProgressIndicator()
          }

          errorMessage.isNotBlank() -> {
            Text("Error: $errorMessage", color = Colors.alert)
          }

          pdfFile != null -> {
            Text("สร้าง PDF สำเร็จ: ${pdfFile?.name}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
              val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile!!
              )
              val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              }
              context.startActivity(intent)
            }) {
              Text("เปิด PDF")
            }
          }
        }
      }
    }
  }
}