package com.thanes.wardstock.data.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanes.wardstock.data.models.MachineStatus
import com.thanes.wardstock.services.usb.SerialPortManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class MachineStatusViewModel(application: Application) : AndroidViewModel(application) {

  private val serialPortManager = SerialPortManager.getInstance(application)

  private val _machineStatus = MutableStateFlow(MachineStatus())
  val machineStatus = _machineStatus.asStateFlow()

  private var statusPollingJob: Job? = null

  companion object {
    private const val TAG = "MachineStatusVM"
    private const val CMD_REQUEST_STATUS = 0x51
    private const val RSP_MACHINE_STATUS = 0x52
    private const val TEMP_START_INDEX = 28
    private const val HUMIDITY_START_INDEX = 36
  }

  fun startPollingMachineStatus(intervalMillis: Long = 15000) {
    stopPollingMachineStatus()

    startListeningForStatus()

    statusPollingJob = viewModelScope.launch {
      while (true) {
        requestMachineStatus()
        delay(intervalMillis)
      }
    }
    Log.d(TAG, "Started polling machine status every $intervalMillis ms.")
  }

  fun stopPollingMachineStatus() {
    statusPollingJob?.cancel()
    serialPortManager.stopReadingSerialttyS1()
    Log.d(TAG, "Stopped polling machine status.")
  }

  private fun startListeningForStatus() {
    serialPortManager.startReadingSerialttyS1 { data ->
      if (data.size > 2 && data[2].toInt() and 0xFF == RSP_MACHINE_STATUS) {
        parseMachineStatusResponse(data)
      }
    }
  }

  private fun requestMachineStatus() {
    val commNo = serialPortManager.getRunning() + 1
    val packet = mutableListOf(
      0xFA, 0xFB,
      CMD_REQUEST_STATUS,
      0x01,
      commNo
    )
    var checksum = 0
    for (byteValue in packet) {
      checksum = checksum xor byteValue
    }
    packet.add(checksum and 0xFF)

    val cmdBytes = packet.map { it.toByte() }.toByteArray()
    serialPortManager.writeSerialttyS1Raw(cmdBytes)
    serialPortManager.saveRunning(commNo)
    Log.d(TAG, "Requested machine status (0x51)")
  }

  private fun parseMachineStatusResponse(data: ByteArray) {
    if (data.size < HUMIDITY_START_INDEX + 8) {
      Log.w(TAG, "Received status packet is too short. Size: ${data.size}")
      return
    }

    try {
      val tempBytes = data.sliceArray(TEMP_START_INDEX until TEMP_START_INDEX + 8)
      val tempString = String(tempBytes, StandardCharsets.US_ASCII).trim('\u0000', ' ')

      val humidityBytes = data.sliceArray(HUMIDITY_START_INDEX until HUMIDITY_START_INDEX + 8)
      val humidityString = String(humidityBytes, StandardCharsets.US_ASCII).trim('\u0000', ' ')

      viewModelScope.launch {
        val currentStatus = _machineStatus.value
        val newStatus = currentStatus.copy(
          temperature = if (tempString.isNotEmpty()) "$tempString °C" else currentStatus.temperature,
          humidity = if (humidityString.isNotEmpty()) "$humidityString %" else currentStatus.humidity,
          isTempOk = true,
          isHumidityOk = true,
          lastUpdated = System.currentTimeMillis()
        )
        _machineStatus.value = newStatus
        Log.d(TAG, "Updated Status: Temp=$tempString, Humidity=$humidityString")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing machine status response: ${e.message}")
    }
  }

  override fun onCleared() {
    super.onCleared()
    stopPollingMachineStatus()
  }
}