package com.medic.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.medic.app.nav.PositionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.medic.app.data.HospitalWithBearing
import com.medic.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrientScreen(
    positionSource: PositionSource,
    sunAzimuthDeg: Double?,
    sunElevationDeg: Double?,
    correctedHeadingDeg: Double?,
    nearestHospitals: List<HospitalWithBearing>,
    onSightSun: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FieldGreen)
            .verticalScroll(scroll)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "ORIENT", style = FieldType.heading, color = Bone)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (positionSource) {
                PositionSource.GPS_TRUSTED ->
                    "GPS is available — solar compass is a backup, not primary, right now."
                PositionSource.DEAD_RECKONING ->
                    "GPS unavailable. Dead reckoning active. Solar compass can correct heading drift."
                PositionSource.SOLAR_FIX ->
                    "No reliable position fix. Point the phone at the sun to derive true north."
            },
            style = FieldType.caption,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CompassRing(correctedHeadingDeg ?: 0.0, sunAzimuthDeg)

        Spacer(modifier = Modifier.height(20.dp))

        if (sunElevationDeg != null && sunElevationDeg < 5.0) {
            Text(
                text = "Sun is too low or below the horizon right now — solar sighting won't be reliable.",
                style = FieldType.caption,
                color = SeriousAmber
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PanelMoss)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hold the phone flat, point the TOP EDGE at the sun, then tap below.",
                        style = FieldType.body,
                        color = Bone
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FieldButton(
                        text = "SIGHT SUN",
                        onClick = onSightSun,
                        modifier = Modifier.fillMaxWidth()
                    )
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

        Spacer(modifier = Modifier.height(20.dp))
        HospitalsNearPanel(nearestHospitals = nearestHospitals)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CompassRing(headingDeg: Double, sunAzimuthDeg: Double?) {
    val animatedHeading by animateFloatAsState(
        targetValue = headingDeg.toFloat(),
        animationSpec = LodestarMotion.compassSpring,
        label = "compass-needle"
    )

    val sizeDp = 220.dp
    Canvas(modifier = Modifier.size(sizeDp)) {
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
