package com.thanes.wardstock.screens.report

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val paint = Paint().apply {
      isAntiAlias = true
    }

    val margin = 40f
    val tableWidth = 515f
    val rowHeight = 35f
    val headerHeight = 40f

    val colWidths = floatArrayOf(100f, 250f, 80f, 85f)
    var y = 60f

    val ibmBold = ResourcesCompat.getFont(context, R.font.ibmpiexsansthailooped_bold)
    val ibmNormal = ResourcesCompat.getFont(context, R.font.ibmpiexsansthailooped_regular)

    paint.textSize = 18f
    paint.typeface = Typeface.create(ibmBold, Typeface.BOLD)
    paint.color = Color.BLACK
    canvas.drawText("📊 รายงานช่องยา (Inventory Report)", margin + 100f, y, paint)
    y += 50f

    paint.style = Paint.Style.FILL
    paint.color = "#E3F2FD".toColorInt()
    val headerRect = RectF(margin, y, margin + tableWidth, y + headerHeight)
    canvas.drawRect(headerRect, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1f
    paint.color = Color.BLACK
    canvas.drawRect(headerRect, paint)

    var x = margin
    for (i in 0 until colWidths.size - 1) {
      x += colWidths[i]
      canvas.drawLine(x, y, x, y + headerHeight, paint)
    }

    paint.style = Paint.Style.FILL
    paint.color = "#1976D2".toColorInt()
    paint.textSize = 12f
    paint.typeface = Typeface.create(ibmBold, Typeface.BOLD)

    val headers = arrayOf(
      context.getString(R.string.position),
      context.getString(R.string.drug_name),
      context.getString(R.string.quantity),
      context.getString(R.string.pdf_min_max)
    )
    x = margin
    for (i in headers.indices) {
      val textX = x + (colWidths[i] - paint.measureText(headers[i])) / 2
      canvas.drawText(headers[i], textX, y + headerHeight / 2 + 4f, paint)
      x += colWidths[i]
    }

    y += headerHeight

    paint.textSize = 10f
    paint.typeface = Typeface.create(ibmNormal, Typeface.NORMAL)

    data?.forEachIndexed { index, item ->
      if (index % 2 == 0) {
        paint.style = Paint.Style.FILL
        paint.color = "#F8F9FA".toColorInt()
        val rowRect = RectF(margin, y, margin + tableWidth, y + rowHeight)
        canvas.drawRect(rowRect, paint)
      }

      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 1f
      paint.color = Color.BLACK
      val rowRect = RectF(margin, y, margin + tableWidth, y + rowHeight)
      canvas.drawRect(rowRect, paint)

      x = margin
      for (i in 0 until colWidths.size - 1) {
        x += colWidths[i]
        canvas.drawLine(x, y, x, y + rowHeight, paint)
      }

      paint.style = Paint.Style.FILL
      paint.color = Color.BLACK
      x = margin

      val centerY = y + rowHeight / 2 + 4f

      val posText = item.inventoryPosition.toString()
      canvas.drawText(posText, x + (colWidths[0] - paint.measureText(posText)) / 2, centerY, paint)
      x += colWidths[0]

      val drugName = item.drugName.let { if (it.length > 30) it.take(27) + "..." else it }
      canvas.drawText(drugName, x + 5f, centerY, paint)
      x += colWidths[1]

      val qtyText = item.inventoryQty.toString()
      canvas.drawText(qtyText, x + (colWidths[2] - paint.measureText(qtyText)) / 2, centerY, paint)
      x += colWidths[2]

      val minMaxText = "${item.inventoryMin}/${item.inventoryMAX}"
      canvas.drawText(
        minMaxText,
        x + (colWidths[3] - paint.measureText(minMaxText)) / 2,
        centerY,
        paint
      )

      y += rowHeight
    }

    y += 30f
    paint.textSize = 12f
    paint.typeface = Typeface.create(ibmBold, Typeface.BOLD)
    paint.color = "#1976D2".toColorInt()
    canvas.drawText("📋 ${context.getString(R.string.summary)}:", margin, y, paint)
    y += 20f

    paint.textSize = 10f
    paint.typeface = Typeface.create(ibmNormal, Typeface.NORMAL)
    paint.color = Color.BLACK
    canvas.drawText("${context.getString(R.string.total)}: ${data?.size ?: 0}", margin + 20f, y, paint)
    y += 15f

    val totalQty = data?.sumOf { it.inventoryQty } ?: 0
    canvas.drawText("${context.getString(R.string.total_quantity)}: $totalQty", margin + 20f, y, paint)
    y += 15f

    val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText("${context.getString(R.string.generated)}: $currentDate", margin + 20f, y, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = "#1976D2".toColorInt()
    val reportRect = RectF(20f, 20f, 575f, y + 30f)
    canvas.drawRect(reportRect, paint)

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
          pdfFile = generatePdf(response.body()?.data ?: emptyList())
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