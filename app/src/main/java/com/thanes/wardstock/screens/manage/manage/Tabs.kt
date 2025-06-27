package com.thanes.wardstock.screens.manage.manage

import androidx.annotation.StringRes
import com.thanes.wardstock.R
import kotlin.enums.enumEntries

enum class Tabs(val route: String, @StringRes val labelRes: Int) {
  Inventory("tab_inventory", R.string.tab_inventory),
  Group("tab_group", R.string.tab_group);

  companion object {
    val entries = enumEntries<Tabs>()
  }
}