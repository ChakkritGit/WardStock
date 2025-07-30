//package com.thanes.wardstock.data.viewModel
//
//import android.app.Application
//import android.util.Log
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import com.thanes.wardstock.data.models.MachineStatus
//import com.thanes.wardstock.services.usb.SerialPortManager
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.launch
//
//class MachineStatusViewModel(application: Application) : AndroidViewModel(application) {
//
//  private val serialPortManager = SerialPortManager.getInstance(application)
//
//  val machineStatus = MutableStateFlow(MachineStatus())
//
//  private var statusPollingJob: Job? = null
//
//  companion object {
//    private const val TAG = "MachineStatusVM"
//    private const val CMD_REQUEST_STATUS = 0x51
//    private const val RSP_MACHINE_STATUS = 0x52
//
//    private const val TEMP_START_INDEX = 28
//    private const val HUMIDITY_START_INDEX = TEMP_START_INDEX + 8
//
//    private val SENSOR_NOT_READY_BYTE = 0xAA.toByte()
//  }
//
//  fun startPollingMachineStatus(intervalMillis: Long = 15000) {
//    stopPollingMachineStatus()
//    startListeningForStatus()
//    statusPollingJob = viewModelScope.launch {
//      while (true) {
//        requestMachineStatus()
//        delay(intervalMillis)
//      }
//    }
//    Log.d(TAG, "Started polling machine status every $intervalMillis ms.")
//  }
//
//  fun stopPollingMachineStatus() {
//    statusPollingJob?.cancel()
//    serialPortManager.stopReadingSerialttyS1()
//    Log.d(TAG, "Stopped polling machine status.")
//  }
//
//  private fun startListeningForStatus() {
//    serialPortManager.startReadingSerialttyS1 { data ->
//      if (data.size > 4 && data[2].toInt() and 0xFF == RSP_MACHINE_STATUS) {
//        val commNo = data.getOrNull(4)?.toInt()?.and(0xFF)
//        if (commNo != null && commNo != 0) {
//          serialPortManager.saveRunning(commNo)
//        }
//        parseMachineStatusResponse(data)
//      }
//    }
//  }
//
//  private fun requestMachineStatus() {
//    val lastKnownCommNo = serialPortManager.getRunning()
//    val nextCommNo = if (lastKnownCommNo >= 255) 1 else lastKnownCommNo + 1
//
//    val packet = mutableListOf(0xFA, 0xFB, CMD_REQUEST_STATUS, 0x01, nextCommNo)
//    var checksum = 0
//    for (byteValue in packet) {
//      checksum = checksum xor byteValue
//    }
//    packet.add(checksum and 0xFF)
//
//    val cmdBytes = packet.map { it.toByte() }.toByteArray()
//    serialPortManager.writeSerialttyS1Raw(cmdBytes)
////    Log.d(TAG, "Requested machine status (0x51) with CommNo: $nextCommNo")
//  }
//
//  private fun parseMachineStatusResponse(data: ByteArray) {
////    Log.d(TAG, "Parsing response: ${data.joinToString(" ") { "%02x".format(it) }}")
//    try {
//      var temperature = "--.-"
//      var humidity = "--"
//      var isTempOk = false
//      var isHumidityOk = false
//
//      if (data.size >= TEMP_START_INDEX + 8) {
//        val tempBytes = data.copyOfRange(TEMP_START_INDEX, TEMP_START_INDEX + 8)
//        if (tempBytes.all { it == SENSOR_NOT_READY_BYTE }) {
//          Log.w(TAG, "Temperature sensor not ready.")
//          temperature = "N/A"
//        } else {
//          temperature = try {
//            parseAsciiToDecimal(tempBytes)
//          } catch (e: Exception) {
//            Log.e(TAG, "Temperature parsing failed: ${e.message}")
//            "Error"
//          }
//          isTempOk = temperature != "Error"
//        }
//      }
//
//      if (data.size >= HUMIDITY_START_INDEX + 8) {
//        val humBytes = data.copyOfRange(HUMIDITY_START_INDEX, HUMIDITY_START_INDEX + 8)
//        if (humBytes.all { it == SENSOR_NOT_READY_BYTE }) {
//          Log.w(TAG, "Humidity sensor not ready.")
//          humidity = "N/A"
//        } else {
//          humidity = try {
//            parseAsciiToDecimal(humBytes)
//          } catch (e: Exception) {
//            Log.e(TAG, "Humidity parsing failed: ${e.message}")
//            "Error"
//          }
//          isHumidityOk = humidity != "Error"
//        }
//      }
//
//      viewModelScope.launch {
//        machineStatus.value = machineStatus.value.copy(
//          temperature = temperature,
//          humidity = humidity,
//          isTempOk = isTempOk,
//          isHumidityOk = isHumidityOk,
//          lastUpdated = System.currentTimeMillis()
//        )
//      }
//
//    } catch (e: Exception) {
//      Log.e(TAG, "Exception during parsing machine status: ${e.message}", e)
//    }
//  }
//
//  private fun parseAsciiToDecimal(bytes: ByteArray): String {
//    val str = bytes
//      .map { it.toInt().toChar() }
//      .joinToString("")
//      .trim()
//      .replace("[^0-9\\.]".toRegex(), "")
//
//    str.toFloatOrNull()?.let {
//      return String.format("%.1f", it)
//    }
//    throw IllegalArgumentException("Invalid sensor format: $str")
//  }
//
//  override fun onCleared() {
//    super.onCleared()
//    stopPollingMachineStatus()
//  }
//}
