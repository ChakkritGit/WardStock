package com.thanes.wardstock.data.models

import androidx.annotation.DrawableRes

data class LanguageModel(
  val code: String,
  val name: String,
  @DrawableRes
  val flag: Int
)

