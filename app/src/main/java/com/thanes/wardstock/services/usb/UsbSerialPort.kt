package com.thanes.wardstock.services.usb

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import android.util.Log
import kotlinx.coroutines.*
import java.io.*

class SerialPortManager private constructor(context: Context) {
  private val appContext: Context = context.applicationContext
  private var inputStreamS1: InputStream? = null
  private var outputStreamS1: OutputStream? = null

  private var inputStreamS2: InputStream? = null
  private var outputStreamS2: OutputStream? = null

  private var jobReaderS1: Job? = null
  private var jobReaderS2: Job? = null

  @Volatile
  private var isConnected = false

  private val prefs: SharedPreferences =
    appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

  companion object {
    private var INSTANCE: SerialPortManager? = null

    fun getInstance(context: Context): SerialPortManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: SerialPortManager(context).also { INSTANCE = it }
      }
    }

    private const val PREF_RUNNING = "running_counter"
    private const val PREF_NAME = "vending_prefs"
    private const val TAG = "SerialPortManager"
    private const val TTY_S1 = "/dev/ttyS1"
    private const val TTY_S2 = "/dev/ttyS2"
  }

  private fun setSerialConfig(devicePath: String, baudRate: Int) {
    val cmd = arrayOf(
      "su", "-c",
      "stty -F $devicePath $baudRate cs8 -cstopb -parenb -ixon"
    )
    try {
      val process = Runtime.getRuntime().exec(cmd)
      val exitCode = process.waitFor()
      if (exitCode != 0) {
        val errorMsg = process.errorStream.bufferedReader().readText().trim()
        Log.e(TAG, "Failed to configure $devicePath with stty, exit=$exitCode. Error: $errorMsg")
      } else {
        Log.d(TAG, "Configured $devicePath to $baudRate")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error configuring serial port: ${e.message}")
    }
  }

  fun connect(baudRateS1: Int = 57600, baudRateS2: Int = 9600): Boolean {
    if (isConnected) return true

    try {
      setSerialConfig(TTY_S1, baudRateS1)
      setSerialConfig(TTY_S2, baudRateS2)

      val s1File = File(TTY_S1)
      val s2File = File(TTY_S2)

      if (!s1File.canRead() || !s1File.canWrite() || !s2File.canRead() || !s2File.canWrite()) {
        Log.e(TAG, "Permission denied on serial ports")
        return false
      }

      inputStreamS1 = FileInputStream(s1File)
      outputStreamS1 = FileOutputStream(s1File)

      inputStreamS2 = FileInputStream(s2File)
      outputStreamS2 = FileOutputStream(s2File)

      isConnected = true
      Log.d(TAG, "✅ Connected to ttyS1 and ttyS2")
      return true
    } catch (e: Exception) {
      Log.e(TAG, "❌ Error connecting serial ports: ${e.message}")
      disconnectPorts()
      return false
    }
  }

  fun writeSerialttyS1Raw(cmdBytes: ByteArray): Boolean {
    return try {
      outputStreamS1?.write(cmdBytes)
      outputStreamS1?.flush()
//      Log.d(TAG, "Sent to ttyS1: ${cmdBytes.joinToString(",") { "%02x".format(it) }}")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error writing to ttyS1: ${e.message}")
      false
    }
  }

  fun writeSerialttyS1Ack(): Boolean {
    val commands = mutableListOf(0xfa, 0xfb, 0x42, 0x00, 0x43)
    val cmdBytes = commands.map { it.toByte() }.toByteArray()

    return try {
      outputStreamS1?.write(cmdBytes)
      outputStreamS1?.flush()
//      Log.d(TAG, "Sent to ttyS1: ${cmdBytes.joinToString(",") { "%02x".format(it) }}")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error writing to ttyS1: ${e.message}")
      false
    }
  }

  fun createSerialttyS1Command(
    slot: Int,
    communicationNumber: Int,
    enableDropSensor: Boolean,
    enableElevator: Boolean
  ): List<Int> {
    val packetBytes = mutableListOf<Int>()

    packetBytes.add(0xFA)
    packetBytes.add(0xFB)

    packetBytes.add(0x06)

    packetBytes.add(0x05)

    if (communicationNumber < 1 || communicationNumber > 255) {
      throw IllegalArgumentException("Communication number must be between 1 and 255.")
    }
    packetBytes.add(communicationNumber)

    packetBytes.add(if (enableDropSensor) 0x01 else 0x00)

    packetBytes.add(if (enableElevator) 0x01 else 0x00)

    if (slot < 0 || slot > 65535) {
      throw IllegalArgumentException("Slot number is out of valid range for 2 bytes.")
    }
    val selectionHighByte = (slot shr 8) and 0xFF
    val selectionLowByte = slot and 0xFF
    packetBytes.add(selectionHighByte)
    packetBytes.add(selectionLowByte)

    var checksum = 0
    for (byteValue in packetBytes) {
      checksum = checksum xor byteValue
    }
    packetBytes.add(checksum and 0xFF)

    return packetBytes
  }

  fun writeSerialttyS2(command: String): Boolean {
    if (outputStreamS2 == null) {
      Log.e(TAG, "Cannot write to ttyS2: Port not connected")
      return false
    }

    return try {
      outputStreamS2?.write(command.toByteArray(Charsets.US_ASCII))
      outputStreamS2?.flush()
//      Log.d(TAG, "Sent to ttyS2: $command")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error writing to ttyS2: ${e.message}")
      false
    }
  }

  fun startReadingSerialttyS1(onDataReceived: (ByteArray) -> Unit): Boolean {
    if (inputStreamS1 == null) {
      Log.e(TAG, "Cannot read from ttyS1: Port not connected")
      return false
    }

    jobReaderS1?.cancel()
    jobReaderS1 = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
      val buffer = ByteArray(256)
      while (isActive) {
        try {
          val len = inputStreamS1?.read(buffer) ?: -1
          if (len > 0) {
            onDataReceived(buffer.copyOf(len))
          }
        } catch (e: Exception) {
          Log.e(TAG, "Read error on ttyS1: ${e.message}")
          break
        }
      }
    }
    return true
  }

  fun startReadingSerialttyS2(onDataReceived: (ByteArray) -> Unit): Boolean {
    if (inputStreamS2 == null) {
      Log.e(TAG, "Cannot read from ttyS2: Port not connected")
      return false
    }

    jobReaderS2?.cancel()
    jobReaderS2 = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
      val buffer = ByteArray(256)
      while (isActive) {
        try {
          val len = inputStreamS2?.read(buffer) ?: -1
          if (len > 0) {
            onDataReceived(buffer.copyOf(len))
          }
        } catch (e: Exception) {
          Log.e(TAG, "Read error on ttyS2: ${e.message}")
          break
        }
      }
    }
    return true
  }

  fun stopReadingSerialttyS1() {
    jobReaderS1?.cancel()
    jobReaderS1 = null
  }

  fun stopReadingSerialttyS2() {
    jobReaderS2?.cancel()
    jobReaderS2 = null
  }

  fun disconnectPorts() {
    try {
      jobReaderS1?.cancel()
      jobReaderS2?.cancel()

      inputStreamS1?.close()
      outputStreamS1?.close()
      inputStreamS2?.close()
      outputStreamS2?.close()

      inputStreamS1 = null
      outputStreamS1 = null
      inputStreamS2 = null
      outputStreamS2 = null

      isConnected = false
      Log.d(TAG, "Disconnected serial ports")
    } catch (e: Exception) {
      Log.e(TAG, "Error disconnecting ports: ${e.message}")
    }
  }

  fun resetRunning() {
    saveRunning(1)
  }

  fun getRunning(): Int {
    return prefs.getInt(PREF_RUNNING, 1)
  }

  fun saveRunning(value: Int) {
    prefs.edit { putInt(PREF_RUNNING, value) }
  }

  fun isConnected(): Boolean = isConnected

  @Deprecated("Use startReadingSerialttyS1 instead")
  fun readSerialttyS1(onDataReceived: (ByteArray) -> Unit): Boolean {
    return startReadingSerialttyS1(onDataReceived)
  }

  @Deprecated("Use startReadingSerialttyS2 instead")
  fun readSerialttyS2(onDataReceived: (ByteArray) -> Unit): Boolean {
    return startReadingSerialttyS2(onDataReceived)
  }
}

//package com.thanes.wardstock.services.usb
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.util.Log
//import androidx.core.content.edit
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.InputStream
//import java.io.OutputStream
//
//class SerialPortManager private constructor(context: Context) {
//  private val appContext: Context = context.applicationContext
//  private var inputStreamS1: InputStream? = null
//  private var outputStreamS1: OutputStream? = null
//
//  private var inputStreamS2: InputStream? = null
//  private var outputStreamS2: OutputStream? = null
//
//  private var jobReaderS1: Job? = null
//  private var jobReaderS2: Job? = null
//
//  @Volatile
//  private var isConnected = false
//
//  private val prefs: SharedPreferences =
//    appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//
//  companion object {
//    private var INSTANCE: SerialPortManager? = null
//
//    fun getInstance(context: Context): SerialPortManager {
//      return INSTANCE ?: synchronized(this) {
//        INSTANCE ?: SerialPortManager(context).also { INSTANCE = it }
//      }
//    }
//
//    private const val PREF_RUNNING = "running_counter"
//    private const val PREF_NAME = "vending_prefs"
//    private const val TAG = "SerialPortManager"
//    private const val TTY_S1 = "/dev/ttyS1"
//    private const val TTY_S2 = "/dev/ttyS2"
//
//    private const val DEBUG = true
//  }
//
//  private fun setSerialConfig(devicePath: String, baudRate: Int): Boolean {
//    val cmd = arrayOf(
//      "su", "-c",
//      "stty -F $devicePath $baudRate cs8 -cstopb -parenb -ixon"
//    )
//    try {
//      val process = Runtime.getRuntime().exec(cmd)
//      val exitCode = process.waitFor()
//      if (exitCode != 0) {
//        val errorMsg = process.errorStream.bufferedReader().readText().trim()
//        Log.e(TAG, "Failed to configure $devicePath with stty, exit=$exitCode. Error: $errorMsg")
//        return false
//      } else {
//        Log.d(TAG, "Successfully configured $devicePath to $baudRate baud")
//        return true
//      }
//    } catch (e: Exception) {
//      Log.e(TAG, "Exception while running stty for $devicePath: ${e.message}")
//      return false
//    }
//  }
//
//  fun connect(baudRateS1: Int = 57600, baudRateS2: Int = 9600): Boolean {
//    if (isConnected) return true
//
//    if (!setSerialConfig(TTY_S1, baudRateS1) || !setSerialConfig(TTY_S2, baudRateS2)) {
//      Log.e(TAG, "Aborting connection due to serial port configuration failure.")
//      return false
//    }
//
//    try {
//      val s1File = File(TTY_S1)
//      val s2File = File(TTY_S2)
//
//      if (!s1File.canRead() || !s1File.canWrite() || !s2File.canRead() || !s2File.canWrite()) {
//        Log.e(TAG, "Permission denied on serial ports. Check root access and file permissions.")
//        return false
//      }
//
//      inputStreamS1 = FileInputStream(s1File)
//      outputStreamS1 = FileOutputStream(s1File)
//      inputStreamS2 = FileInputStream(s2File)
//      outputStreamS2 = FileOutputStream(s2File)
//
//      isConnected = true
//      Log.d(TAG, "✅ Connected to ttyS1 and ttyS2 successfully.")
//      return true
//    } catch (e: Exception) {
//      Log.e(TAG, "❌ Error connecting to serial port files: ${e.message}")
//      disconnectPorts()
//      return false
//    }
//  }
//
//  fun writeSerialttyS1Raw(cmdBytes: ByteArray): Boolean {
//    return try {
//      if (DEBUG) {
//        Log.d(TAG, "ttyS1 >>> ${cmdBytes.joinToString(",") { "%02x".format(it) }}")
//      }
//      outputStreamS1?.write(cmdBytes)
//      outputStreamS1?.flush()
//      true
//    } catch (e: Exception) {
//      Log.e(TAG, "Error writing raw data to ttyS1: ${e.message}")
//      false
//    }
//  }
//
//  fun writeSerialttyS1Ack(): Boolean {
//    val ackBytes =
//      byteArrayOf(0xfa.toByte(), 0xfb.toByte(), 0x42.toByte(), 0x00.toByte(), 0x43.toByte())
//    return writeSerialttyS1Raw(ackBytes)
//  }
//
//  private fun calculateMdbChecksum(bytes: List<Int>): Int {
//    var checksum = 0
//    for (byteValue in bytes) {
//      checksum = checksum xor byteValue
//    }
//    return checksum and 0xFF
//  }
//
//  fun createSerialttyS1Command(
//    slot: Int, communicationNumber: Int, enableDropSensor: Boolean, enableElevator: Boolean
//  ): List<Int> {
//    val packetBytes = mutableListOf<Int>()
//    packetBytes.add(0xFA)
//    packetBytes.add(0xFB)
//    packetBytes.add(0x06)
//    packetBytes.add(0x05)
//    packetBytes.add(communicationNumber)
//    packetBytes.add(if (enableDropSensor) 0x01 else 0x00)
//    packetBytes.add(if (enableElevator) 0x01 else 0x00)
//
//    val selectionHighByte = (slot shr 8) and 0xFF
//    val selectionLowByte = slot and 0xFF
//    packetBytes.add(selectionHighByte)
//    packetBytes.add(selectionLowByte)
//
//    val checksum = calculateMdbChecksum(packetBytes)
//    packetBytes.add(checksum)
//
//    return packetBytes
//  }
//
//  fun writeSerialttyS2(command: String): Boolean {
//    if (outputStreamS2 == null) {
//      Log.e(TAG, "Cannot write to ttyS2: Port not connected")
//      return false
//    }
//    return try {
//      if (DEBUG) {
//        Log.d(TAG, "ttyS2 >>> $command")
//      }
//      outputStreamS2?.write(command.toByteArray(Charsets.US_ASCII))
//      outputStreamS2?.flush()
//      true
//    } catch (e: Exception) {
//      Log.e(TAG, "Error writing to ttyS2: ${e.message}")
//      false
//    }
//  }
//
//  fun startReadingSerialttyS1(onDataReceived: (ByteArray) -> Unit): Boolean {
//    if (inputStreamS1 == null) return false
//    jobReaderS1?.cancel()
//    jobReaderS1 = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//      val buffer = ByteArray(256)
//      while (isActive) {
//        try {
//          val len = inputStreamS1?.read(buffer) ?: -1
//          if (len > 0) {
//            val receivedData = buffer.copyOf(len)
//            if (DEBUG) {
//              Log.d(TAG, "ttyS1 <<< ${receivedData.joinToString(",") { "%02x".format(it) }}")
//            }
//            onDataReceived(receivedData)
//          }
//        } catch (e: IOException) {
//          Log.e(TAG, "Read error on ttyS1, port disconnected: ${e.message}")
//          break
//        }
//      }
//    }
//    return true
//  }
//
//  fun startReadingSerialttyS2(onDataReceived: (ByteArray) -> Unit): Boolean {
//    if (inputStreamS2 == null) return false
//    jobReaderS2?.cancel()
//    jobReaderS2 = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//      val buffer = ByteArray(256)
//      while (isActive) {
//        try {
//          val len = inputStreamS2?.read(buffer) ?: -1
//          if (len > 0) {
//            val receivedData = buffer.copyOf(len)
//            if (DEBUG) {
//              Log.d(TAG, "ttyS2 <<< (hex): ${receivedData.joinToString(",") { "%02x".format(it) }}")
//            }
//            onDataReceived(receivedData)
//          }
//        } catch (e: IOException) {
//          Log.e(TAG, "Read error on ttyS2, port disconnected: ${e.message}")
//          break
//        }
//      }
//    }
//    return true
//  }
//
//  fun stopReadingSerialttyS1() {
//    jobReaderS1?.cancel()
//    jobReaderS1 = null
//  }
//
//  fun stopReadingSerialttyS2() {
//    jobReaderS2?.cancel()
//    jobReaderS2 = null
//  }
//
//  fun disconnectPorts() {
//    try {
//      stopReadingSerialttyS1()
//      stopReadingSerialttyS2()
//      Thread.sleep(50)
//
//      inputStreamS1?.close()
//      outputStreamS1?.close()
//      inputStreamS2?.close()
//      outputStreamS2?.close()
//
//      isConnected = false
//      Log.d(TAG, "Disconnected all serial ports.")
//    } catch (e: Exception) {
//      Log.e(TAG, "Error during port disconnection: ${e.message}")
//    }
//  }
//
//  fun resetRunning() = saveRunning(1)
//  fun getRunning(): Int = prefs.getInt(PREF_RUNNING, 1)
//  fun saveRunning(value: Int) = prefs.edit { putInt(PREF_RUNNING, value) }
//  fun isConnected(): Boolean = isConnected
//}