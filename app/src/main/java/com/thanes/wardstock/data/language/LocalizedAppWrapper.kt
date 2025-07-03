package com.thanes.wardstock.data.language

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
fun LocalizedAppWrapper(content: @Composable () -> Unit) {
  val baseContext = LocalContext.current

  val langFlow = remember(baseContext) {
    baseContext.languageDataStore.data.map { prefs ->
      prefs[stringPreferencesKey("selected_language")] ?: "en"
    }
  }

  val langCode by langFlow.collectAsState(initial = "en")
  val locale = remember(langCode) { Locale.forLanguageTag(langCode) }

  val localizedContext = remember(locale) {
    baseContext.withLocale(locale)
  }

  CompositionLocalProvider(
    LocalAppLocale provides locale,
    LocalContext provides localizedContext
  ) {
    content()
  }
}
