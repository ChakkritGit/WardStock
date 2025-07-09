package com.thanes.wardstock.data.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.services.jna.FvLibrary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FingerVeinViewModel : ViewModel() {

  private val _verifyResult = MutableStateFlow<Boolean?>(null)
  val verifyResult = _verifyResult.asStateFlow()

  private val _registerResult = MutableStateFlow<Boolean?>(null)
  val registerResult = _registerResult.asStateFlow()

  private val _deleteResult = MutableStateFlow<Boolean?>(null)
  val deleteResult = _deleteResult.asStateFlow()

  private val _logs = MutableStateFlow<String?>(null)
  val logs = _logs.asStateFlow()

  private var lib: FvLibrary? = null

  private suspend fun prepareFvLibrary(context: Context): Boolean {
    return try {
      val configDir = File(context.filesDir, "fv_config")
      if (!configDir.exists()) configDir.mkdirs()

      val response = ApiRepository.getUserConfig()
      if (!response.isSuccessful || response.body()?.data.isNullOrBlank()) return false

      val jsonFile = File(configDir, "user.json")
      jsonFile.writeText(response.body()?.data ?: "")

      lib = FvLibrary.load()
      val result = lib?.fvInit(configDir.absolutePath) ?: -1
      result == 0
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }


  fun verify(context: Context) {
    viewModelScope.launch {
      val ok = prepareFvLibrary(context)
      if (!ok) {
        _verifyResult.value = false
        return@launch
      }
      val configDir = File(context.filesDir, "fv_config")
      val initResult = lib?.fvInit(configDir.absolutePath)
      if (initResult != 0) {
        _verifyResult.value = false
        return@launch
      }

      val imageBuffer = ByteArray(320 * 240)
      val template1 = ByteArray(1024)
      val template2 = ByteArray(1024)

      lib?.fvCaptureImage(imageBuffer, imageBuffer.size)

      lib?.fvExtractTemplate(imageBuffer, template1)
      lib?.fvCaptureImage(imageBuffer, imageBuffer.size)
      lib?.fvExtractTemplate(imageBuffer, template2)

      val result = lib?.fvVerify(template1, template2)
      _verifyResult.value = result == 0
      _verifyResult.value = result == 0
    }
  }

//  fun register(context: Context) {
//    viewModelScope.launch {
//      val ok = prepareFvLibrary(context)
//      if (!ok) {
//        _registerResult.value = false
//        return@launch
//      }
//
//      val result = lib?.fvRegister() ?: -1
//      _registerResult.value = result == 0
//    }
//  }
//
//  fun delete(context: Context) {
//    viewModelScope.launch {
//      val ok = prepareFvLibrary(context)
//      if (!ok) {
//        _deleteResult.value = false
//        return@launch
//      }
//
//      val result = lib?.fvDelete() ?: -1
//      _deleteResult.value = result == 0
//    }
//  }
//
//  fun loadLogs(context: Context) {
//    viewModelScope.launch {
//      val ok = prepareFvLibrary(context)
//      if (!ok) {
//        _logs.value = "ไม่สามารถโหลด log ได้"
//        return@launch
//      }
//
//      val logText = lib?.fvGetLogs() ?: "ไม่มี log"
//      _logs.value = logText
//    }
//  }
}
