package com.thanes.wardstock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.LanguageModel
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.utils.getLocalizedLanguageName

@Composable
fun LanguageSwitcher(
  currentLanguage: String,
  availableLanguages: List<LanguageModel>,
  onLanguageSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var expanded by remember { mutableStateOf(false) }

  val selected = remember(currentLanguage, availableLanguages) {
    availableLanguages.firstOrNull { it.code == currentLanguage }
      ?: availableLanguages.first()
  }

  Box(
    modifier = modifier
      .wrapContentSize(Alignment.TopEnd)
      .clip(RoundedCornerShape(RoundRadius.Medium))
  ) {
    Row(
      modifier = Modifier
        .height(40.dp)
        .clip(RoundedCornerShape(RoundRadius.Medium))
        .background(Colors.BlueGrey80.copy(alpha = 0.5f))
        .clickable { expanded = !expanded }
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = getLocalizedLanguageName(context, selected.code),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
      )
      Spacer(Modifier.width(4.dp))
      Icon(
        painter = painterResource(
          if (expanded) R.drawable.keyboard_arrow_up_24px
          else R.drawable.keyboard_arrow_down_24px
        ),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .background(Colors.BlueGrey100)
        .clip(RoundedCornerShape(RoundRadius.Medium)),
      properties = PopupProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true
      )
    ) {
      availableLanguages.forEach { lang ->
        DropdownMenuItem(
          text = {
            Text(
              text = getLocalizedLanguageName(context, lang.code),
              style = MaterialTheme.typography.bodyMedium,
              color = if (lang.code == selected.code)
                MaterialTheme.colorScheme.primary
              else
                MaterialTheme.colorScheme.onSurface
            )
          },
          onClick = {
            if (lang.code != currentLanguage) {
              onLanguageSelected(lang.code)
            }
            expanded = false
          }
        )
      }
    }
  }
}
