package com.thanes.wardstock.screens.setting.language

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.data.language.LanguageManager
import com.thanes.wardstock.data.models.LanguageModel
import com.thanes.wardstock.ui.components.LanguageSwitcher
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LanguageSwitcher(context: Context) {
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

  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(
        shape = RoundedCornerShape(RoundRadius.Large),
        color = Colors.BlueGrey80.copy(alpha = 0.7f),
        modifier = Modifier
          .size(42.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.language_24px),
          contentDescription = "NextOpen",
          tint = Colors.BluePrimary,
          modifier = Modifier
            .size(14.dp)
            .padding(8.dp)
        )
      }
      Spacer(modifier = Modifier.width(20.dp))
      Text(
        stringResource(R.string.change_language),
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
      )
    }
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

fun triggerAppRecomposition(context: Context) {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && context is android.app.Activity) {
    CoroutineScope(Dispatchers.Main).launch {
      val savedLanguage = LanguageManager.getInstance().getSavedLanguage(context)

      val intent = android.content.Intent("LANGUAGE_CHANGED")
      intent.putExtra("language", savedLanguage)
      context.sendBroadcast(intent)

      context.recreate()
    }
  }
}
