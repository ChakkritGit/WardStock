package com.thanes.wardstock.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.sun.jna.Pointer
import com.thanes.wardstock.data.repositories.ApiRepository
import com.thanes.wardstock.services.jna.FingerVeinLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FingerVien : FingerVeinLib() {
  val imageBitmap = mutableStateOf<Bitmap?>(null)
  val logMessages = mutableStateListOf<String>()
  val isEnrolling = mutableStateOf(false)
  val isVerifying = mutableStateOf(false)
  val verifiedUid = mutableStateOf("")
  val verifiedUsername = mutableStateOf("")
  val lastEnrolledTemplate: MutableState<String?> = mutableStateOf(null)

  private var isLastVerify = false

  val userHandler: User

  init {
    userHandler = UserApiHandler()
  }

  var onVerificationResult: ((isSuccess: Boolean) -> Unit)? = null
  var onFingerStatusChanged: ((isFingerDown: Boolean) -> Unit)? = null
  var onVerificationScoreUpdated: ((score: Double) -> Unit)? = null

  override fun sys_init(applicationContext: Context) {
    super.sys_init(applicationContext)
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  fun updateMsg(msg: String) {
    if (logMessages.size > 100) {
      logMessages.removeLast()
    }
    logMessages.add(0, msg)
  }

  private fun showFvImg(imgBuf: Pointer, bufLen: Int) {
    try {
      val bytes = imgBuf.getByteArray(0, bufLen)
      val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
      imageBitmap.value = bitmap
    } catch (e: Exception) {
      Log.e("showFvImg", "Error decoding bitmap", e)
    }
  }

  override fun fv_enroll(uid: String, uname: String): Int {
    return if (!isEnrolling.value) {
      if (super.fv_enroll(uid, uname) == 0) {
        isEnrolling.value = true
        isLastVerify = isVerifying.value
        isVerifying.value = false
        0
      } else -1
    } else {
      if (LibFvHelper.INSTANCE.fv_enroll("", "", null, null) == 0) {
        isEnrolling.value = false
        if (isLastVerify) fv_verify(true)
        0
      } else -1
    }
  }

  override fun fv_verify(start: Boolean) {
    isVerifying.value = start
    super.fv_verify(start)
  }

  private fun translateMessage(originalMsg: String): String {
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

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbLog = LibFvHelper.CbLogImpl { _, logStr, _ ->
    val trimmedLog = logStr.trim()
    val scorePrefix = "认证:"

    if (trimmedLog.startsWith(scorePrefix)) {
      try {
        val scoreString = trimmedLog.substring(scorePrefix.length)
        val score = scoreString.toDouble()
        onVerificationScoreUpdated?.invoke(score)

      } catch (e: NumberFormatException) {
        Log.e("FingerVein", e.message.toString())
        CoroutineScope(Dispatchers.Main).launch {
          updateMsg(translateMessage(trimmedLog))
        }
      }
    } else {
      CoroutineScope(Dispatchers.Main).launch {
        val translated = translateMessage(trimmedLog)
        updateMsg(translated)
      }
    }
  }

  val cbGrab = LibFvHelper.CbGrabImpl { imgBuf, bufLen ->
    CoroutineScope(Dispatchers.Main).launch {
      showFvImg(imgBuf, bufLen)
    }
  }

  val cbFingerstatus = LibFvHelper.CbFingerstatusImpl { }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbEnrollFeature = LibFvHelper.CbEnrollFeatureImpl { uid, uname, buf, bufLen ->
    CoroutineScope(Dispatchers.Main).launch {
      if (bufLen > 0) {
        lastEnrolledTemplate.value = userHandler.base64Encode(buf, bufLen)
        userHandler.addUser(uid, uname, buf, bufLen)
        if (isLastVerify) {
          fv_verify(true)
        }
        updateMsg("ลงทะเบียนสำเร็จ! กรุณากด 'ปิด' เพื่อดำเนินการต่อ")
      } else {
        updateMsg("ลงทะเบียนล้มเหลว")
        lastEnrolledTemplate.value = null
      }
      isEnrolling.value = false
    }
  }

  fun clearLastEnrolledTemplate() {
    lastEnrolledTemplate.value = null
  }

  val cbEnrollImg = LibFvHelper.CbEnrollImgImpl { _, _, _, _ -> }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbVerify = LibFvHelper.CbVerifyImpl { uid, uname, _, _, _, _ ->
    CoroutineScope(Dispatchers.Main).launch {
      val success = uid.isNotEmpty()
      val message = if (success) {
        val displayName = uname.ifEmpty { "ผู้ใช้ไม่ระบุชื่อ" }
        "${translateMessage("认证成功")}: $displayName (ID: $uid)"
      } else {
        translateMessage("认证失败")
      }
      updateMsg(message)
      verifiedUid.value = if (success) uid else ""
      verifiedUsername.value = if (success) uname else ""
    }
  }

  inner class UserApiHandler : User() {
    private val apiScope = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun loadUser() {
      apiScope.launch {
        try {
          val response = ApiRepository.getUserConfig()
          if (response.isSuccessful) {
            val allBiometrics = response.body()?.data ?: emptyList()
            var loadCount = 0
            allBiometrics.forEach { bioData ->
              try {
                val featureSize = 3352
                val featurePointer = base64Decode(bioData.featureData, featureSize)
                fv_load(bioData.id, bioData.userName, featurePointer, featureSize)
                loadCount++
              } catch (e: Exception) {
                Log.e("UserApiHandler",e.message.toString())
                CoroutineScope(Dispatchers.Main).launch {
                  updateMsg("ประมวลผลข้อมูลของ ${bioData.id} ล้มเหลว")
                }
              }
            }
            CoroutineScope(Dispatchers.Main).launch {
              updateMsg("โหลดข้อมูล $loadCount รายการสำเร็จ! พร้อมยืนยันตัวตน")
            }
          } else {
            val errorJson = response.errorBody()?.string()
            CoroutineScope(Dispatchers.Main).launch {
              updateMsg("โหลดข้อมูลล้มเหลว: ${response.code()} ${parseErrorMessage(response.code(), errorJson)}")
            }
          }
        } catch (e: Exception) {
          CoroutineScope(Dispatchers.Main).launch {
            updateMsg("การเชื่อมต่อล้มเหลว: ${parseExceptionMessage(e)}")
          }
        }
      }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun addUser(uid: String, uname: String, buf: Pointer, bufLen: Int) {
      fv_load(uid, uname, buf, bufLen)
      CoroutineScope(Dispatchers.Main).launch {
        updateMsg("เพิ่ม '$uname' เข้าสู่ Cache สำหรับการยืนยันตัวตน")
      }
    }

    override fun delUser(uid: String) {
    }

    override fun clearUser() {
    }
  }
}