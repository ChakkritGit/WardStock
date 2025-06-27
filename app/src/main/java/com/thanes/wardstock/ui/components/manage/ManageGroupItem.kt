package com.thanes.wardstock.ui.components.manage

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.GroupInventoryModel
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.utils.ImageUrl
import kotlinx.coroutines.delay

@Composable
fun AnimatedGroupItem(
  index: Int,
  item: GroupInventoryModel,
  filteredList: List<GroupInventoryModel>,
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
    GroupItemCard(index, item, filteredList)
  }
}

@Composable
fun GroupItemCard(index: Int, item: GroupInventoryModel, filteredList: List<GroupInventoryModel>) {
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
        horizontalArrangement = Arrangement.spacedBy(24.dp)
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(RoundRadius.Medium))
        ) {
          if (item.drugimage.isNotEmpty()) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(Colors.BlueGrey40)

            ) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(ImageUrl + item.drugimage)
                  .crossfade(true)
                  .error(R.drawable.face_24px)
                  .fallback(R.drawable.face_24px)
                  .build(),
                contentDescription = item.drugname,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
              )
            }
          } else {
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(Colors.BlueGrey40),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = painterResource(R.drawable.medication_24px),
                contentDescription = "Drug Image",
                tint = Colors.BlueGrey80,
                modifier = Modifier
                  .size(48.dp)
              )
            }
          }
        }

        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            item.drugname,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(450.dp)
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              "Min ${item.groupmin}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Medium,
              color = Colors.BlueGrey40,
            )
            Text(
              "|",
              fontSize = 18.sp,
              fontWeight = FontWeight.Medium,
              color = Colors.BlueGrey40,
            )
            Text(
              "Max ${item.groupmax}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Medium,
              color = Colors.BlueGrey40,
            )
          }
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item.inventoryList.forEach { list ->
              Text(
                "${stringResource(R.string.drug_inventory_no)} ${list.inventoryPosition}",
                modifier = Modifier
                  .background(Color.Transparent, RoundedCornerShape(RoundRadius.Small))
                  .border(1.5.dp, Colors.BluePrimary, RoundedCornerShape(RoundRadius.Small))
                  .padding(horizontal = 8.dp, vertical = 1.5.dp),
                color = Colors.BluePrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
              )
            }
          }
        }
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

    if (index == filteredList.lastIndex) {
      HorizontalDivider(color = Colors.BlueGrey80)
    } else {
      HorizontalDivider(color = Colors.BlueGrey80, modifier = Modifier.padding(start = 110.dp))
    }
  }
}