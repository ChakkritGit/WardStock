package com.thanes.wardstock.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(
  count: Int,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.displayLarge
) {


  AnimatedContent(
    targetState = count,
    transitionSpec = {
      if (targetState > initialState) {
        (slideInVertically(
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
          )
        ) { height -> height } + fadeIn()).togetherWith(
          slideOutVertically(
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessLow
            )
          ) { height -> -height } + fadeOut())
      } else {
        (slideInVertically(
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
          )
        ) { height -> -height } + fadeIn()).togetherWith(
          slideOutVertically(
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioMediumBouncy,
              stiffness = Spring.StiffnessLow
            )
          ) { height -> height } + fadeOut())
      }
    },
    label = "AnimatedCounter"
  ) { targetCount ->
    Text(
      text = targetCount.toString(),
      style = style,
      softWrap = false,
      modifier = modifier
    )
  }
}
