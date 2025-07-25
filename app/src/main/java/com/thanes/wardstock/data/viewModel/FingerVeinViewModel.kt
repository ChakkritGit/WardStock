package com.thanes.wardstock.data.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.utils.FingerVien
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class FingerVeinViewModel(application: Application) : AndroidViewModel(application) {
  private val fvController = FingerVien()
  private var isInitialized = false

  val imageBitmap = fvController.imageBitmap
  val logMessages = fvController.logMessages
  val isEnrolling = fvController.isEnrolling
  val isVerifying = fvController.isVerifying
  val verifiedUid = fvController.verifiedUid
  val lastEnrolledTemplate: State<String?> = fvController.lastEnrolledTemplate

  private val MAX_FAILED_ATTEMPTS = 5
  private val VERIFICATION_THRESHOLD = 0.75
  private val LOCKOUT_DURATION_SECONDS = 30

  private var failedAttempts = 0
  val isLockedOut = mutableStateOf(false)
  val lockoutCountdown = mutableStateOf(0)

  private var lockoutJob: Job? = null
  private var verificationJob: Job? = null
  private var maxScoreInAttempt = 0.0
  private var isFingerCurrentlyDown = false

  init {
    setupFingerVeinCallbacks()
  }

  private fun setupFingerVeinCallbacks() {
    fvController.onVerificationResult = { isSuccess ->
      if (isSuccess) {
        handleVerificationSuccess()
      }
    }
    fvController.onVerificationScoreUpdated = { score ->
      if (isFingerCurrentlyDown && score > maxScoreInAttempt) {
        maxScoreInAttempt = score
      }
    }
    fvController.onFingerStatusChanged = { isFingerDown ->
      if (isVerifying.value && !isLockedOut.value) {
        if (isFingerDown && !isFingerCurrentlyDown) {
          isFingerCurrentlyDown = true
          maxScoreInAttempt = 0.0
          fvController.updateMsg("กำลังประมวลผล...")
        }
        if (!isFingerDown && isFingerCurrentlyDown) {
          isFingerCurrentlyDown = false
          viewModelScope.launch {
            delay(100)
            if (isVerifying.value) {
              judgeAttemptByScore()
            }
          }
        }
      }
    }
  }

  private fun judgeAttemptByScore() {
    // ให้ cbVerify เป็นตัวตัดสินหลัก, ที่นี่เราจะจัดการเฉพาะกรณีล้มเหลว
    if (maxScoreInAttempt < VERIFICATION_THRESHOLD) {
      handleVerificationFailure()
    }
  }

  private fun handleVerificationSuccess() {
    if (!isVerifying.value) return
    stopVerification(isSuccess = true)
    failedAttempts = 0
    isLockedOut.value = false
  }

  private fun handleVerificationFailure() {
    failedAttempts++
    if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
      isLockedOut.value = true
      fvController.updateMsg("ยืนยันตัวตนล้มเหลวครบ $MAX_FAILED_ATTEMPTS ครั้ง!")
      stopVerification(isSuccess = false)
      startLockoutCountdown()
    } else {
      val remaining = MAX_FAILED_ATTEMPTS - failedAttempts
      val scorePercent = (maxScoreInAttempt * 100).toInt()
      fvController.updateMsg("ไม่สำเร็จ (ความแม่นยำ $scorePercent%), เหลือโอกาสอีก $remaining ครั้ง")
    }
  }

  private fun startLockoutCountdown() {
    lockoutJob?.cancel()
    lockoutJob = viewModelScope.launch {
      lockoutCountdown.value = LOCKOUT_DURATION_SECONDS
      tickerFlow(LOCKOUT_DURATION_SECONDS)
        .onCompletion {
          if (isLockedOut.value) {
            resetLockout()
          }
        }
        .collect { remainingSeconds ->
          lockoutCountdown.value = remainingSeconds
        }
    }
  }

  private fun tickerFlow(periodSeconds: Int) = flow {
    for (i in periodSeconds downTo 1) {
      emit(i)
      delay(1000)
    }
  }

  fun resetLockout() {
    lockoutJob?.cancel()
    isLockedOut.value = false
    failedAttempts = 0
    lockoutCountdown.value = 0
    fvController.updateMsg("ระบบพร้อมใช้งาน")
    toggleVerify()
  }

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
      }
    }
  }

  fun reloadAllBiometrics() {
    if (!isInitialized) return
    fvController.userHandler.loadUser()
  }

  fun enroll(uid: String, uname: String = "") {
    if (!isInitialized) return
    fvController.fv_enroll(uid, uname)
  }

  fun toggleVerify() {
    if (isVerifying.value) {
      stopVerification(isSuccess = false)
    } else {
      startVerification()
    }
  }

  private fun startVerification() {
    if (isLockedOut.value) {
      fvController.updateMsg("ระบบถูกล็อกอยู่")
      return
    }
    // รีเซ็ตสถานะทุกครั้งที่เริ่มใหม่
    failedAttempts = 0
    maxScoreInAttempt = 0.0
    isFingerCurrentlyDown = false
    fvController.updateMsg("กรุณาวางนิ้วเพื่อยืนยันตัวตน")

    isVerifying.value = true
    fvController.fv_verify(true)
  }

  private fun stopVerification(isSuccess: Boolean) {
    if (!isVerifying.value) return
    fvController.fv_verify(false)
    isVerifying.value = false
    if (!isSuccess) {
      fvController.updateMsg("หยุดการยืนยันตัวตน")
    }
  }

  fun deleteUser(uid: String) {
    if (!isInitialized || uid.isBlank()) return
    fvController.fv_del(uid)
  }

  fun clearAllUsers() {
    if (!isInitialized) return
    fvController.fv_clear()
  }

  fun clearLastEnrolledTemplate() {
    fvController.clearLastEnrolledTemplate()
  }

  override fun onCleared() {
    super.onCleared()
    lockoutJob?.cancel()
    verificationJob?.cancel()
    if (isInitialized) {
      fvController.fv_exit()
    }
  }
}