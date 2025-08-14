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
//import java.io.FileDescriptor
//import java.io.FileInputStream
//import java.io.FileOutputStream
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
//  private var fileDescriptorS1: FileDescriptor? = null
//  private var fileDescriptorS2: FileDescriptor? = null
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
//  }
//
//  fun connect(baudRateS1: Int = 57600, baudRateS2: Int = 9600): Boolean {
//    if (isConnected) return true
//    try {
//      fileDescriptorS1 = SerialPortJNI.openPort(TTY_S1, baudRateS1)
//      if (fileDescriptorS1 == null) {
//        throw Exception("JNI failed to open ttyS1")
//      }
//      inputStreamS1 = FileInputStream(fileDescriptorS1)
//      outputStreamS1 = FileOutputStream(fileDescriptorS1)
//
//      fileDescriptorS2 = SerialPortJNI.openPort(TTY_S2, baudRateS2)
//      if (fileDescriptorS2 == null) {
//        throw Exception("JNI failed to open ttyS2")
//      }
//      inputStreamS2 = FileInputStream(fileDescriptorS2)
//      outputStreamS2 = FileOutputStream(fileDescriptorS2)
//
//      isConnected = true
//      return true
//    } catch (e: Exception) {
//      Log.e(TAG, "❌ Error opening serial port files: ${e.message}")
//      disconnectPorts()
//      return false
//    }
//  }
//
//  fun writeSerialttyS1Raw(cmdBytes: ByteArray): Boolean {
//    return try {
//      outputStreamS1?.write(cmdBytes)
//      outputStreamS1?.flush()
////      Log.d(TAG, "Sent to ttyS1: ${cmdBytes.joinToString(",") { "%02x".format(it) }}")
//      true
//    } catch (e: Exception) {
//      Log.e(TAG, "Error writing to ttyS1: ${e.message}")
//      false
//    }
//  }
//
//  fun writeSerialttyS1Ack(): Boolean {
//    val commands = mutableListOf(0xfa, 0xfb, 0x42, 0x00, 0x43)
//    val cmdBytes = commands.map { it.toByte() }.toByteArray()
//
//    return try {
//      outputStreamS1?.write(cmdBytes)
//      outputStreamS1?.flush()
////      Log.d(TAG, "Sent to ttyS1: ${cmdBytes.joinToString(",") { "%02x".format(it) }}")
//      true
//    } catch (e: Exception) {
//      Log.e(TAG, "Error writing to ttyS1: ${e.message}")
//      false
//    }
//  }
//
//  fun createSerialttyS1Command(
//    slot: Int,
//    communicationNumber: Int,
//    enableDropSensor: Boolean,
//    enableElevator: Boolean
//  ): List<Int> {
//    val packetBytes = mutableListOf<Int>()
//
//    packetBytes.add(0xFA)
//    packetBytes.add(0xFB)
//
//    packetBytes.add(0x06)
//
//    packetBytes.add(0x05)
//
//    if (communicationNumber < 1 || communicationNumber > 255) {
//      throw IllegalArgumentException("Communication number must be between 1 and 255.")
//    }
//    packetBytes.add(communicationNumber)
//
//    packetBytes.add(if (enableDropSensor) 0x01 else 0x00)
//
//    packetBytes.add(if (enableElevator) 0x01 else 0x00)
//
//    if (slot < 0 || slot > 65535) {
//      throw IllegalArgumentException("Slot number is out of valid range for 2 bytes.")
//    }
//    val selectionHighByte = (slot shr 8) and 0xFF
//    val selectionLowByte = slot and 0xFF
//    packetBytes.add(selectionHighByte)
//    packetBytes.add(selectionLowByte)
//
//    var checksum = 0
//    for (byteValue in packetBytes) {
//      checksum = checksum xor byteValue
//    }
//    packetBytes.add(checksum and 0xFF)
//
//    return packetBytes
//  }
//
//  fun writeSerialttyS2(command: String): Boolean {
//    if (outputStreamS2 == null) {
//      Log.e(TAG, "Cannot write to ttyS2: Port not connected")
//      return false
//    }
//
//    return try {
//      outputStreamS2?.write(command.toByteArray(Charsets.US_ASCII))
//      outputStreamS2?.flush()
////      Log.d(TAG, "Sent to ttyS2: $command")
//      true
//    } catch (e: Exception) {
//      Log.e(TAG, "Error writing to ttyS2: ${e.message}")
//      false
//    }
//  }
//
//  fun startReadingSerialttyS1(onDataReceived: (ByteArray) -> Unit): Boolean {
//    if (inputStreamS1 == null) {
//      Log.e(TAG, "Cannot read from ttyS1: Port not connected")
//      return false
//    }
//
//    jobReaderS1?.cancel()
//    jobReaderS1 = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//      val buffer = ByteArray(256)
//      while (isActive) {
//        try {
//          val len = inputStreamS1?.read(buffer) ?: -1
//          if (len > 0) {
//            onDataReceived(buffer.copyOf(len))
//          }
//        } catch (e: Exception) {
//          Log.e(TAG, "Read error on ttyS1: ${e.message}")
//          break
//        }
//      }
//    }
//    return true
//  }
//
//  fun startReadingSerialttyS2(onDataReceived: (ByteArray) -> Unit): Boolean {
//    if (inputStreamS2 == null) {
//      Log.e(TAG, "Cannot read from ttyS2: Port not connected")
//      return false
//    }
//
//    jobReaderS2?.cancel()
//    jobReaderS2 = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//      val buffer = ByteArray(256)
//      while (isActive) {
//        try {
//          val len = inputStreamS2?.read(buffer) ?: -1
//          if (len > 0) {
//            onDataReceived(buffer.copyOf(len))
//          }
//        } catch (e: Exception) {
//          Log.e(TAG, "Read error on ttyS2: ${e.message}")
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
//      jobReaderS1?.cancel()
//      jobReaderS2?.cancel()
//
//      SerialPortJNI.closePortFromFileDescriptor(fileDescriptorS1)
//      SerialPortJNI.closePortFromFileDescriptor(fileDescriptorS2)
//
//      inputStreamS1?.close()
//      outputStreamS1?.close()
//      inputStreamS2?.close()
//      outputStreamS2?.close()
//
//      inputStreamS1 = null
//      outputStreamS1 = null
//      inputStreamS2 = null
//      outputStreamS2 = null
//
//      isConnected = false
//      Log.d(TAG, "Disconnected serial ports")
//    } catch (e: Exception) {
//      Log.e(TAG, "Error disconnecting ports: ${e.message}")
//    }
//  }
//
//  fun resetRunning() {
//    saveRunning(1)
//  }
//
//  fun getRunning(): Int {
//    return prefs.getInt(PREF_RUNNING, 1)
//  }
//
//  fun saveRunning(value: Int) {
//    prefs.edit { putInt(PREF_RUNNING, value) }
//  }
//
//  fun isConnected(): Boolean = isConnected
//
//  @Deprecated("Use startReadingSerialttyS1 instead")
//  fun readSerialttyS1(onDataReceived: (ByteArray) -> Unit): Boolean {
//    return startReadingSerialttyS1(onDataReceived)
//  }
//
//  @Deprecated("Use startReadingSerialttyS2 instead")
//  fun readSerialttyS2(onDataReceived: (ByteArray) -> Unit): Boolean {
//    return startReadingSerialttyS2(onDataReceived)
//  }
//}

package com.thanes.wardstock.services.usb

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SerialPortManager private constructor(context: Context) {
  private val appContext: Context = context.applicationContext

  private var fdS1: Int = -1
  private var fdS2: Int = -1

  private var jobReaderS1: Job? = null
  private var jobReaderS2: Job? = null

  @Volatile
  private var isConnected = false

  private val prefs: SharedPreferences =
    appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

  companion object {
    @Volatile
    private var INSTANCE: SerialPortManager? = null

    fun getInstance(context: Context): SerialPortManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: SerialPortManager(context.applicationContext).also { INSTANCE = it }
      }
    }

    private const val PREF_RUNNING = "running_counter"
    private const val PREF_NAME = "vending_prefs"
    private const val TAG = "SerialPortManager"
    private const val TTY_S1 = "/dev/ttyS1"
    private const val TTY_S2 = "/dev/ttyS2"
  }

  fun connect(baudRateS1: Int = 57600, baudRateS2: Int = 9600): Boolean {
    if (isConnected) return true

    synchronized(this) {
      try {
        grantPermissions()

        fdS1 = SerialPortJNI.openPort(TTY_S1, baudRateS1)
        fdS2 = SerialPortJNI.openPort(TTY_S2, baudRateS2)

        if (fdS1 < 0 || fdS2 < 0) {
          throw Exception("JNI failed to open one or more ports. S1_fd=$fdS1, S2_fd=$fdS2")
        }

        isConnected = true
        Log.i(TAG, "✅ Serial ports connected successfully via JNI.")
        return true
      } catch (e: Exception) {
        Log.e(TAG, "❌ Failed to connect serial ports: ${e.message}", e)
        disconnectPorts()
        return false
      }
    }
  }

  fun disconnectPorts() {
    Log.d(TAG, "Disconnecting serial ports...")
    try {
      jobReaderS1?.cancel()
      jobReaderS2?.cancel()

      if (fdS1 >= 0) SerialPortJNI.closePort(fdS1)
      if (fdS2 >= 0) SerialPortJNI.closePort(fdS2)

    } catch (e: Exception) {
      Log.e(TAG, "Error during port disconnection: ${e.message}")
    } finally {
      fdS1 = -1
      fdS2 = -1
      jobReaderS1 = null
      jobReaderS2 = null
      isConnected = false
      Log.d(TAG, "All serial port resources have been released.")
    }
  }

  private fun grantPermissions() {
    Log.d(TAG, "Attempting to grant permissions to serial ports...")
    try {
      val command = "chmod 666 $TTY_S1 $TTY_S2"
      val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
      val exitCode = process.waitFor()
      if (exitCode != 0) {
        val errorMsg = process.errorStream.bufferedReader().readText().trim()
        Log.w(TAG, "chmod command failed with exit code $exitCode. Error: $errorMsg")
      } else {
        Log.i(TAG, "Permissions granted to serial ports via 'su'.")
      }
    } catch (e: Exception) {
      Log.e(
        TAG,
        "Failed to grant permissions. This might be okay on some devices. Error: ${e.message}"
      )
    }
  }

  fun writeSerialttyS1Raw(cmdBytes: ByteArray): Boolean {
    if (!isConnected || fdS1 < 0) {
      Log.e(TAG, "Cannot write to ttyS1: Port not connected or invalid FD.")
      return false
    }
    val bytesWritten = SerialPortJNI.writeBytes(fdS1, cmdBytes)
    return if (bytesWritten == cmdBytes.size) {
      true
    } else {
      Log.e(TAG, "JNI write to ttyS1 failed. Expected ${cmdBytes.size} but wrote $bytesWritten")
      false
    }
  }

  fun writeSerialttyS2(command: String): Boolean {
    if (!isConnected || fdS2 < 0) {
      Log.e(TAG, "Cannot write to ttyS2: Port not connected or invalid FD.")
      return false
    }
    val cmdBytes = command.toByteArray(Charsets.US_ASCII)
    val bytesWritten = SerialPortJNI.writeBytes(fdS2, cmdBytes)
    return if (bytesWritten == cmdBytes.size) {
      true
    } else {
      Log.e(TAG, "JNI write to ttyS2 failed. Expected ${cmdBytes.size} but wrote $bytesWritten")
      false
    }
  }

  fun startReadingSerialttyS1(onDataReceived: (ByteArray) -> Unit): Boolean {
    if (!isConnected || fdS1 < 0) {
      Log.w(TAG, "Cannot start reading ttyS1: Not connected.")
      return false
    }
    jobReaderS1?.cancel()
    jobReaderS1 = CoroutineScope(Dispatchers.IO).launch {
      val buffer = ByteArray(1024)
      while (isActive) {
        val bytesRead = SerialPortJNI.readBytes(fdS1, buffer, buffer.size)
        if (bytesRead > 0) {
          onDataReceived(buffer.copyOf(bytesRead))
        } else if (bytesRead < 0) {
          Log.e(TAG, "Read error on ttyS1, stopping reader.")
          break
        }
        delay(20)
      }
    }
    return true
  }

  fun startReadingSerialttyS2(onDataReceived: (ByteArray) -> Unit): Boolean {
    if (!isConnected || fdS2 < 0) {
      Log.w(TAG, "Cannot start reading ttyS2: Not connected.")
      return false
    }
    jobReaderS2?.cancel()
    jobReaderS2 = CoroutineScope(Dispatchers.IO).launch {
      val buffer = ByteArray(1024)
      while (isActive) {
        val bytesRead = SerialPortJNI.readBytes(fdS2, buffer, buffer.size)
        if (bytesRead > 0) {
          onDataReceived(buffer.copyOf(bytesRead))
        } else if (bytesRead < 0) {
          Log.e(TAG, "Read error on ttyS2, stopping reader.")
          break
        }
        delay(20)
      }
    }
    return true
  }

  fun writeSerialttyS1Ack(): Boolean {
    val ackBytes =
      byteArrayOf(0xfa.toByte(), 0xfb.toByte(), 0x42.toByte(), 0x00.toByte(), 0x43.toByte())
    return writeSerialttyS1Raw(ackBytes)
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
    packetBytes.add(communicationNumber)
    packetBytes.add(if (enableDropSensor) 0x01 else 0x00)
    packetBytes.add(if (enableElevator) 0x01 else 0x00)
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

  fun resetRunning() = saveRunning(1)
  fun getRunning(): Int = prefs.getInt(PREF_RUNNING, 1)
  fun saveRunning(value: Int) = prefs.edit { putInt(PREF_RUNNING, value) }
  fun isConnected(): Boolean = isConnected
}