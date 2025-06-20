package com.thanes.wardstock.ui.components.manage

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.NavigationItem
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import kotlinx.coroutines.delay

@Composable
fun ManageListItem(navController: NavHostController) {
  val menuItems = remember { MenuItems.getMainMenuItems() }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
  ) {
    itemsIndexed(
      items = menuItems
    ) { index, item ->
      AnimatedCardItem(index, item, menuItems) { navController.navigate(item.route) }
    }
  }
}

@Composable
fun AnimatedCardItem(
  index: Int,
  item: NavigationItem,
  menuItems: List<NavigationItem>,
  onClick: () -> Unit
) {
  var visible by remember { mutableStateOf(false) }

  val animatedScale by animateFloatAsState(
    targetValue = if (visible) 1f else 0.85f,
    animationSpec = tween(
      durationMillis = 300,
      easing = FastOutSlowInEasing
    ),
    label = "scale"
  )

  val animatedAlpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(
      durationMillis = 250,
      easing = LinearOutSlowInEasing
    ),
    label = "alpha"
  )

  val animatedOffsetY by animateIntAsState(
    targetValue = if (visible) 0 else 100,
    animationSpec = tween(
      durationMillis = 250,
      easing = FastOutSlowInEasing
    ),
    label = "offsetY"
  )

  LaunchedEffect(Unit) {
    delay(index * 80L)
    visible = true
  }

  Box(
    modifier = Modifier
      .graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
        alpha = animatedAlpha
        translationY = animatedOffsetY.toFloat()
      }
      .clickable { onClick() },
  ) {
    MenuItemRow(index, item, menuItems)
  }
}


@Composable
fun MenuItemRow(
  index: Int,
  item: NavigationItem,
  menuItems: List<NavigationItem>,
) {
  Column(
    modifier = Modifier.background(color = Colors.BlueGrey120)
  ) {
    if (index == 0) {
      HorizontalDivider(color = Colors.BlueGrey80)
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(RoundRadius.Medium))
        ) {
          Box(
            modifier = Modifier
              .matchParentSize()
              .background(Colors.BluePrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              painter = painterResource(item.iconRes),
              contentDescription = stringResource(item.titleRes),
              tint = Colors.BlueGrey80,
              modifier = Modifier
                .size(36.dp)
            )
          }
        }

        Text(
          text = stringResource(item.titleRes),
          fontWeight = FontWeight.Medium,
          fontSize = 18.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.width(450.dp)
        )
      }

      Icon(
        painter = painterResource(R.drawable.chevron_right_24px),
        contentDescription = "Details",
        tint = Colors.BlueGrey40,
        modifier = Modifier
          .padding(start = 8.dp)
          .align(Alignment.CenterVertically)
          .size(36.dp)
      )
    }

    if (index == menuItems.lastIndex) {
      HorizontalDivider(color = Colors.BlueGrey80)
    } else {
      HorizontalDivider(color = Colors.BlueGrey80, modifier = Modifier.padding(start = 85.dp))
    }
  }
}
