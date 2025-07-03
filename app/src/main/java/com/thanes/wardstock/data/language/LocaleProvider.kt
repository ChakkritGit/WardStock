package com.thanes.wardstock.data.language

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

val LocalAppLocale = compositionLocalOf { Locale.getDefault() }