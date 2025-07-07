package com.thanes.wardstock.ui.components.report

import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.NavigationItem
import com.thanes.wardstock.navigation.Routes

class ReportItems {
  companion object {
    fun getReportItems(): List<NavigationItem> {
      return listOf(
        NavigationItem(
          iconRes = R.drawable.picture_as_pdf_24px,
          titleRes = R.string.min_max,
          route = Routes.ReportDrugMinMax.route
        ),
      )
    }
  }
}