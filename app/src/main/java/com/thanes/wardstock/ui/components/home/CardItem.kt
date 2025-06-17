package com.thanes.wardstock.ui.components.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanes.wardstock.data.models.OrderItem
import com.thanes.wardstock.data.models.OrderModel
import com.thanes.wardstock.ui.theme.Colors
import com.thanes.wardstock.ui.theme.ibmpiexsansthailooped
import kotlinx.coroutines.delay

@Composable
fun CardItem(index: Int, item: OrderItem) {
  val shadowColor = when (item.drugPriority) {
    1 -> Colors.BlueGrey40.copy(alpha = 0.65f)
    2 -> Color.Yellow.copy(alpha = 0.65f)
    else -> Color(0xFFE91E63).copy(alpha = 0.65f)
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Colors.BlueGrey140),
    modifier = Modifier
      .fillMaxWidth()
      .shadow(
        elevation = 14.dp,
        shape = RoundedCornerShape(24.dp),
        ambientColor = shadowColor,
        spotColor = shadowColor
      ),
    shape = RoundedCornerShape(24.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          top = 15.dp,
          end = 25.dp,
          bottom = 15.dp,
          start = 20.dp
        ),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = "Drug: ${item.drugName}",
          style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Colors.BluePrimary,
            fontFamily = ibmpiexsansthailooped
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Quantity: ${item.qty} ${item.unit}",
            style = TextStyle(
              fontSize = 18.sp,
              color = Colors.BlueSecondary,
              fontFamily = ibmpiexsansthailooped
            )
          )

          Text(
            text = "|",
            style = TextStyle(
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = Colors.BlueSecondary,
              fontFamily = ibmpiexsansthailooped
            )
          )

          Text(
            text = "Position: ${item.position}",
            style = TextStyle(
              fontSize = 18.sp,
              color = Colors.BlueSecondary,
              fontFamily = ibmpiexsansthailooped
            )
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
          modifier = Modifier
            .background(
              color = when (item.drugPriority) {
                1 -> Colors.BlueGrey140
                2 -> Color.Yellow
                else -> Color(0xFFE91E63)
              },
              shape = RoundedCornerShape(16.dp)
            )
            .border(
              width = 1.5.dp,
              color = when (item.drugPriority) {
                1 -> Colors.BlueGrey40
                2 -> Color.Yellow
                else -> Color(0xFFE91E63)
              },
              shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
          Text(
            text = when (item.drugPriority) {
              1 -> "Normal"
              2 -> "HAD"
              else -> "Narcotic"
            },
            style = TextStyle(
              fontSize = 18.sp,
              color = when (item.drugPriority) {
                1 -> Colors.BlueGrey40
                2 -> Colors.BlueGrey40
                else -> Colors.BlueGrey140
              },
              fontFamily = ibmpiexsansthailooped
            )
          )
        }
      }

      Box(
        modifier = Modifier.width(125.dp),
        contentAlignment = Alignment.Center
      ) {
        when (item.status) {
          "pending" -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
              CircularProgressIndicator(
                color = Color(0xFFFF9800), // Orange
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
              )
              Text(
                text = "กำลังจัด",
                style = TextStyle(
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Medium,
                  color = Colors.BlueGrey40,
                  fontFamily = ibmpiexsansthailooped
                )
              )
            }
          }

          "receive" -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.Green,
                modifier = Modifier.size(38.dp)
              )
              Text(
                text = "จัดเสร็จแล้ว",
                style = TextStyle(
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.Green,
                  fontFamily = ibmpiexsansthailooped
                )
              )
            }
          }

          "error" -> {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(38.dp)
              )
              Text(
                text = "ผิดพลาด",
                style = TextStyle(
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.Red,
                  fontFamily = ibmpiexsansthailooped
                )
              )
            }
          }

          else -> {
            Text(
              text = "รอจัด",
              style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Colors.BlueGrey40,
                fontFamily = ibmpiexsansthailooped
              )
            )
          }
        }
      }
    }
  }
}

@Composable
fun AnimatedCardItem(index: Int, item: OrderItem) {
  var visible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(index * 150L)
    visible = true
  }

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(700)) +
            slideInVertically(tween(700)) { it / 2 } +
            scaleIn(
              animationSpec = tween(durationMillis = 800),
              initialScale = 0.85f
            ),
    modifier = Modifier.padding(top = 4.dp, end = 12.dp, start = 12.dp)
  ) {
    CardItem(index, item)
  }
}

@Composable
fun PrescriptionHeader(orderState: OrderModel) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = orderState.patientName,
      style = TextStyle(
        fontSize = 27.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = ibmpiexsansthailooped,
        color = Colors.BluePrimary
      ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "No: ${orderState.id}",
      style = TextStyle(
        fontSize = 20.sp,
        fontFamily = ibmpiexsansthailooped,
        color = Colors.BlueGrey40
      )
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "HN: ${orderState.hn}",
      style = TextStyle(
        fontSize = 20.sp,
        fontFamily = ibmpiexsansthailooped,
        color = Colors.BlueGrey40
      )
    )

    Spacer(modifier = Modifier.height(4.dp))

    orderState.wardDesc?.let { ward ->
      Text(
        text = "Ward: $ward",
        style = TextStyle(
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped,
          color = Colors.BlueGrey40
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    orderState.priorityDesc?.let { priority ->
      Text(
        text = "Priority: $priority",
        style = TextStyle(
          fontSize = 20.sp,
          fontFamily = ibmpiexsansthailooped,
          color = Colors.BlueGrey40
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 6.dp),
      color = Colors.BlueGrey40
    )
  }
}