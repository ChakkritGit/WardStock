package com.thanes.wardstock.ui.components.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.RoundRadius
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped

@Composable
fun GradientButton(
  text: String? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  textSize: TextUnit? = null,
  fontWeight: FontWeight? = null,
  textColor: Color = Color.White,
  disabledTextColor: Color = Colors.BlueGrey80,
  enabled: Boolean = true,
  gradient: Brush = Brush.verticalGradient(
    colors = listOf(
      Colors.BlueSecondary,
      Colors.BluePrimary
    ),
  ),
  disabledGradient: Brush = Brush.verticalGradient(
    colors = listOf(
      Colors.BlueGrey40,
      Colors.BlueGrey40,
    )
  ),
  contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
  shape: Shape = RoundedCornerShape(RoundRadius.Medium),
  content: (@Composable () -> Unit)? = null
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled) 0.95f else 1f, animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
    ), label = "button_scale"
  )

  val alpha by animateFloatAsState(
    targetValue = if (enabled) 1f else 0.5f, animationSpec = tween(150), label = "button_alpha"
  )

  Box(
    modifier = modifier
      .scale(scale)
      .alpha(alpha)
      .background(
        brush = if (enabled) gradient else disabledGradient, shape = shape
      )
      .clip(shape)
      .clickable(
        interactionSource = interactionSource, indication = null, enabled = enabled
      ) { onClick() }
      .padding(contentPadding),
    contentAlignment = Alignment.Center) {
    when {
      content != null -> content()
      text != null -> {
        Text(
          text = text,
          color = if (enabled) textColor else disabledTextColor,
          style = TextStyle(
            fontSize = textSize ?: 16.sp,
            fontWeight = fontWeight,
            fontFamily = ibmpiexsansthailooped
          )
        )
      }
    }
  }
}