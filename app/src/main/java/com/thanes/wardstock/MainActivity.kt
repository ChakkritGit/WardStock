package com.thanes.wardstock

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.thanes.wardstock.data.language.LanguageManager
import com.thanes.wardstock.navigation.AppNavigation
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.theme.WardStockTheme
import java.util.*

class MainActivity : ComponentActivity() {
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    LanguageManager.getInstance().applyLanguageToActivity(this)

    permissionRequest()

    enableEdgeToEdge()

    setContent {
      HideSystemControll.manageSystemBars(this, hide = true)

      WardStockTheme {
        val context = LocalContext.current
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigation(navController, innerPadding, context = context)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    HideSystemControll.manageSystemBars(this, hide = true)
  }

  override fun attachBaseContext(newBase: Context) {
    val languageManager = LanguageManager.getInstance()
    val context = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      updateContextLocale(newBase, languageManager.getSavedLanguage(newBase))
    } else {
      newBase
    }
    super.attachBaseContext(context)
  }

  private fun updateContextLocale(context: Context, language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    return context.createConfigurationContext(config)
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun permissionRequest() {
    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
        arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001
      )
    }
  }
}

