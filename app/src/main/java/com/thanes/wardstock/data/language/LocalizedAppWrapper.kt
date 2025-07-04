package com.thanes.wardstock.data.language

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import com.thanes.wardstock.data.store.languageDataStore
import kotlinx.coroutines.flow.map
import java.util.Locale

@Composable
fun LocalizedAppWrapper(context: Context, content: @Composable () -> Unit) {
  val langFlow = remember(context) {
    context.languageDataStore.data.map { prefs ->
      prefs[stringPreferencesKey("selected_language")] ?: "en"
    }
  }

  val langCode by langFlow.collectAsState(initial = "en")
  val locale = remember(langCode) { Locale.forLanguageTag(langCode) }

  val localizedContext = remember(locale) {
    context.withLocale(locale)
  }

  CompositionLocalProvider(
    LocalAppLocale provides locale,
    LocalContext provides localizedContext
  ) {
    content()
  }
}
