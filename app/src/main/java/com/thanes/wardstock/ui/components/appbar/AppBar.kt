package com.thanes.wardstock.ui.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.R
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
  title: String,
  onBack: () -> Unit
) {
  TopAppBar(
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .clip(RoundedCornerShape(RoundRadius.Large))
          .clickable(onClick = onBack)
      ) {
        Icon(
          painter = painterResource(R.drawable.arrow_back_ios_new_24px),
          contentDescription = "arrow_back_ios_new_24px",
          tint = Colors.BluePrimary,
          modifier = Modifier
            .size(44.dp)
            .padding(6.dp)
        )
        Text(
          text = title,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          fontWeight = FontWeight.Medium,
          fontFamily = ibmpiexsansthailooped,
          fontSize = 22.sp,
          modifier = Modifier
            .widthIn(max = 500.dp)
            .padding(end = 14.dp)
        )
      }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = Colors.BlueGrey100,
      titleContentColor = Colors.BluePrimary,
    ),
  )
}
