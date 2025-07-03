package com.thanes.wardstock

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thanes.wardstock.services.machine.Dispense
import com.thanes.wardstock.services.rabbit.RabbitMQService
import com.thanes.wardstock.services.usb.SerialPortManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class App : Application() {

  private val applicationScope = CoroutineScope(Dispatchers.IO)

  private var _dispenseService: Dispense? = null
  val dispenseService: Dispense? get() = _dispenseService

  var isInitialized by mutableStateOf(false)
    private set

  override fun onCreate() {
    super.onCreate()

    initializeServices()
  }

  override fun attachBaseContext(base: Context) {
    val context = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      updateContextLocale(base)
    } else {
      base
    }
    super.attachBaseContext(context)
  }

  private fun updateContextLocale(context: Context): Context {
    val fallbackLanguage = context
      .getSharedPreferences("language_prefs", MODE_PRIVATE)
      .getString("selected_language", "en") ?: "en"

    val locale = Locale.forLanguageTag(fallbackLanguage)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    return context.createConfigurationContext(config)
  }

  private fun initializeServices() {
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

  override fun onTerminate() {
    super.onTerminate()
    applicationScope.launch {
      try {
        val serialPortManager = SerialPortManager.getInstance(this@App)
        serialPortManager.disconnectPorts()
        RabbitMQService.getInstance().disconnect()
      } catch (e: Exception) {
        Log.e("App", "Error during cleanup: ${e.message}")
      }
    }
  }
}