package com.thanes.wardstock.ui.components.appbar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
  title: String,
  onBack: () -> Unit
) {
  TopAppBar(
    title = {
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
        fontFamily = ibmpiexsansthailooped,
        modifier = Modifier.width(500.dp)
      )
    },
    navigationIcon = {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .size(54.dp)
          .padding(4.dp)
      ) {
        Icon(
          painter = painterResource(R.drawable.chevron_left_24px),
          contentDescription = "chevron_left_24px",
          tint = Colors.BluePrimary,
          modifier = Modifier.fillMaxSize()
        )
      }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = Colors.BlueGrey100,
      titleContentColor = Colors.BluePrimary,
    ),
  )
}
