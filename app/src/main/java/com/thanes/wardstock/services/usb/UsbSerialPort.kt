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

//  fun createSerialttyS1Command(slot: Int): MutableList<Int> {
//    val running = getRunning()
//    val newRunning = if (running == 255) 1 else running + 1
//    saveRunning(newRunning)
//
//    val commands = mutableListOf(
//      0xfa, 0xfb, 0x06, 0x05, newRunning, 0x00, 0x00, 0x00, slot
//    )
//
//    var checksum = 0
//    for (element in commands) {
//      checksum = if (element == 0xfa) 0xfa else checksum xor element
//    }
//    commands.add(checksum)
//
//    return commands
//  }

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
