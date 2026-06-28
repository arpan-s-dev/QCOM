package com.medic.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.medic.app.ui.theme.CompassBrass
import com.medic.app.ui.theme.LodestarMotion
import com.medic.app.ui.theme.ModerateYellow
import com.medic.app.ui.theme.NeutralGray
import com.medic.app.ui.theme.PanelBorder
import com.medic.app.ui.theme.PanelDeep
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassRing(
    headingDeg: Double,
    sunAzimuthDeg: Double? = null,
    modifier: Modifier = Modifier
) {
    val animatedHeading by animateFloatAsState(
        targetValue = headingDeg.toFloat(),
        animationSpec = LodestarMotion.compassSpring,
        label = "compass-needle"
    )

    val sizeDp = 220.dp
    Canvas(modifier = modifier.size(sizeDp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 12

        drawCircle(
            color = PanelBorder,
            radius = radius,
            center = center,
            style = Stroke(width = 2.5f)
        )

        listOf(0.0 to "N", 90.0 to "E", 180.0 to "S", 270.0 to "W").forEach { (angle, label) ->
            val rad = Math.toRadians(angle - 90.0)
            val tickOuter = Offset(
                center.x + radius * cos(rad).toFloat(),
                center.y + radius * sin(rad).toFloat()
            )
            val tickInner = Offset(
                center.x + (radius - 14) * cos(rad).toFloat(),
                center.y + (radius - 14) * sin(rad).toFloat()
            )
            drawLine(color = NeutralGray, start = tickInner, end = tickOuter, strokeWidth = 2.5f)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                center.x + (radius - 28) * cos(rad).toFloat() - 6,
                center.y + (radius - 28) * sin(rad).toFloat() + 6,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#E8E4D9")
                    textSize = 28f
                    isFakeBoldText = true
                }
            )
        }

        if (sunAzimuthDeg != null) {
            val rad = Math.toRadians(sunAzimuthDeg - 90.0)
            val sunPos = Offset(
                center.x + (radius - 4) * cos(rad).toFloat(),
                center.y + (radius - 4) * sin(rad).toFloat()
            )
            drawCircle(color = ModerateYellow, radius = 9f, center = sunPos)
        }

        val needleRad = Math.toRadians(animatedHeading.toDouble() - 90.0)
        val needleTip = Offset(
            center.x + (radius - 20) * cos(needleRad).toFloat(),
            center.y + (radius - 20) * sin(needleRad).toFloat()
        )
        val needleTail = Offset(
            center.x - (radius * 0.22f) * cos(needleRad).toFloat(),
            center.y - (radius * 0.22f) * sin(needleRad).toFloat()
        )
        drawLine(color = CompassBrass, start = needleTail, end = needleTip, strokeWidth = 5f)
        drawCircle(color = CompassBrass, radius = 7f, center = center)
        drawCircle(color = PanelDeep, radius = 3f, center = center)
    }
}
