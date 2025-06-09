package com.thanes.wardstock.screens.setting

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.ui.components.appbar.AppBar
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.R
import com.thanes.wardstock.data.language.LanguageManager
import com.thanes.wardstock.data.models.LanguageModel
import com.thanes.wardstock.ui.components.LanguageSwitcher
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(navController: NavHostController, context: Context) {
  val scope = rememberCoroutineScope()
  val languageManager = remember { LanguageManager.getInstance() }

  val allLanguages = listOf(
    LanguageModel("th", "ไทย"),
    LanguageModel("en", "English"),
  )

  var currentLanguage by remember { mutableStateOf("") }
  var shouldRecompose by remember { mutableStateOf(false) }


  LaunchedEffect(Unit) {
    currentLanguage = languageManager.getSavedLanguage(context)
  }

  LaunchedEffect(shouldRecompose) {
    if (shouldRecompose) {
      kotlinx.coroutines.delay(100)
      shouldRecompose = false
    }
  }

  Scaffold(
    topBar = {
      AppBar(
        title = stringResource(R.string.settings),
        onBack = { navController.popBackStack() }
      )
    },
    containerColor = Colors.BlueGrey100
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding)) {
      Column(modifier = Modifier.padding(8.dp)) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            stringResource(R.string.change_language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
          )
          if (currentLanguage.isNotEmpty()) {
            LanguageSwitcher(
              languagesList = allLanguages,
              currentLanguage = currentLanguage,
              onCurrentLanguageChange = { newLanguage ->
                scope.launch {
                  languageManager.changeLanguage(context, newLanguage)

                  currentLanguage = newLanguage

                  shouldRecompose = true

                  triggerAppRecomposition(context)
                }
              },
              modifier = Modifier.padding(16.dp)
            )
          }
        }
      }
    }
  }
}

private fun triggerAppRecomposition(context: Context) {
  if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
    if (context is android.app.Activity) {
      val intent = android.content.Intent("LANGUAGE_CHANGED")
      intent.putExtra("language", LanguageManager.getInstance().getSavedLanguage(context))
      context.sendBroadcast(intent)

      context.recreate()
    }
  }
}