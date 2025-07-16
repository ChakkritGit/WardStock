package com.thanes.wardstock.data.viewModel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.utils.FingerVien
import kotlinx.coroutines.launch

class FingerVeinViewModel(application: Application) : AndroidViewModel(application) {

  private val fvController = FingerVien()
  private var isInitialized = false

  val imageBitmap = fvController.imageBitmap
  val logMessages = fvController.logMessages
  val isEnrolling = fvController.isEnrolling
  val isVerifying = fvController.isVerifying
  val verifiedUid = fvController.verifiedUid

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  fun initialize() {
    if (isInitialized) return

    val appContext = getApplication<Application>().applicationContext

    viewModelScope.launch {
      try {
        System.setProperty("jna.nosys", "true")

        fvController.sys_init(appContext)
        isInitialized = true
      } catch (e: Throwable) {
        fvController.updateMsg("เกิดข้อผิดพลาดร้ายแรงในการเริ่มต้น: ${e.message}")
        Log.e("FingerVeinViewModel", "Initialization failed", e)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  fun enroll(uid: String, uname: String = "") {
    if (!isInitialized) {
      fvController.updateMsg("ระบบยังไม่พร้อมใช้งาน")
      return
    }
    fvController.fv_enroll(uid, uname)
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  fun toggleVerify() {
    if (!isInitialized) {
      fvController.updateMsg("ระบบยังไม่พร้อมใช้งาน")
      return
    }
    fvController.fv_verify(!isVerifying.value)
  }

  fun deleteUser(uid: String) {
    if (!isInitialized || uid.isBlank()) return
    fvController.fv_del(uid)
  }

  fun clearAllUsers() {
    if (!isInitialized) return
    fvController.fv_clear()
  }

  override fun onCleared() {
    super.onCleared()
    if (isInitialized) {
      fvController.fv_exit()
      isInitialized = false
      Log.d("FingerVeinViewModel", "ViewModel cleared and fv_exit called.")
    }
  }
}