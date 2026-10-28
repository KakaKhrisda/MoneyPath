package com.example.moneypath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The MoneyPath logo: an "M" whose middle stroke rises into a growth arrow.
 * Use it anywhere: MoneyPathLogo(logoSize = 40.dp)
 */
@Composable
fun MoneyPathLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 40.dp
) {
    Canvas(modifier = modifier.size(logoSize)) {
        val u = this.size.minDimension / 100f

        val brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1DBF73), Color(0xFF0B6B4B)),
            start = Offset(0f, 0f),
            end = Offset(this.size.width, this.size.height)
        )

        val stroke = Stroke(
            width = 17f * u,
            cap = StrokeCap.Butt,
            join = StrokeJoin.Miter,
            miter = 10f
        )

        // Left leg, down-slope and rising arrow shaft
        val mShape = Path().apply {
            moveTo(14f * u, 96f * u)
            lineTo(14f * u, 32f * u)
            lineTo(46f * u, 66f * u)
            lineTo(78f * u, 26f * u)
        }
        drawPath(path = mShape, brush = brush, style = stroke)

        // Right leg of the M
        val rightLeg = Path().apply {
            moveTo(90f * u, 52f * u)
            lineTo(90f * u, 96f * u)
        }
        drawPath(path = rightLeg, brush = brush, style = stroke)

        // Arrow head
        val arrowHead = Path().apply {
            moveTo(98f * u, 2f * u)
            lineTo(68f * u, 10f * u)
            lineTo(90f * u, 32f * u)
            close()
        }
        drawPath(path = arrowHead, brush = brush)
    }
}