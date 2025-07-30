package com.thanes.wardstock.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun ExpireText(drugExpire: String, fontSize: TextUnit = 16.sp) {
  val remainingDays = try {
    val instant = Instant.parse(drugExpire)
    val expireDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    ChronoUnit.DAYS.between(today, expireDate)
  } catch (_: Exception) {
    null
  }

  val displayText = if (remainingDays != null) {
    if (remainingDays >= 0)
      "${stringResource(R.string.expired_in)} $remainingDays ${stringResource(R.string.days)}"
    else
      stringResource(R.string.expire_date)
  } else {
    "Invalid date"
  }

  Text(
    text = displayText,
    fontSize = fontSize,
    color = Colors.BlueGrey40
  )
}