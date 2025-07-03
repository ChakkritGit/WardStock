package com.thanes.wardstock.utils

import android.content.Context
import com.thanes.wardstock.R

fun getLocalizedLanguageName(context: Context, code: String): String {
  return when (code) {
    "en" -> context.getString(R.string.lang_english)
    "th" -> context.getString(R.string.lang_thai)
    else -> code
  }
}