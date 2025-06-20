package com.thanes.wardstock.data.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class NavigationItem(
  @DrawableRes val iconRes: Int,
  @StringRes val titleRes: Int,
  val route: String
)
