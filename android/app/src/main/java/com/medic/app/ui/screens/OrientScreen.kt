package com.medic.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.medic.app.nav.PositionSource
import com.medic.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * ORIENT section. Two states:
 *  1. Normal: shows GPS-derived heading/position as usual (handled
 *     elsewhere once Person 1/whoever owns map rendering wires it in).
 *  2. SOLAR_FIX fallback: this screen's real purpose -- point the phone's
 *     camera-back direction at the visible sun, tap "Sight Sun," and the
 *     AR overlay below shows the derived true-north heading ring plus the
 *     device's corrected compass needle.
 *
 * The camera preview itself (CameraX) is NOT included here -- this is the
 * overlay/heading-math layer that would sit on top of a CameraX PreviewView
 * in the real Activity. Wiring the actual camera feed is flagged as a
 * follow-up; the heading math underneath (SolarCompass.kt) is verified.
 */
@Composable
fun OrientScreen(
    positionSource: PositionSource,
    sunAzimuthDeg: Double?,
    sunElevationDeg: Double?,
    correctedHeadingDeg: Double?,
    onSightSun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InkBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ORIENT", style = FieldType.heading, color = OffWhite)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (positionSource) {
                PositionSource.GPS_TRUSTED -> "GPS is available -- solar compass is a backup, not primary, right now."
                PositionSource.DEAD_RECKONING -> "GPS unavailable. Dead reckoning active. Solar compass can correct heading drift."
                PositionSource.SOLAR_FIX -> "No reliable position fix. Point the phone at the sun to derive true north."
            },
            style = FieldType.caption,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CompassRing(correctedHeadingDeg ?: 0.0, sunAzimuthDeg)

        Spacer(modifier = Modifier.height(20.dp))

        if (sunElevationDeg != null && sunElevationDeg < 5.0) {
            Text(
                text = "Sun is too low or below the horizon right now -- solar sighting won't be reliable.",
                style = FieldType.caption,
                color = SeriousAmber
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PanelDark)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hold the phone flat, point the TOP EDGE at the sun, then tap below.",
                        style = FieldType.body,
                        color = OffWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = onSightSun) {
                        Text("SIGHT SUN")
                    }
                }
            }
        }

        if (sunAzimuthDeg != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sun azimuth: ${"%.1f".format(sunAzimuthDeg)}°  •  elevation: ${"%.1f".format(sunElevationDeg ?: 0.0)}°",
                style = FieldType.caption
            )
        }
    }
}

/**
 * Simple AR-style compass ring: a circle with cardinal ticks, a sun marker
 * at its azimuth, and a needle showing the corrected device heading. This
 * is a static Canvas drawing, not a live camera AR overlay -- the real
 * camera-composited version would draw this same ring over a CameraX
 * PreviewView once that's wired in.
 */
@Composable
private fun CompassRing(headingDeg: Double, sunAzimuthDeg: Double?) {
    val sizeDp = 220.dp
    Canvas(modifier = Modifier.size(sizeDp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 12

        // Outer ring
        drawCircle(color = PanelBorder, radius = radius, center = center, style = Stroke(width = 3f))

        // Cardinal ticks (N/E/S/W) -- N is always "up" on screen here since
        // this is a fixed compass-rose view, not a rotating-with-device view.
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
            drawLine(color = NeutralGray, start = tickInner, end = tickOuter, strokeWidth = 3f)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                center.x + (radius - 28) * cos(rad).toFloat() - 6,
                center.y + (radius - 28) * sin(rad).toFloat() + 6,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    isFakeBoldText = true
                }
            )
        }

        // Sun marker
        if (sunAzimuthDeg != null) {
            val rad = Math.toRadians(sunAzimuthDeg - 90.0)
            val sunPos = Offset(
                center.x + (radius - 4) * cos(rad).toFloat(),
                center.y + (radius - 4) * sin(rad).toFloat()
            )
            drawCircle(color = ModerateYellow, radius = 10f, center = sunPos)
        }

        // Heading needle (corrected true-north heading)
        val needleRad = Math.toRadians(headingDeg - 90.0)
        val needleTip = Offset(
            center.x + (radius - 20) * cos(needleRad).toFloat(),
            center.y + (radius - 20) * sin(needleRad).toFloat()
        )
        drawLine(color = SignalTeal, start = center, end = needleTip, strokeWidth = 6f)
        drawCircle(color = SignalTeal, radius = 8f, center = center)
    }
}
