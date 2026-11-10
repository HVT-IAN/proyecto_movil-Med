package com.meds.app.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.core.ui.theme.MedsSemanticColors
import kotlin.math.min

@Composable
fun PlantArt(
    stage: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalMedsColors.current
    Canvas(modifier = modifier) {
        drawPlant(stage.coerceIn(1, 5), colors)
    }
}

private fun DrawScope.drawPlant(stage: Int, colors: MedsSemanticColors) {
    val w = size.width
    val h = size.height
    val unit = min(w, h)

    drawOval(
        color = Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(w * 0.22f, h * 0.91f),
        size = Size(w * 0.56f, h * 0.06f)
    )

    val potPath = Path().apply {
        moveTo(w * 0.30f, h * 0.74f)
        lineTo(w * 0.70f, h * 0.74f)
        lineTo(w * 0.64f, h * 0.93f)
        lineTo(w * 0.36f, h * 0.93f)
        close()
    }
    drawPath(potPath, colors.pot)
    drawRoundRect(
        color = colors.pot.copy(alpha = 0.85f),
        topLeft = Offset(w * 0.28f, h * 0.71f),
        size = Size(w * 0.44f, h * 0.055f),
        cornerRadius = CornerRadius(unit * 0.03f)
    )

    val stemTop = h * (0.56f - (stage - 1) * 0.035f)
    drawLine(
        color = colors.leafDeep,
        start = Offset(w * 0.5f, h * 0.72f),
        end = Offset(w * 0.5f, stemTop),
        strokeWidth = unit * 0.018f
    )

    val pairs = when (stage) {
        1 -> 1
        2 -> 2
        3 -> 2
        4 -> 3
        else -> 4
    }
    val stemBottom = h * 0.71f
    val stemHeight = stemBottom - stemTop

    for (index in 0 until pairs) {
        val fraction = (index + 1f) / (pairs + 1f)
        val y = stemTop + stemHeight * fraction
        val leafWidth = w * (0.18f + stage * 0.006f)
        val leafHeight = h * 0.075f

        rotate(degrees = -32f, pivot = Offset(w * 0.5f, y)) {
            drawOval(
                color = colors.leaf,
                topLeft = Offset(w * 0.5f - leafWidth, y - leafHeight / 2f),
                size = Size(leafWidth, leafHeight)
            )
        }
        rotate(degrees = 32f, pivot = Offset(w * 0.5f, y)) {
            drawOval(
                color = colors.leaf.copy(alpha = 0.92f),
                topLeft = Offset(w * 0.5f, y - leafHeight / 2f),
                size = Size(leafWidth, leafHeight)
            )
        }
    }

    drawOval(
        color = colors.leafDeep,
        topLeft = Offset(w * 0.5f - w * 0.055f, stemTop - h * 0.035f),
        size = Size(w * 0.11f, h * 0.07f)
    )

    drawLine(
        color = Color.White.copy(alpha = 0.22f),
        start = Offset(w * 0.40f, h * 0.80f),
        end = Offset(w * 0.44f, h * 0.89f),
        strokeWidth = unit * 0.014f
    )
}
