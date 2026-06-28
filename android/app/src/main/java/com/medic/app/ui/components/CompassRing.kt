package com.medic.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.medic.app.ui.theme.Bone
import com.medic.app.ui.theme.CompassBrass
import com.medic.app.ui.theme.CriticalRed
import com.medic.app.ui.theme.ModerateYellow
import com.medic.app.ui.theme.NeutralGray
import com.medic.app.ui.theme.PanelBorder
import com.medic.app.ui.theme.PanelDeep
import com.medic.app.ui.theme.SignalOrange
import kotlin.math.cos
import kotlin.math.sin

/**
 * A live "find north" compass. [headingDeg] is the device azimuth (0 = the top
 * edge points at magnetic north). The whole dial rotates by -heading so N, the
 * needle, the sun, and the hospital bearing all sit at their real-world
 * directions; a fixed index at the top shows the way the phone is pointing.
 */
@Composable
fun CompassRing(
    headingDeg: Double,
    sunAzimuthDeg: Double? = null,
    targetBearingDeg: Double? = null,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(220.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 14

        // A point at compass [bearing], at distance [r] from centre, rotated so
        // that real north sits at (-heading) from straight up.
        fun pt(bearing: Double, r: Float): Offset {
            val rad = Math.toRadians((bearing - headingDeg) - 90.0)
            return Offset(center.x + r * cos(rad).toFloat(), center.y + r * sin(rad).toFloat())
        }

        drawCircle(color = PanelBorder, radius = radius, center = center, style = Stroke(width = 2.5f))

        // Minor ticks every 30°.
        for (b in 0 until 360 step 30) {
            if (b % 90 != 0) {
                drawLine(PanelBorder, pt(b.toDouble(), radius - 8), pt(b.toDouble(), radius), strokeWidth = 1.5f)
            }
        }

        // Cardinal ticks + labels.
        listOf(0.0 to "N", 90.0 to "E", 180.0 to "S", 270.0 to "W").forEach { (b, label) ->
            val isN = label == "N"
            drawLine(
                color = if (isN) CriticalRed else NeutralGray,
                start = pt(b, radius - 14),
                end = pt(b, radius),
                strokeWidth = if (isN) 4f else 2.5f
            )
            val lp = pt(b, radius - 32)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                lp.x,
                lp.y + 10f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor(if (isN) "#E0392F" else "#E8E4D9")
                    textSize = 30f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // Sun direction (day) and hospital bearing markers ride the same rotation.
        sunAzimuthDeg?.let { drawCircle(ModerateYellow, 9f, pt(it, radius - 6)) }
        targetBearingDeg?.let {
            drawLine(SignalOrange, pt(it, radius * 0.45f), pt(it, radius - 6), strokeWidth = 4f)
            drawCircle(SignalOrange, 10f, pt(it, radius - 6))
        }

        // North–south needle: red half points to true north.
        drawLine(CriticalRed, center, pt(0.0, radius - 24), strokeWidth = 6f)
        drawLine(NeutralGray, center, pt(180.0, radius - 24), strokeWidth = 6f)
        drawCircle(CompassBrass, 7f, center)
        drawCircle(PanelDeep, 3f, center)

        // Fixed index at the top = the direction the phone is pointing.
        drawHeadingIndex(center, radius)
    }
}

private fun DrawScope.drawHeadingIndex(center: Offset, radius: Float) {
    val path = Path().apply {
        moveTo(center.x - 11f, center.y - radius - 4f)
        lineTo(center.x + 11f, center.y - radius - 4f)
        lineTo(center.x, center.y - radius + 14f)
        close()
    }
    drawPath(path, Bone)
}
