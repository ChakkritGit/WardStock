package com.thanes.wardstock

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.thanes.wardstock.data.language.LanguageManager
import com.thanes.wardstock.navigation.AppNavigation
import com.thanes.wardstock.ui.theme.WardStockTheme
import java.util.*

class MainActivity : ComponentActivity() {

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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    LanguageManager.getInstance().applyLanguageToActivity(this)

    permissionRequest()

    enableEdgeToEdge()

    setContent {
      WardStockTheme {
        ManageSystemBars(hide = true)

        val context = LocalContext.current
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigation(navController, innerPadding, context = context)
        }
      }
    }
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

@Composable
fun ManageSystemBars(hide: Boolean) {
  val view = LocalView.current

  if (!view.isInEditMode) {
    DisposableEffect(hide) {
      val window = (view.context as? Activity)?.window
      var originalSystemUiVisibility: Int? = null
      var originalSystemBarsBehavior: Int? = null

      if (window != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          val insetsController = WindowCompat.getInsetsController(window, view)
          originalSystemBarsBehavior = insetsController.systemBarsBehavior

          if (hide) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
              WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
          } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            originalSystemBarsBehavior.let { insetsController.systemBarsBehavior = it }
          }
        } else {
          @Suppress("DEPRECATION")
          originalSystemUiVisibility = window.decorView.systemUiVisibility

          @Suppress("DEPRECATION")
          if (hide) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
          } else {
            originalSystemUiVisibility.let { window.decorView.systemUiVisibility = it }
          }
        }
      }

      onDispose {
        if (window != null && hide) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val currentController = WindowCompat.getInsetsController(window, view)
            currentController.show(WindowInsetsCompat.Type.systemBars())
            originalSystemBarsBehavior?.let { currentController.systemBarsBehavior = it }
          } else {
            @Suppress("DEPRECATION")
            originalSystemUiVisibility?.let { window.decorView.systemUiVisibility = it }
          }
        }
      }
    }
  }
}