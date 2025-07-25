package com.thanes.wardstock

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.thanes.wardstock.data.language.LocalizedAppWrapper
import com.thanes.wardstock.data.viewModel.FingerVeinViewModel
import com.thanes.wardstock.navigation.AppNavigation
import com.thanes.wardstock.ui.components.system.HideSystemControll
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.WardStockTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
  private val fingerVienViewModel: FingerVeinViewModel by viewModels()

  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    permissionRequest()

    HideSystemControll.manageSystemBars(this, hide = true)

    enableEdgeToEdge()

    val splashScreen = installSplashScreen()
    fingerVienViewModel.initialize()

    setContent {
      LocalizedAppWrapper(this, this) {
        WardStockTheme {
          val navController = rememberNavController()

          Surface(
            modifier = Modifier
              .fillMaxSize()
              .background(Colors.black),
            shape = RoundedCornerShape(RoundRadius.Large)
          ) {
            Scaffold(
              modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
              AppNavigation(
                navController = navController,
                innerPadding = innerPadding,
                splashScreen = splashScreen,
                fingerVienViewModel = fingerVienViewModel,
                context = this
              )
            }
          }
        }
      }
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
      HideSystemControll.manageSystemBars(this, hide = true)
    }
  }

  override fun onResume() {
    super.onResume()
    HideSystemControll.manageSystemBars(this, hide = true)
  }

  override fun attachBaseContext(newBase: Context) {
    val fallbackLanguage = newBase
      .getSharedPreferences("language_prefs", MODE_PRIVATE)
      .getString("selected_language", "en") ?: "en"

    val context = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      updateContextLocale(newBase, fallbackLanguage)
    } else {
      newBase
    }

    super.attachBaseContext(context)
  }

  private fun updateContextLocale(context: Context, language: String): Context {
    val locale = Locale.forLanguageTag(language)
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
