package com.thanes.wardstock.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanes.wardstock.data.models.LanguageModel

@Composable
fun LanguageSwitcher(
  languagesList: List<LanguageModel>,
  currentLanguage: String,
  onCurrentLanguageChange: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  var selectedItem by remember { mutableStateOf(languagesList.first { it.code == currentLanguage }) }

  Box(
    modifier = Modifier
      .padding(all = 16.dp)
      .wrapContentSize(Alignment.TopEnd)
  ) {
    Row(
      modifier = Modifier
        .height(24.dp)
        .clickable {
          expanded = !expanded
        }
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
      LanguageListItem(selectedItem)
    }

    DropdownMenu(
      expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(
        MaterialTheme.colorScheme.background
      )
    ) {
      repeat(languagesList.size) {
        val item = languagesList[it]
        DropdownMenuItem(text = {
          LanguageListItem(selectedItem = item)
        }, onClick = {
          selectedItem = item
          expanded = !expanded
          onCurrentLanguageChange(selectedItem.code)
        })
      }
    }
  }
}

@Composable
fun LanguageListItem(selectedItem: LanguageModel) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Image(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape),
      painter = painterResource(selectedItem.flag),
      contentScale = ContentScale.Crop,
      contentDescription = selectedItem.code
    )

    Text(
      modifier = Modifier.padding(start = 8.dp),
      text = selectedItem.name,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.W500,
        color = MaterialTheme.colorScheme.onBackground,
      )
    )
  }
}
