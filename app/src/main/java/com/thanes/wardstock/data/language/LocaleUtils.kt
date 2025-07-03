package com.thanes.wardstock.data.language

import android.content.Context
import android.content.res.Configuration
import java.util.*

fun Context.withLocale(locale: Locale): Context {
  val config = Configuration(resources.configuration)
  config.setLocale(locale)
  return createConfigurationContext(config)
}