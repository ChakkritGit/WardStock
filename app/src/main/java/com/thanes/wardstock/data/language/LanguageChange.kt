package com.thanes.wardstock.data.language

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class LanguageChange {
  fun changeLanguage(context: Context, languageCode: String) {

    //if needed we can set in pref.


    //version >= 13
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.getSystemService(LocaleManager::class.java).applicationLocales =
        LocaleList.forLanguageTags(languageCode)
    } else {
      //version < 13
      AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }
  }

  fun getLanguageCode(context: Context,): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()?.split("-")?.first() ?: "en"
    } else {
      //version < 13
      AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")?.first() ?: "en"
    }
  }
}