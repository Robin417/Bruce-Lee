package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.SeniorTheme

@Composable
fun WaterCupGraphic(
  waterCount: Int,
  modifier: Modifier = Modifier,
  goalCups: Int = 8,
) {
  val colors = SeniorTheme.colors
  val targetFraction = (waterCount.coerceIn(0, goalCups).toFloat() / goalCups.toFloat())
  val animatedFraction by
    animateFloatAsState(
      targetValue = targetFraction,
      animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
      label = "water_fill_fraction",
    )
  val description = stringResource(R.string.cd_water_cup, waterCount)

  Canvas(
    modifier =
      modifier.size(width = 96.dp, height = 116.dp).semantics {
        contentDescription = description
      }
  ) {
    val w = size.width
    val h = size.height
    val strokePx = w * 0.055f

    val topLeftX = w * 0.14f
    val topRightX = w * 0.86f
    val bottomLeftX = w * 0.24f
    val bottomRightX = w * 0.76f
    val topY = h * 0.10f
    val bottomY = h * 0.92f
    val cornerRadius = w * 0.08f

    val cupPath =
      Path().apply {
        moveTo(topLeftX, topY)
        lineTo(topRightX, topY)
        lineTo(bottomRightX, bottomY - cornerRadius)
        quadraticTo(bottomRightX, bottomY, bottomRightX - cornerRadius, bottomY)
        lineTo(bottomLeftX + cornerRadius, bottomY)
        quadraticTo(bottomLeftX, bottomY, bottomLeftX, bottomY - cornerRadius)
        close()
      }

    // Glass interior background
    drawPath(path = cupPath, color = colors.waterGlassInterior)

    // Animated water fill inside clipped cup path
    if (animatedFraction > 0.005f) {
      clipPath(cupPath) {
        val usableHeight = bottomY - (topY + h * 0.08f)
        val waterTopY = bottomY - (usableHeight * animatedFraction)
        val waveHeight = h * 0.025f

        val waterPath =
          Path().apply {
            moveTo(0f, waterTopY)
            quadraticTo(w * 0.25f, waterTopY - waveHeight, w * 0.5f, waterTopY)
            quadraticTo(w * 0.75f, waterTopY + waveHeight, w, waterTopY)
            lineTo(w, h)
            lineTo(0f, h)
            close()
          }

        drawPath(
          path = waterPath,
          brush =
            Brush.verticalGradient(
              colors = listOf(colors.waterFillLight, colors.waterFillDeep),
              startY = waterTopY,
              endY = bottomY,
            ),
        )

        // Bright water surface rim highlight
        val surfaceLine =
          Path().apply {
            moveTo(0f, waterTopY)
            quadraticTo(w * 0.25f, waterTopY - waveHeight, w * 0.5f, waterTopY)
            quadraticTo(w * 0.75f, waterTopY + waveHeight, w, waterTopY)
          }
        drawPath(
          path = surfaceLine,
          color = Color.White.copy(alpha = 0.65f),
          style = Stroke(width = strokePx * 0.55f, cap = StrokeCap.Round),
        )
      }
    }

    // Subtle measurement tick lines on the right side of the cup
    val tickFractions = listOf(0.25f, 0.5f, 0.75f)
    val usableHeight = bottomY - (topY + h * 0.08f)
    tickFractions.forEach { frac ->
      val y = bottomY - usableHeight * frac
      val progress = (y - topY) / (bottomY - topY)
      val rightEdgeX = topRightX + (bottomRightX - topRightX) * progress
      drawLine(
        color = colors.waterGlassOutline.copy(alpha = 0.35f),
        start = Offset(rightEdgeX - w * 0.14f, y),
        end = Offset(rightEdgeX - w * 0.04f, y),
        strokeWidth = strokePx * 0.45f,
        cap = StrokeCap.Round,
      )
    }

    // Glass shine highlight on the left
    drawLine(
      color = Color.White.copy(alpha = 0.55f),
      start = Offset(w * 0.24f, topY + h * 0.12f),
      end = Offset(w * 0.30f, bottomY - h * 0.14f),
      strokeWidth = strokePx * 0.7f,
      cap = StrokeCap.Round,
    )

    // Outer glass rim outline
    drawPath(
      path = cupPath,
      color = colors.waterGlassOutline,
      style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
  }
}
