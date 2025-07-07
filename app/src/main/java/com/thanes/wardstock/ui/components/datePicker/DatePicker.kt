package com.thanes.wardstock.ui.components.datePicker

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerField(
  context: Context,
  selectedDate: LocalDate,
  onDateSelected: (LocalDate) -> Unit,
  label: Int,
  modifier: Modifier = Modifier
) {
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
  val dateString = selectedDate.format(formatter)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clickable {
        val dialog = DatePickerDialog(
          context,
          { _, year, month, dayOfMonth ->
            val newDate = LocalDate.of(year, month + 1, dayOfMonth)
            onDateSelected(newDate)
          },
          selectedDate.year,
          selectedDate.monthValue - 1,
          selectedDate.dayOfMonth
        )

        dialog.show()
      }
  ) {
    OutlinedTextField(
      value = dateString,
      onValueChange = { },
      label = { Text(stringResource(label)) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(RoundRadius.Large),
      readOnly = true,
      enabled = false,
      textStyle = TextStyle(fontSize = 20.sp),
      leadingIcon = {
        Icon(
          painter = painterResource(R.drawable.calendar_month_24px),
          contentDescription = "calendar icon",
          tint = Colors.BlueGrey40,
          modifier = Modifier.size(32.dp),
        )
      },
      colors = TextFieldDefaults.colors(
        focusedTextColor = Colors.BlueSecondary,
        focusedIndicatorColor = Colors.BlueSecondary,
        unfocusedIndicatorColor = Colors.BlueSecondary.copy(alpha = 0.3f),
        focusedLabelColor = Colors.BlueSecondary,
        unfocusedLabelColor = Colors.BlueGrey40,
        cursorColor = Colors.BlueSecondary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedLeadingIconColor = Colors.BlueSecondary,
        disabledTextColor = Colors.BlueSecondary,
        disabledLabelColor = Colors.BlueGrey40,
        disabledLeadingIconColor = Colors.BlueGrey40,
        disabledIndicatorColor = Colors.BlueSecondary.copy(alpha = 0.3f)
      )
    )
  }
}
