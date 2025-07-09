package com.thanes.wardstock.screens.report

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.InventoryMinMax
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID.randomUUID

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
    val pageWidth = 595f
    val pageHeight = 842f
    val margin = 40f
    val tableWidth = 515f
    val rowHeight = 35f
    val headerHeight = 40f
    val colWidths = floatArrayOf(100f, 250f, 80f, 85f)

    val ibmBold = ResourcesCompat.getFont(context, R.font.ibmpiexsansthailooped_bold)
    val ibmNormal = ResourcesCompat.getFont(context, R.font.ibmpiexsansthailooped_regular)

    var currentPageNumber = 1
    var pageInfo =
      PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), currentPageNumber)
        .create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    val paint = Paint().apply {
      isAntiAlias = true
    }

    fun createNewPage() {
      pdfDocument.finishPage(page)
      currentPageNumber++
      pageInfo =
        PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), currentPageNumber)
          .create()
      page = pdfDocument.startPage(pageInfo)
      canvas = page.canvas

      paint.style = Paint.Style.FILL
      paint.color = Color.WHITE
      canvas.drawRect(0f, 0f, pageWidth, pageHeight, paint)
    }

    fun drawTableHeader(y: Float): Float {
      val headerY = y

      paint.style = Paint.Style.FILL
      paint.color = "#E3F2FD".toColorInt()
      val headerRect = RectF(margin, headerY, margin + tableWidth, headerY + headerHeight)
      canvas.drawRect(headerRect, paint)

      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 1f
      paint.color = Color.BLACK
      canvas.drawRect(headerRect, paint)

      var x = margin
      for (i in 0 until colWidths.size - 1) {
        x += colWidths[i]
        canvas.drawLine(x, headerY, x, headerY + headerHeight, paint)
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
        canvas.drawText(headers[i], textX, headerY + headerHeight / 2 + 4f, paint)
        x += colWidths[i]
      }

      return headerY + headerHeight
    }

    paint.style = Paint.Style.FILL
    paint.color = Color.WHITE
    canvas.drawRect(0f, 0f, pageWidth, pageHeight, paint)

    var y = 60f

    paint.textSize = 18f
    paint.typeface = Typeface.create(ibmBold, Typeface.BOLD)
    paint.color = Color.BLACK
    canvas.drawText("📊 รายงานช่องยา (Inventory Report)", margin + 100f, y, paint)
    y += 50f

    y = drawTableHeader(y)

    paint.textSize = 10f
    paint.typeface = Typeface.create(ibmNormal, Typeface.NORMAL)

    data?.forEachIndexed { index, item ->
      if (y + rowHeight + 100f > pageHeight - margin) {
        createNewPage()
        y = 60f

        paint.textSize = 18f
        paint.typeface = Typeface.create(ibmBold, Typeface.BOLD)
        paint.color = Color.BLACK
        canvas.drawText(
          "📊 รายงานช่องยา (Inventory Report) - หน้า $currentPageNumber",
          margin + 50f,
          y,
          paint
        )
        y += 50f

        y = drawTableHeader(y)

        paint.textSize = 10f
        paint.typeface = Typeface.create(ibmNormal, Typeface.NORMAL)
      }

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

      var x = margin
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

    if (y + 80f > pageHeight - margin) {
      createNewPage()
      y = 60f
    }

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
    val reportRect = RectF(20f, 20f, pageWidth - 20f, y + 30f)
    canvas.drawRect(reportRect, paint)

    pdfDocument.finishPage(page)

    val file = File(
      context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
      "inventory_report_${randomUUID()}.pdf"
    )
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

  fun openPdfWithIntent(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.provider",
      file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(uri, "application/pdf")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
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
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          pdfFile?.let { openPdfWithIntent(context, it) }
        },
        containerColor = Colors.BluePrimary,
        shape = RoundedCornerShape(RoundRadius.Medium),
        modifier = Modifier
          .height(72.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.picture_as_pdf_24px),
          contentDescription = "picture_as_pdf_24px",
          tint = Colors.white,
          modifier = Modifier.size(36.dp)
        )
      }
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      when {
        isLoading -> {
          CircularProgressIndicator()
        }

        errorMessage.isNotBlank() -> {
          Text("Error: $errorMessage", color = Colors.alert)
        }

        pdfFile != null -> {
          PdfLazyViewer(pdfFile!!)
        }

        else -> {
          if (!isLoading) {
            Text(stringResource(R.string.empty_data))
          }
        }
      }
    }
  }
}

@Composable
fun PdfLazyViewer(pdfFile: File) {
  var pageCount by remember { mutableIntStateOf(0) }
  var currentPage by remember { mutableIntStateOf(0) }

  val pdfRenderer by remember {
    mutableStateOf(createPdfRenderer(pdfFile))
  }

  DisposableEffect(pdfRenderer) {
    onDispose {
      pdfRenderer?.close()
    }
  }

  LaunchedEffect(pdfRenderer) {
    pageCount = pdfRenderer?.pageCount ?: 0
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (pdfRenderer == null || pageCount == 0) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .background(Colors.BlueGrey100)
      ) {
        items(pageCount) { index ->
          PdfPageItem(pdfRenderer!!, index) { visibleIndex ->
            currentPage = visibleIndex + 1
          }
        }
      }

      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp)
          .background(Colors.black.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text(
          text = "${stringResource(R.string.pages)} $currentPage / $pageCount",
          color = Colors.white
        )
      }
    }
  }
}

fun createPdfRenderer(file: File): PdfRenderer? {
  return try {
    val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    PdfRenderer(fileDescriptor)
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}

@Composable
fun PdfPageItem(
  pdfRenderer: PdfRenderer,
  pageIndex: Int,
  onVisible: (Int) -> Unit
) {
  var bitmap by remember { mutableStateOf<Bitmap?>(null) }

  var scale by remember { mutableFloatStateOf(1f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  val gestureModifier = Modifier.pointerInput(scale) {
    detectTapGestures(
      onDoubleTap = {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
      }
    )
  }

  val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
    scale = (scale * zoomChange).coerceIn(1f, 5f)
    offsetX += offsetChange.x
    offsetY += offsetChange.y
  }

  LaunchedEffect(pageIndex) {
    withContext(Dispatchers.IO) {
      val page = pdfRenderer.openPage(pageIndex)
      val scale = 5f
      val width = (page.width * scale).toInt()
      val height = (page.height * scale).toInt()
      val bmp = createBitmap(width, height)
      val renderRect = android.graphics.Rect(0, 0, width, height)
      page.render(bmp, renderRect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
      page.close()
      bitmap = bmp
    }
  }

  LaunchedEffect(Unit) {
    onVisible(pageIndex)
  }

  if (bitmap == null) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator()
    }
  } else {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 8.dp)
        .clip(RoundedCornerShape(RoundRadius.Medium))
        .graphicsLayer(
          scaleX = scale,
          scaleY = scale,
          translationX = offsetX,
          translationY = offsetY
        )
        .then(gestureModifier)
        .transformable(transformableState)
    ) {
      Image(
        bitmap = bitmap!!.asImageBitmap(),
        contentDescription = "Page $pageIndex",
        modifier = Modifier
          .fillMaxWidth()
      )
    }
  }
}
