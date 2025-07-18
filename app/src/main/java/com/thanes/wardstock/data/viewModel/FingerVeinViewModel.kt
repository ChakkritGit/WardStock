package com.thanes.wardstock.data.viewModel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.utils.FingerVien
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class FingerVeinViewModel(application: Application) : AndroidViewModel(application) {
  private val fvController = FingerVien()
  private var isInitialized = false
  val imageBitmap = fvController.imageBitmap
  val logMessages = fvController.logMessages
  val isEnrolling = fvController.isEnrolling
  val isVerifying = fvController.isVerifying
  val verifiedUid = fvController.verifiedUid
  val verifiedUsername = fvController.verifiedUsername
  val lastEnrolledTemplate: State<String?> = fvController.lastEnrolledTemplate

  private val MAX_FAILED_ATTEMPTS = 5
  private val VERIFICATION_THRESHOLD = 0.75

  private var failedAttempts = 0
  val isLockedOut = mutableStateOf(false)

  private var maxScoreInAttempt = 0.0
  private var isFingerCurrentlyDown = false
  private var verificationJob: Job? = null
  private val LOCKOUT_DURATION_SECONDS = 30
  val lockoutCountdown = mutableIntStateOf(0)
  private var lockoutJob: Job? = null

  init {
    setupFingerVeinCallbacks()
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)

  private fun setupFingerVeinCallbacks() {
    fvController.onVerificationResult = { isSuccess ->
      viewModelScope.launch(Dispatchers.Main) launch@{
        if (isVerifying.value.not()) {
          return@launch
        }
        if (isSuccess) {
          handleVerificationSuccess()
        }
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
    Log.d("FingerVeinVM", "Judging attempt. Max score: $maxScoreInAttempt")
    if (maxScoreInAttempt >= VERIFICATION_THRESHOLD) {
      handleVerificationSuccess()
    } else {
      handleVerificationFailure()
    }
  }

  private fun handleVerificationSuccess() {
    stopVerification()
    failedAttempts = 0
    isLockedOut.value = false
  }

  private fun handleVerificationFailure() {
    failedAttempts++
    if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
      isLockedOut.value = true
      fvController.updateMsg("ยืนยันตัวตนล้มเหลวครบ $MAX_FAILED_ATTEMPTS ครั้ง! ระบบถูกล็อก")
      stopVerification()

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
      lockoutCountdown.intValue = LOCKOUT_DURATION_SECONDS

      tickerFlow(LOCKOUT_DURATION_SECONDS)
        .onCompletion {
          if (isLockedOut.value) {
            resetLockout()
          }
        }
        .collect { remainingSeconds ->
          lockoutCountdown.intValue = remainingSeconds
        }
    }
  }

  private fun tickerFlow(periodSeconds: Int) = flow {
    for (i in periodSeconds downTo 1) {
      emit(i)
      delay(1000)
    }
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
    if (!isInitialized) {
      fvController.updateMsg("ระบบยังไม่ได้เริ่มต้น ไม่สามารถโหลดข้อมูลได้")
      return
    }
    fvController.userHandler.loadUser()
  }

  fun enroll(uid: String, uname: String = "") {
    if (!isInitialized) {
      fvController.updateMsg("ระบบยังไม่พร้อมใช้งาน")
      return
    }
    fvController.fv_enroll(uid, uname)
  }

  fun toggleVerify() {
    if (isVerifying.value) {
      stopVerification()
    } else {
      startVerification()
    }
  }

  private fun startVerification() {
    if (isLockedOut.value) {
      fvController.updateMsg("ระบบถูกล็อกอยู่")
      return
    }
    resetVerificationState()
    fvController.fv_verify(true)
  }

  private fun stopVerification() {
    verificationJob?.cancel()
    fvController.fv_verify(false)
  }

  private fun resetVerificationState() {
    lockoutCountdown.intValue = 0
    failedAttempts = 0
    maxScoreInAttempt = 0.0
    isFingerCurrentlyDown = false
    fvController.updateMsg("กรุณาวางนิ้วเพื่อยืนยันตัวตน")
  }

  fun resetLockout() {
    lockoutJob?.cancel()

    isLockedOut.value = false
    resetVerificationState()
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
    verificationJob?.cancel()
    lockoutJob?.cancel()
    if (isInitialized) {
      fvController.fv_exit()
      isInitialized = false
    }
  }
}