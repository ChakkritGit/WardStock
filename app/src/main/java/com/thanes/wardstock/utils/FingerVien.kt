package com.thanes.wardstock.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.sun.jna.Pointer
import com.thanes.wardstock.services.jna.FingerVeinLib
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets

class FingerVien : FingerVeinLib() {
  val imageBitmap = mutableStateOf<Bitmap?>(null)
  val logMessages = mutableStateListOf<String>()
  val isEnrolling = mutableStateOf(false)
  val isVerifying = mutableStateOf(false)
  val verifiedUid = mutableStateOf("")

  private var isLastVerify = false

  val userHandler: User

  init {
    userHandler = UserJson()
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  override fun sys_init(applicationContext: Context) {
    super.sys_init(applicationContext)
//    updateMsg("ระบบเริ่มต้น, รอการเชื่อมต่ออุปกรณ์...")
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
      } else {
        -1
      }
    } else {
      if (LibFvHelper.INSTANCE.fv_enroll("", "", null, null) == 0) {
        isEnrolling.value = false
        if (isLastVerify) {
          fv_verify(true)
        }
        0
      } else {
        -1
      }
    }
  }

  override fun fv_verify(start: Boolean) {
    isVerifying.value = start
    super.fv_verify(start)
  }

  private fun translateMessage(originalMsg: String): String {
    val translations = mapOf(
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
      "停止认证" to "หยุดการยืนยันตัวตน",
    )

    translations[originalMsg]?.let {
      return it
    }

    translations.keys.forEach { key ->
      if (originalMsg.startsWith(key)) {
        val translatedPrefix = translations[key]
        val remainingPart = originalMsg.substring(key.length)
        return "$translatedPrefix$remainingPart"
      }
    }
    return originalMsg
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbLog = LibFvHelper.CbLogImpl { _, logStr, _ ->
    val translated = translateMessage(logStr)
    updateMsg(translated)
  }

  val cbGrab = LibFvHelper.CbGrabImpl { imgBuf, bufLen ->
    showFvImg(imgBuf, bufLen)
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbFingerstatus = LibFvHelper.CbFingerstatusImpl { status ->
    updateMsg("สถานะนิ้ว: " + if (status != 0) "วาง" else "ยก")
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbEnrollFeature = LibFvHelper.CbEnrollFeatureImpl { uid, uname, buf, bufLen ->
    if (bufLen > 0) {
      userHandler.addUser(uid, uname, buf, bufLen)
      if (isLastVerify) {
        fv_verify(true)
      }
      updateMsg("ลงทะเบียนสำเร็จ, ID ผู้ใช้: $uid")
    } else {
      updateMsg("ลงทะเบียนล้มเหลว")
    }
    isEnrolling.value = false
  }

  val cbEnrollImg = LibFvHelper.CbEnrollImgImpl { _, _, _, _ ->
    // ไม่ต้องทำอะไรกับรูปภาพที่ลงทะเบียน
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  val cbVerify = LibFvHelper.CbVerifyImpl { uid, _, _, _, _, _ ->
    val success = uid.isNotEmpty()
    val message = if (success) {
      translateMessage("认证成功") + ", ID ผู้ใช้: $uid"
    } else {
      translateMessage("认证失败")
    }
    updateMsg(message)
    verifiedUid.value = if (success) uid else ""
  }

  inner class UserJson : User() {
    private var userInfo: JSONArray? = null

    private val userFileName = "user.json"

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun loadUser() {
      try {
        val file = File(context.filesDir, userFileName)
        if (!file.exists()) {
          userInfo = JSONArray("[]")
          return
        }
        if (userInfo == null) {
          val json = readFile()
          userInfo = if (json.isNullOrBlank() || json == "{}") JSONArray("[]") else JSONArray(json)
        }

        for (i in 0 until (userInfo?.length() ?: 0)) {
          val obj = userInfo!!.getJSONObject(i)
          val featureArray = obj.getJSONArray("feature_array")
          for (j in 0 until featureArray.length()) {
            val featureObj = featureArray.getJSONObject(j)
            val size = featureObj.getInt("size")
            val feature = base64Decode(featureObj.getString("feature"), size)
            fv_load(obj.getString("uid"), obj.getString("uname"), feature, size)
          }
        }
        updateMsg("โหลดข้อมูลผู้ใช้ ${userInfo?.length() ?: 0} คนเรียบร้อย")
      } catch (e: Exception) {
        updateMsg("เกิดข้อผิดพลาดในการโหลดข้อมูลผู้ใช้: ${e.message}")
        updateMsg("ไฟล์ JSON อาจไม่เข้ากัน, กำลังล้างข้อมูลและเริ่มใหม่")
        clearUser()
      }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun addUser(uid: String, uname: String, buf: Pointer, bufLen: Int) {
      try {
        var obj = findUser(uid)
        if (obj == null) {
          obj = JSONObject().apply {
            put("uid", uid)
            put("uname", uname)
            put("feature_array", JSONArray())
          }
          userInfo?.put(obj)
        }
        val featureArray = obj.getJSONArray("feature_array")
        val featureObj = JSONObject().apply {
          put("size", bufLen)
          put("feature", base64Encode(buf, bufLen))
        }
        featureArray.put(featureObj)
        writeFile(userInfo.toString())
      } catch (e: Exception) {
        updateMsg("เกิดข้อผิดพลาดในการเพิ่มผู้ใช้: ${e.message}")
      }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun delUser(uid: String) {
      try {
        userInfo?.let {
          for (i in 0 until it.length()) {
            if (uid == it.getJSONObject(i).getString("uid")) {
              it.remove(i)
              break
            }
          }
          writeFile(it.toString())
          updateMsg("ลบผู้ใช้ $uid เรียบร้อยแล้ว")
        }
      } catch (e: Exception) {
        updateMsg("เกิดข้อผิดพลาดในการลบผู้ใช้: ${e.message}")
      }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun clearUser() {
      userInfo = JSONArray()
      writeFile(userInfo.toString())
      updateMsg("ล้างข้อมูลผู้ใช้ทั้งหมดเรียบร้อยแล้ว")
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun findUser(uid: String): JSONObject? {
      try {
        userInfo?.let {
          for (i in 0 until it.length()) {
            val obj = it.getJSONObject(i)
            if (uid == obj.getString("uid")) {
              return obj
            }
          }
        }
      } catch (e: Exception) {
        updateMsg("เกิดข้อผิดพลาดในการค้นหาผู้ใช้: ${e.message}")
      }
      return null
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun readFile(): String? {
      return try {
        context.openFileInput(userFileName).use { fin ->
          fin.bufferedReader().use { it.readText() }
        }
      } catch (_: java.io.FileNotFoundException) {
        null
      } catch (e: Exception) {
        updateMsg("เกิดข้อผิดพลาดในการอ่านไฟล์: ${e.message}")
        null
      }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun writeFile(json: String) {
      try {
        context.openFileOutput(userFileName, Context.MODE_PRIVATE).use {
          it.write(json.toByteArray(StandardCharsets.UTF_8))
        }
      } catch (e: Exception) {
        updateMsg("เกิดข้อผิดพลาดในการเขียนไฟล์: ${e.message}")
      }
    }
  }
}