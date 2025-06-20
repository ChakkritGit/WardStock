package com.thanes.wardstock.screens.refill

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thanes.wardstock.R
import com.thanes.wardstock.data.models.RefillModel
import com.thanes.wardstock.data.viewModel.RefillViewModel
import com.thanes.wardstock.navigation.Routes
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.utils.ImageUrl
import kotlinx.coroutines.delay

@Composable
fun RefillItemCard(index: Int, item: RefillModel, filteredList: List<RefillModel>) {
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
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(96.dp)
          .clip(RoundedCornerShape(6.dp))
      ) {
        if (!item.drugImage.isNullOrBlank()) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(ImageUrl + item.drugImage)
              .crossfade(true)
              .build(),
            contentDescription = item.drugName ?: "Drug",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
          )
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
                .size(64.dp)
            )
          }
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(vertical = 1.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = item.inventoryPosition.toString(),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = item.drugName
            ?: stringResource(R.string.no_drug_name),
          fontWeight = FontWeight.Medium,
          fontSize = 18.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.width(450.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row {
          val stockQty = item.inventoryQty
          val stockMin = item.inventoryMin
          val (bg, border, text) = when {
            stockQty == 0 -> Triple(Color(0xFFFFCDD2), Color(0xFFD32F2F), Color(0xFFD32F2F))
            stockQty <= stockMin -> Triple(Color(0xFFFFF9C4), Color(0xFFFFA000), Color(0xFFFFA000))
            else -> Triple(Color(0xFFE0E0E0), Color(0xFFBDBDBD), Color.Black)
          }

          Text(
            text = "${stringResource(R.string.remaining_stock)} $stockQty",
            fontSize = 16.sp,
            color = text,
            modifier = Modifier
              .border(1.dp, border, RoundedCornerShape(RoundRadius.Medium))
              .background(bg, RoundedCornerShape(RoundRadius.Medium))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          )

          Spacer(modifier = Modifier.width(8.dp))

          val isNarcotic = item.drugPriority != 1
          val drugLabel =
            if (isNarcotic) stringResource((R.string.Narcotic_drug)) else stringResource(R.string.normal_drug)
          val drugColor = if (isNarcotic) Color(0xFFE91E63) else Color(0xFF78909C)

          Text(
            text = drugLabel,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
              .background(drugColor, RoundedCornerShape(RoundRadius.Medium))
              .padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Min ${item.inventoryMin} Max ${item.inventoryMAX}",
          fontSize = 16.sp,
          color = Colors.BlueGrey40
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

    if (index == filteredList.lastIndex) {
      HorizontalDivider(color = Colors.BlueGrey80)
    } else {
      HorizontalDivider(color = Colors.BlueGrey80, modifier = Modifier.padding(start = 125.dp))
    }
  }
}

@Composable
fun AnimatedCardItem(
  index: Int,
  item: RefillModel,
  navController: NavHostController,
  viewModel: RefillViewModel,
  filteredList: List<RefillModel>
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
      .clickable(onClick = {
        viewModel.selectDrug(item)
        navController.navigate(Routes.RefillDrug.route)
      })
  ) {
    RefillItemCard(index, item, filteredList)
  }
}