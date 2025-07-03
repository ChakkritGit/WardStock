package com.thanes.wardstock.data.language

import android.content.Context
import com.thanes.wardstock.data.store.LanguageDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    }
  }

  suspend fun getSavedLanguage(context: Context): String {
    return LanguageDataStore.getSavedLanguage(context)
  }

  suspend fun isLanguageSet(context: Context, languageCode: String): Boolean {
    return getSavedLanguage(context) == languageCode
  }
}