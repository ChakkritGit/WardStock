package com.thanes.wardstock.services.jna

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.thanes.wardstock.utils.FingerVien

open class FingerVeinLib {
  private var appContext: Context? = null
  protected val context: Context
    get() = appContext
      ?: throw IllegalStateException("FingerVeinLib not initialized. Call sys_init first.")

  private var manager: UsbManager? = null
  private var attachedDevCnt = 0
  private var detachedDevCnt = 0
  private val initDevice = mutableListOf<UsbDevice>()
  private val attachedDevice = mutableListOf<UsbDevice>()
  private val usbConnection = mutableListOf<UsbDeviceConnection>()
  protected var isInit = false

  companion object {
    private const val ACTION_USB_PERMISSION =
      "com.thanes.wardstock.USB_PERMISSION"
    private const val VID = 6353
    private const val PID = 48059
  }

  private val mUsbReceiver = object : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onReceive(c: Context, intent: Intent) {
      val action = intent.action
      val device: UsbDevice? =
        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)

      when (action) {
        ACTION_USB_PERMISSION -> {
          synchronized(this) {
            try {
              if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                handlePermissionGranted(device)
              } else {
                (this@FingerVeinLib as? FingerVien)?.updateMsg("ผู้ใช้ปฏิเสธการอนุญาต")
              }
            } catch (e: Exception) {
              (this@FingerVeinLib as? FingerVien)?.updateMsg("เกิดข้อผิดพลาดในการขออนุญาต USB: ${e.message}")
            }
          }
        }

        UsbManager.ACTION_USB_DEVICE_ATTACHED -> handleDeviceAttached(device)
        UsbManager.ACTION_USB_DEVICE_DETACHED -> handleDeviceDetached(device)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun handlePermissionGranted(device: UsbDevice?) {
    device?.let { grantedDevice ->
      val it = attachedDevice.iterator()
      while (it.hasNext()) {
        if (it.next() == grantedDevice) {
          initDevice.add(grantedDevice)
          it.remove()
          break
        }
      }
    }
    fv_init()
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun handleDeviceAttached(device: UsbDevice?) {
    device?.let {
      if (VID == it.vendorId && PID == it.productId) {
        attachedDevCnt++
        (this as? FingerVien)?.updateMsg("พบอุปกรณ์ USB, กำลังขออนุญาต... ($attachedDevCnt)")
        attachedDevice.add(it)
        requestPermission(it)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun handleDeviceDetached(device: UsbDevice?) {
    device?.let {
      if (VID == it.vendorId && PID == it.productId) {
        (this as? FingerVien)?.updateMsg("อุปกรณ์ USB ถูกถอดออก")
        detachedDevCnt++
        if (detachedDevCnt >= attachedDevCnt) {
          if (isInit) {
            LibFvHelper.INSTANCE.fv_exit()
          }
          closeConnection()
        }
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun requestPermission(device: UsbDevice) {
    try {
      val flags =
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
      val intent = Intent(ACTION_USB_PERMISSION).apply { putExtra(UsbManager.EXTRA_DEVICE, device) }
      val permissionIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
      manager?.requestPermission(device, permissionIntent)
      Log.d("debug", "requestPermission")
    } catch (e: Exception) {
      (this as? FingerVien)?.updateMsg("เกิดข้อผิดพลาดในการขออนุญาต: ${e.message}")
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  fun requestAllPermissions() {
    manager?.deviceList?.values?.forEach { device ->
      if (VID == device.vendorId && PID == device.productId) {
        attachedDevCnt++
        if (manager?.hasPermission(device) == true) {
          initDevice.add(device)
        } else {
          attachedDevice.add(device)
          requestPermission(device)
        }
      }
    }
    fv_init()
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  open fun sys_init(applicationContext: Context) {
    this.appContext = applicationContext
    manager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager

    val filter = IntentFilter(ACTION_USB_PERMISSION).apply {
      addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
      addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
    }
    ContextCompat.registerReceiver(
      context,
      mUsbReceiver,
      filter,
      ContextCompat.RECEIVER_NOT_EXPORTED
    )
    requestAllPermissions()
  }

  private fun closeConnection() {
    usbConnection.forEach { it.close() }
    usbConnection.clear()
    initDevice.clear()
    attachedDevice.clear()
    attachedDevCnt = 0
    detachedDevCnt = 0
    isInit = false
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private fun fv_init() {
    if (initDevice.size < attachedDevCnt || attachedDevCnt <= 0) return

    detachedDevCnt = 0
    val fd = IntArray(10)
    var idx = 0
    initDevice.forEach { device ->
      val usbInterface = device.getInterface(0)
      val connection = manager?.openDevice(device)
      if (connection != null && connection.claimInterface(usbInterface, true)) {
        fd[idx++] = connection.fileDescriptor
        usbConnection.add(connection)
      } else {
        connection?.close()
        (this as? FingerVien)?.updateMsg("ไม่พบอุปกรณ์ USB")
      }
    }

    if (idx == 0) return

    val pFd = Memory((idx * 4).toLong())
    pFd.write(0, fd, 0, idx)

    val myFv = (this as? FingerVien)
    if (LibFvHelper.INSTANCE.fv_init_ex(
        myFv?.cbLog,
        myFv?.cbGrab,
        myFv?.cbFingerstatus,
        cbReboot,
        pFd,
        idx
      ) == 0
    ) {
      isInit = true
      myFv?.userHandler?.loadUser()
      if (myFv?.isVerifying?.value == true) {
        fv_verify(true)
      }
    }
  }

  fun fv_exit() {
    try {
      appContext?.unregisterReceiver(mUsbReceiver)
      if (isInit) {
        LibFvHelper.INSTANCE.fv_exit()
      }
      closeConnection()
      appContext = null
    } catch (e: Exception) {
      Log.e("FingerVeinLib", "Error on exit: ${e.message}")
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  protected fun checkUser(uid: String): Boolean {
    if (uid.isBlank()) {
      (this as? FingerVien)?.updateMsg("ID ผู้ใช้ว่างเปล่า กรุณากรอก ID ผู้ใช้")
      return false
    }
    return true
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  open fun fv_enroll(uid: String, uname: String): Int {
    if (!checkUser(uid)) return -1
    val myFv = (this as? FingerVien)
    return LibFvHelper.INSTANCE.fv_enroll(uid, uname, myFv?.cbEnrollFeature, myFv?.cbEnrollImg)
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  open fun fv_verify(start: Boolean) {
    (this as? FingerVien)?.updateMsg(if (start) "เริ่มการยืนยันตัวตน" else "หยุดการยืนยันตัวตน")
    LibFvHelper.INSTANCE.fv_verify(if (start) 1 else 0, (this as? FingerVien)?.cbVerify)
  }

  fun fv_del(uid: String) {
    val ret = LibFvHelper.INSTANCE.fv_del(uid)
    if (ret == 0) {
      (this as? FingerVien)?.userHandler?.delUser(uid)
    }
  }

  fun fv_clear() {
    val ret = LibFvHelper.INSTANCE.fv_del("")
    if (ret == 0) {
      (this as? FingerVien)?.userHandler?.clearUser()
    }
  }

  fun fv_load(uid: String, uname: String, feature: Pointer, size: Int) {
    LibFvHelper.INSTANCE.fv_load(uid, uname, feature, size)
  }

  interface LibFvHelper : Library {
    companion object {
      val INSTANCE: LibFvHelper by lazy {
        Native.load("fv", LibFvHelper::class.java) as LibFvHelper
      }
    }

    fun fv_init_ex(
      CB_LOG: CbLogImpl?,
      CB_GRAB: CbGrabImpl?,
      CB_STATUS: CbFingerstatusImpl?,
      cbReboot: CbRebootImpl?,
      fd: Pointer,
      dev_cnt: Int
    ): Int

    fun fv_exit(): Int
    fun fv_load(uid: String, uname: String, feature: Pointer, size: Int): Int
    fun fv_del(uid: String): Int
    fun fv_enroll(
      uid: String,
      uname: String,
      CB_FEATURE: CbEnrollFeatureImpl?,
      CB_IMG: CbEnrollImgImpl?
    ): Int

    fun fv_verify(start: Int, CB_VERIFY: CbVerifyImpl?): Int
    fun fv_syn_verify(
      featureData: String,
      featureLen: Int,
      uid: String,
      uname: String,
      threshold: Double,
      score: Pointer
    ): Int

    fun interface CbLogImpl : Callback {
      fun invoke(log_level: Int, log_str: String, bufLen: Int)
    }

    fun interface CbGrabImpl : Callback {
      fun invoke(imgBuf: Pointer, bufLen: Int)
    }

    fun interface CbFingerstatusImpl : Callback {
      fun invoke(status: Int)
    }

    fun interface CbEnrollFeatureImpl : Callback {
      fun invoke(uid: String, uname: String, buf: Pointer, bufLen: Int)
    }

    fun interface CbEnrollImgImpl : Callback {
      fun invoke(uid: String, uname: String, buf: Pointer, bufLen: Int)
    }

    fun interface CbVerifyImpl : Callback {
      fun invoke(
        uid: String,
        uname: String,
        feature: Pointer,
        featureLen: Int,
        img: Pointer,
        imgLen: Int
      )
    }

    fun interface CbRebootImpl : Callback {
      fun invoke()
    }
  }

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  private val cbReboot = LibFvHelper.CbRebootImpl {
    closeConnection()
    requestAllPermissions()
  }

  open inner class User {
    open fun loadUser() {}
    open fun addUser(uid: String, uname: String, buf: Pointer, bufLen: Int) {}
    open fun delUser(uid: String) {}
    open fun clearUser() {}

    fun base64Encode(feature: Pointer, size: Int): String {
      val bytes = feature.getByteArray(0, size.toLong().toInt())
      return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun base64Decode(str: String, size: Int): Pointer {
      val bytes = Base64.decode(str, Base64.DEFAULT)
      val feature = Memory(size.toLong())
      feature.write(0, bytes, 0, bytes.size)
      return feature
    }
  }
}