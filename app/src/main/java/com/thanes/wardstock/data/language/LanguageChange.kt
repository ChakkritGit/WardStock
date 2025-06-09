package com.thanes.wardstock.data.language

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.*
import androidx.core.content.edit

class LanguageManager {
  companion object {
    private const val LANGUAGE_PREFS = "language_prefs"
    private const val LANGUAGE_KEY = "selected_language"
    private const val DEFAULT_LANGUAGE = "en"

    @Volatile
    private var INSTANCE: LanguageManager? = null

    fun getInstance(): LanguageManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: LanguageManager().also { INSTANCE = it }
      }
    }
  }

  private fun getPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
  }

  fun changeLanguage(context: Context, languageCode: String) {
    saveLanguagePreference(context, languageCode)

    applyLanguageChange(context, languageCode)
  }

  private fun saveLanguagePreference(context: Context, languageCode: String) {
    getPreferences(context).edit {
      putString(LANGUAGE_KEY, languageCode)
    }
  }

  fun getSavedLanguage(context: Context): String {
    return getPreferences(context).getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
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
    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    if (context is android.app.Activity) {
      context.recreate()
    }
  }

  fun initializeLanguage(context: Context) {
    val savedLanguage = getSavedLanguage(context)
    applyLanguageChange(context, savedLanguage)
  }

  fun applyLanguageToActivity(activity: android.app.Activity) {
    val savedLanguage = getSavedLanguage(activity)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      val locale = Locale(savedLanguage)
      Locale.setDefault(locale)

      val config = Configuration()
      config.setLocale(locale)

      activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }
  }

  fun isLanguageSet(context: Context, languageCode: String): Boolean {
    return getSavedLanguage(context) == languageCode
  }
}