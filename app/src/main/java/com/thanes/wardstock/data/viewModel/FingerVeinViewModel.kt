//package com.thanes.wardstock.data.viewModel
//
//import android.app.Application
//import androidx.compose.runtime.State
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.thanes.wardstock.utils.FingerVien
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.flow.onCompletion
//import kotlinx.coroutines.launch
//
//class FingerVeinViewModel(application: Application) : AndroidViewModel(application) {
//  private val fvController = FingerVien()
//  private var isInitialized = false
//
//  val imageBitmap = fvController.imageBitmap
//  val logMessages = fvController.logMessages
//  val isEnrolling = fvController.isEnrolling
//  val isVerifying = fvController.isVerifying
//  val verifiedUid = fvController.verifiedUid
//  val lastEnrolledTemplate: State<String?> = fvController.lastEnrolledTemplate
//
//  private val MAX_FAILED_ATTEMPTS = 5
//  private val VERIFICATION_THRESHOLD = 0.75
//  private val LOCKOUT_DURATION_SECONDS = 30
//
//  private var failedAttempts = 0
//  val isLockedOut = mutableStateOf(false)
//  val lockoutCountdown = mutableStateOf(0)
//
//  private var lockoutJob: Job? = null
//  private var verificationJob: Job? = null
//  private var maxScoreInAttempt = 0.0
//  private var isFingerCurrentlyDown = false
//
//  init {
//    setupFingerVeinCallbacks()
//  }
//
//  private fun setupFingerVeinCallbacks() {
//    fvController.onVerificationResult = { isSuccess ->
//      if (isSuccess) {
//        handleVerificationSuccess()
//      }
//    }
//    fvController.onVerificationScoreUpdated = { score ->
//      if (isFingerCurrentlyDown && score > maxScoreInAttempt) {
//        maxScoreInAttempt = score
//      }
//    }
//    fvController.onFingerStatusChanged = { isFingerDown ->
//      if (isVerifying.value && !isLockedOut.value) {
//        if (isFingerDown && !isFingerCurrentlyDown) {
//          isFingerCurrentlyDown = true
//          maxScoreInAttempt = 0.0
//          fvController.updateMsg("กำลังประมวลผล...")
//        }
//        if (!isFingerDown && isFingerCurrentlyDown) {
//          isFingerCurrentlyDown = false
//          viewModelScope.launch {
//            delay(100)
//            if (isVerifying.value) {
//              judgeAttemptByScore()
//            }
//          }
//        }
//      }
//    }
//  }
//
//  private fun judgeAttemptByScore() {
//    if (maxScoreInAttempt < VERIFICATION_THRESHOLD) {
//      handleVerificationFailure()
//    }
//  }
//
//  private fun handleVerificationSuccess() {
//    if (!isVerifying.value) return
//    stopVerification(isSuccess = true)
//    failedAttempts = 0
//    isLockedOut.value = false
//  }
//
//  private fun handleVerificationFailure() {
//    failedAttempts++
//    if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
//      isLockedOut.value = true
//      fvController.updateMsg("ยืนยันตัวตนล้มเหลวครบ $MAX_FAILED_ATTEMPTS ครั้ง!")
//      stopVerification(isSuccess = false)
//      startLockoutCountdown()
//    } else {
//      val remaining = MAX_FAILED_ATTEMPTS - failedAttempts
//      val scorePercent = (maxScoreInAttempt * 100).toInt()
//      fvController.updateMsg("ไม่สำเร็จ (ความแม่นยำ $scorePercent%), เหลือโอกาสอีก $remaining ครั้ง")
//    }
//  }
//
//  private fun startLockoutCountdown() {
//    lockoutJob?.cancel()
//    lockoutJob = viewModelScope.launch {
//      lockoutCountdown.value = LOCKOUT_DURATION_SECONDS
//      tickerFlow(LOCKOUT_DURATION_SECONDS)
//        .onCompletion {
//          if (isLockedOut.value) {
//            resetLockout()
//          }
//        }
//        .collect { remainingSeconds ->
//          lockoutCountdown.value = remainingSeconds
//        }
//    }
//  }
//
//  private fun tickerFlow(periodSeconds: Int) = flow {
//    for (i in periodSeconds downTo 1) {
//      emit(i)
//      delay(1000)
//    }
//  }
//
//  fun resetLockout() {
//    lockoutJob?.cancel()
//    isLockedOut.value = false
//    failedAttempts = 0
//    lockoutCountdown.value = 0
//    fvController.updateMsg("ระบบพร้อมใช้งาน")
//    toggleVerify()
//  }
//
//  fun initialize() {
//    if (isInitialized) return
//    val appContext = getApplication<Application>().applicationContext
//    viewModelScope.launch {
//      try {
//        System.setProperty("jna.nosys", "true")
//        fvController.sys_init(appContext)
//        isInitialized = true
//      } catch (e: Throwable) {
//        fvController.updateMsg("เกิดข้อผิดพลาดร้ายแรงในการเริ่มต้น: ${e.message}")
//      }
//    }
//  }
//
//  fun reloadAllBiometrics() {
//    if (!isInitialized) return
//    fvController.userHandler.loadUser()
//  }
//
//  fun enroll(uid: String, uname: String = "") {
//    if (!isInitialized) return
//    fvController.fv_enroll(uid, uname)
//  }
//
//  fun toggleVerify() {
//    if (isVerifying.value) {
//      stopVerification(isSuccess = false)
//    } else {
//      startVerification()
//    }
//  }
//
//  private fun startVerification() {
//    if (isLockedOut.value) {
//      fvController.updateMsg("ระบบถูกล็อกอยู่")
//      return
//    }
//    failedAttempts = 0
//    maxScoreInAttempt = 0.0
//    isFingerCurrentlyDown = false
//    fvController.updateMsg("กรุณาวางนิ้วเพื่อยืนยันตัวตน")
//
//    isVerifying.value = true
//    fvController.fv_verify(true)
//  }
//
//  private fun stopVerification(isSuccess: Boolean) {
//    if (!isVerifying.value) return
//    fvController.fv_verify(false)
//    isVerifying.value = false
//    if (!isSuccess) {
//      fvController.updateMsg("หยุดการยืนยันตัวตน")
//    }
//  }
//
//  fun deleteUser(uid: String) {
//    if (!isInitialized || uid.isBlank()) return
//    fvController.fv_del(uid)
//  }
//
//  fun clearAllUsers() {
//    if (!isInitialized) return
//    fvController.fv_clear()
//  }
//
//  fun clearLastEnrolledTemplate() {
//    fvController.clearLastEnrolledTemplate()
//  }
//
//  override fun onCleared() {
//    super.onCleared()
//    lockoutJob?.cancel()
//    verificationJob?.cancel()
//    if (isInitialized) {
//      fvController.fv_exit()
//    }
//  }
//}

package com.thanes.wardstock.data.viewModel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.utils.EnrollmentResult
import com.thanes.wardstock.utils.FingerVien
import com.thanes.wardstock.utils.parseErrorMessage
import com.thanes.wardstock.utils.parseExceptionMessage
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
  val logMessages = mutableStateListOf<String>()
  val customMessage: State<String> = fvController.customMessage
  val isEnrolling = fvController.isEnrolling
  val isVerifying = fvController.isVerifying
  val lastEnrolledTemplate = mutableStateOf<String?>(null)
  val verifiedUid = fvController.verifiedUid
  val tag = "FingerVeinViewModel"

  // Lockout Logic
  private val MAX_FAILED_ATTEMPTS = 5
  private val LOCKOUT_DURATION_SECONDS = 30
  private var failedAttempts = 0
  val isLockedOut = mutableStateOf(false)
  val lockoutCountdown = mutableIntStateOf(0)
  private var lockoutJob: Job? = null
  private var isFingerCurrentlyDown = false
  private var maxScoreInAttempt = 0.0
  private val VERIFICATION_THRESHOLD = 0.75

  init {
    setupFingerVeinCallbacks()
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun setupFingerVeinCallbacks() {
    fvController.onRawLog = { rawLog ->
      val translatedLog = translateLogMessage(rawLog)
      addLog(translatedLog)
    }
    fvController.onEnrollmentProgress = { step, totalSteps ->
      if (step < totalSteps) {
        fvController.customMessage.value =
          "สำเร็จ! กรุณายกนิ้วขึ้นแล้ววางอีกครั้ง (${step}/${totalSteps})"
      } else {
        fvController.customMessage.value = "ยอดเยี่ยม! กำลังสร้างข้อมูลลายนิ้วมือ..."
      }
    }

    fvController.onEnrollmentComplete = { result: EnrollmentResult ->
      isEnrolling.value = false
      if (result.isSuccess) {
        lastEnrolledTemplate.value = result.featureData
        fvController.customMessage.value = "สแกนลายนิ้วมือสำเร็จ!"
      } else {
        lastEnrolledTemplate.value = null
        fvController.customMessage.value =
          "เกิดข้อผิดพลาด: ${result.errorMessage ?: "ไม่ทราบสาเหตุ"}"
      }
    }

    fvController.onVerificationResult = { isSuccess, uid, uname ->
      if (isSuccess) {
        handleVerificationSuccess(uid, uname)
      }
    }

    fvController.onFingerStatusChanged = { isFingerDown ->
      if (isEnrolling.value) {
        handleEnrollFingerStatus(isFingerDown)
      } else if (isVerifying.value && !isLockedOut.value) {
        handleVerifyFingerStatus(isFingerDown)
      }
    }

    fvController.onVerificationScoreUpdated = { score ->
      if (isFingerCurrentlyDown && score > maxScoreInAttempt) {
        maxScoreInAttempt = score
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun addLog(log: String) {
    if (logMessages.size > 100) logMessages.removeLast()
    logMessages.add(0, log)
  }

  private fun translateLogMessage(originalMsg: String): String {
    val translations = mapOf(
      "初始化..." to "กำลังเริ่มต้น...",
      "打开设备..." to "กำลังเปิดอุปกรณ์...",
      "发现1个设备" to "พบ 1 อุปกรณ์",
      "设备1打开成功" to "เปิดอุปกรณ์ 1 สำเร็จ",
      "初始化成功" to "เริ่มต้นระบบสำเร็จ",
      "认证成功" to "ยืนยันตัวตนสำเร็จ",
      "认证失败" to "ยืนยันตัวตนล้มเหลว",
      "注册成功" to "ลงทะเบียนสำเร็จ",
      "注册失败" to "ลงทะเบียนล้มเหลว",
      "请放入手指" to "กรุณาวางนิ้ว",
      "请抬起手指" to "กรุณายกนิ้วขึ้น",
      "请再次放入手指" to "กรุณาวางนิ้วอีกครั้ง",
      "图像质量差" to "คุณภาพของภาพต่ำเกินไป",
      "建模成功" to "สร้างโมเดลสำเร็จ",
      "建模失败" to "สร้างโมเดลล้มเหลว",
      "手指移动太快" to "กรุณาวางนิ้วให้นิ่ง",
      "操作超时" to "หมดเวลาการทำงาน",
      "删除成功" to "ลบสำเร็จ",
      "清空成功" to "ล้างข้อมูลสำเร็จ",
      "用户已存在" to "ผู้ใช้นี้มีอยู่แล้ว",
      "开始认证" to "เริ่มการยืนยันตัวตน",
      "停止认证" to "หยุดการยืนยันตัวตน"
    )
    translations[originalMsg]?.let { return it }
    translations.keys.forEach { key ->
      if (originalMsg.startsWith(key)) {
        return "${translations[key]}${originalMsg.substring(key.length)}"
      }
    }
    return originalMsg
  }

  private fun handleEnrollFingerStatus(isFingerDown: Boolean) {
    if (isFingerDown && !isFingerCurrentlyDown) {
      isFingerCurrentlyDown = true
      fvController.customMessage.value = "กำลังสแกน... กรุณาวางนิ้วให้นิ่ง"
    }
    if (!isFingerDown && isFingerCurrentlyDown) {
      isFingerCurrentlyDown = false
    }
  }

  private fun handleVerifyFingerStatus(isFingerDown: Boolean) {
    if (isFingerDown && !isFingerCurrentlyDown) {
      isFingerCurrentlyDown = true
      maxScoreInAttempt = 0.0
      fvController.customMessage.value = "กำลังตรวจสอบข้อมูล..."
    }
    if (!isFingerDown && isFingerCurrentlyDown) {
      isFingerCurrentlyDown = false
      viewModelScope.launch {
        delay(150)
        if (isVerifying.value) {
          if (maxScoreInAttempt < VERIFICATION_THRESHOLD) {
            handleVerificationFailure()
          }
        }
      }
    }
  }

  private fun handleVerificationSuccess(uid: String, uname: String) {
    if (!isVerifying.value) return
    stopVerification()
    failedAttempts = 0
    isLockedOut.value = false
    lockoutJob?.cancel()
    lockoutCountdown.intValue = 0
    fvController.customMessage.value = "ยืนยันตัวตนสำเร็จ: $uname"
    verifiedUid.value = uid
  }

  private fun handleVerificationFailure() {
    failedAttempts++
    if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
      isLockedOut.value = true
      fvController.customMessage.value = "ยืนยันตัวตนล้มเหลวครบ $MAX_FAILED_ATTEMPTS ครั้ง!"
      stopVerification()
      startLockoutCountdown()
    } else {
      val remaining = MAX_FAILED_ATTEMPTS - failedAttempts
      fvController.customMessage.value = "ไม่พบข้อมูล, เหลือโอกาสอีก $remaining ครั้ง"
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

  fun resetLockout() {
    lockoutJob?.cancel()
    isLockedOut.value = false
    failedAttempts = 0
    lockoutCountdown.intValue = 0
    fvController.customMessage.value = "ระบบพร้อมใช้งานอีกครั้ง"
  }

  fun initialize() {
    if (isInitialized) return
    viewModelScope.launch {
      try {
        System.setProperty("jna.nosys", "true")
        fvController.sys_init(getApplication())
        isInitialized = true
        fvController.customMessage.value = "อุปกรณ์พร้อมใช้งาน"
        reloadAllBiometrics()
      } catch (e: Throwable) {
        Log.e(tag, "เกิดข้อผิดพลาดร้ายแรงในการเริ่มต้น: ${e.message}")
        fvController.customMessage.value = "เกิดข้อผิดพลาดในการเริ่มต้นอุปกรณ์"
      }
    }
  }

  fun reloadAllBiometrics() {
    if (!isInitialized) return
    fvController.customMessage.value = "กำลังโหลดข้อมูลจากเซิร์ฟเวอร์..."
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val response = ApiRepository.getAllBiometricsFromApi()
        if (response.isSuccessful) {
          val allBiometrics = response.body()?.data ?: emptyList()
          fvController.fv_clear()
          allBiometrics.forEach { bio ->
            val featureSize = 3352
            val p = fvController.base64Decode(bio.featureData, featureSize)
            fvController.fv_load(bio.id, bio.userName, p, featureSize)
          }
          launch(Dispatchers.Main) {
            fvController.customMessage.value = "โหลดข้อมูล ${allBiometrics.size} รายการสำเร็จ"
          }
        } else {
          val errorJson = response.errorBody()?.string()
          launch(Dispatchers.Main) {
            fvController.customMessage.value =
              "โหลดข้อมูลล้มเหลว: ${parseErrorMessage(response.code(), errorJson)}"
          }
        }
      } catch (e: Exception) {
        launch(Dispatchers.Main) {
          fvController.customMessage.value = "การเชื่อมต่อล้มเหลว: ${parseExceptionMessage(e)}"
        }
      }
    }
  }

  fun startEnrollment(uid: String, uname: String) {
    if (!isInitialized || isEnrolling.value) return
    lastEnrolledTemplate.value = null
    isFingerCurrentlyDown = false
    fvController.customMessage.value = "กรุณาวางนิ้วบนเครื่องสแกน (ครั้งที่ 1/3)"
    fvController.fv_enroll(uid, uname)
  }

  fun cancelEnrollment() {
    if (isEnrolling.value) {
      fvController.fv_enroll("", "")
      fvController.customMessage.value = "ยกเลิกการลงทะเบียน"
    }
  }

  fun startVerification() {
    if (!isInitialized || isVerifying.value || isLockedOut.value) return
    failedAttempts = 0
    isFingerCurrentlyDown = false
    verifiedUid.value = ""
    fvController.customMessage.value = "กรุณาวางนิ้วเพื่อยืนยันตัวตน"
    fvController.fv_verify(true)
  }

  fun stopVerification() {
    if (isVerifying.value) {
      fvController.fv_verify(false)
      fvController.customMessage.value = "หยุดการยืนยันตัวตน"
    }
  }

  fun deleteUser(uid: String) {
    if (!isInitialized || uid.isBlank()) return
    fvController.fv_del(uid)
  }

  fun clearLastEnrolledTemplate() {
    lastEnrolledTemplate.value = null
  }

  override fun onCleared() {
    super.onCleared()
    lockoutJob?.cancel()
    if (isInitialized) fvController.fv_exit()
  }
}