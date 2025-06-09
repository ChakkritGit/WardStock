package com.thanes.wardstock.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanes.wardstock.data.models.LanguageModel
import com.thanes.wardstock.R

@Composable
fun LanguageSwitcher(
  languagesList: List<LanguageModel>,
  currentLanguage: String,
  onCurrentLanguageChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  var selectedItem by remember {
    mutableStateOf(
      languagesList.firstOrNull { it.code == currentLanguage }
        ?: languagesList.firstOrNull()
        ?: LanguageModel("en", "English")
    )
  }

  LaunchedEffect(currentLanguage) {
    languagesList.firstOrNull { it.code == currentLanguage }?.let {
      selectedItem = it
    }
  }

  Box(
    modifier = modifier
      .wrapContentSize(Alignment.TopEnd)
  ) {
    Row(
      modifier = Modifier
        .height(40.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
        .clickable {
          expanded = !expanded
        }
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      LanguageListItem(selectedItem = selectedItem, showName = true)

      Icon(
        painter = painterResource(if (expanded) R.drawable.keyboard_arrow_up_24px else R.drawable.keyboard_arrow_down_24px),
        contentDescription = if (expanded) "Collapse" else "Expand",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.background(
        MaterialTheme.colorScheme.surface
      )
    ) {
      languagesList.forEach { item ->
        DropdownMenuItem(
          text = {
            LanguageListItem(
              selectedItem = item,
              showName = true,
              isSelected = item.code == selectedItem.code
            )
          },
          onClick = {
            if (selectedItem.code != item.code) {
              selectedItem = item
              onCurrentLanguageChange(item.code)
            }
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
fun LanguageListItem(
  selectedItem: LanguageModel,
  showName: Boolean = true,
  isSelected: Boolean = false,
  @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    if (showName) {
      Text(
        text = selectedItem.name,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isSelected)
            MaterialTheme.colorScheme.primary
          else
            MaterialTheme.colorScheme.onSurface
        )
      )
    }
  }
}