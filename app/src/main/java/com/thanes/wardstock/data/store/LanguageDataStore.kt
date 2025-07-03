package com.thanes.wardstock.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val LANGUAGE_PREFS = "language_prefs"
val Context.languageDataStore by preferencesDataStore(name = LANGUAGE_PREFS)

object LanguageDataStore {
  private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
  private const val DEFAULT_LANGUAGE = "en"

  suspend fun saveLanguage(context: Context, languageCode: String) {
    context.languageDataStore.edit { prefs ->
      prefs[LANGUAGE_KEY] = languageCode
    }
  }

  suspend fun getSavedLanguage(context: Context): String {
    return context.languageDataStore.data
      .map { it[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE }
      .first()
  }
}