package com.thanes.wardstock.data.language

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.thanes.wardstock.data.store.LanguageDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageManager {
  companion object {
    @Volatile
    private var INSTANCE: LanguageManager? = null

    fun getInstance(): LanguageManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: LanguageManager().also { INSTANCE = it }
      }
    }
  }

  fun changeLanguage(context: Context, languageCode: String) {
    CoroutineScope(Dispatchers.IO).launch {
      LanguageDataStore.saveLanguage(context, languageCode)
      applyLanguageChange(context, languageCode)
    }
  }

  suspend fun getSavedLanguage(context: Context): String {
    return LanguageDataStore.getSavedLanguage(context)
  }

  private fun applyLanguageChange(context: Context, languageCode: String) {
    when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
          LocaleList.forLanguageTags(languageCode)
      }
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
      }
      else -> {
        setLegacyLocale(context, languageCode)
      }
    }
  }

  private fun setLegacyLocale(context: Context, languageCode: String) {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    if (context is android.app.Activity) {
      context.recreate()
    }
  }

  fun initializeLanguage(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
      val savedLanguage = getSavedLanguage(context)
      applyLanguageChange(context, savedLanguage)
    }
  }

  fun applyLanguageToActivity(activity: android.app.Activity) {
    CoroutineScope(Dispatchers.IO).launch {
      val savedLanguage = getSavedLanguage(activity)

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        val locale = Locale.forLanguageTag(savedLanguage)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
      }
    }
  }

  suspend fun isLanguageSet(context: Context, languageCode: String): Boolean {
    return getSavedLanguage(context) == languageCode
  }
}