package com.thanes.wardstock.services.upload

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

fun uriToMultipartBodyPart(
  context: Context,
  uri: Uri,
  partName: String = "image"
): MultipartBody.Part? {
  val contentResolver = context.contentResolver

  val mimeType = contentResolver.getType(uri) ?: "image/*"
  val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

  val inputStream = contentResolver.openInputStream(uri) ?: return null
  val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
  inputStream.close()

  val maxSize = 1080
  val width = originalBitmap.width
  val height = originalBitmap.height
  val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height, 1.0f)
  val scaledBitmap = originalBitmap.scale((width * scale).toInt(), (height * scale).toInt())

  val tempFile = File.createTempFile("image", ".$extension", context.cacheDir)
  val outputStream = FileOutputStream(tempFile)
  when (extension.lowercase()) {
    "png" -> scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    else -> scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
  }
  outputStream.flush()
  outputStream.close()

  val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), tempFile)
  return MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
}
