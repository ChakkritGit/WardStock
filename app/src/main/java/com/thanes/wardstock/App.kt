package com.thanes.wardstock

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.thanes.wardstock.services.machine.Dispense
import com.thanes.wardstock.services.rabbit.RabbitMQService
import com.thanes.wardstock.services.usb.SerialPortManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

  private val applicationScope = CoroutineScope(Dispatchers.IO)

  private var _dispenseService: Dispense? = null
  val dispenseService: Dispense? get() = _dispenseService

  @Volatile
  var isInitialized = false
    private set

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate() {
    super.onCreate()
    applicationScope.launch {
      try {
        val serialPortManager = SerialPortManager.getInstance(this@App)
        val isConnected = serialPortManager.connect()

        if (isConnected) {
          _dispenseService = Dispense.getInstance(serialPortManager)

          RabbitMQService.getInstance().connect()
          RabbitMQService.getInstance().listenToQueue("vdOrder")

          isInitialized = true
          Log.d("App", "Dispense service initialized successfully")
        } else {
          Log.e("App", "Failed to connect serial ports")
        }
      } catch (e: Exception) {
        Log.e("App", "Error initializing dispense service: ${e.message}")
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onTerminate() {
    super.onTerminate()
    applicationScope.launch {
      val serialPortManager = SerialPortManager.getInstance(this@App)
      serialPortManager.disconnectPorts()
      RabbitMQService.getInstance().disconnect()
    }
  }
}