package com.thanes.wardstock.ui.components.system

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class HideSystemControll {
  companion object {
    fun manageSystemBars(activity: Activity, hide: Boolean) {
      val window = activity.window
      val decorView = window.decorView

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insetsController = WindowCompat.getInsetsController(window, decorView)
        if (hide) {
          insetsController.hide(WindowInsetsCompat.Type.systemBars())
          insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
          insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
      } else {
        if (hide) {
          @Suppress("DEPRECATION")
          decorView.systemUiVisibility = (
                  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                          or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                          or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                          or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                          or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                          or View.SYSTEM_UI_FLAG_FULLSCREEN
                  )
        } else {
          @Suppress("DEPRECATION")
          decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
      }
    }
  }
}