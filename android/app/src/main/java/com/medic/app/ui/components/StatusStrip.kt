package com.medic.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.medic.app.nav.PositionSource
import com.medic.app.ui.theme.*

/**
 * Signature element: always-visible position trust indicator.
 * Pulsing dot speed communicates normal tracking vs spoof alert.
 */

@Composable
fun StatusStrip(
    positionSource: PositionSource,
    spoofDetected: Boolean,
    headingDegrees: Float?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelMoss)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PositionSourcePill(positionSource, spoofDetected, headingDegrees)
    }
}

@Composable
private fun PositionSourcePill(
    source: PositionSource,
    spoofDetected: Boolean,
    headingDegrees: Float?
) {
    val targetColor = when (source) {
        PositionSource.GPS_TRUSTED -> GpsTrustedGreen
        PositionSource.DEAD_RECKONING -> DeadReckoningAmber
        PositionSource.SOLAR_FIX -> SolarFixBlue
        PositionSource.STAR_FIX -> StarFixViolet
    }
    val label = when (source) {
        PositionSource.GPS_TRUSTED -> "GPS_TRUSTED"
        PositionSource.DEAD_RECKONING -> "DEAD_RECKONING"
        PositionSource.SOLAR_FIX -> "SOLAR_FIX"
        PositionSource.STAR_FIX -> "STAR_FIX"
    }

    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = LodestarMotion.colorCrossfade,
        label = "position-source-color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PanelDeep)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        PulsingDot(color = color, fast = spoofDetected)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = FieldType.statusLabel, color = color)
            if (spoofDetected) {
                Text(
                    text = "SPOOF_DETECTED — frozen to last trusted fix",
                    style = FieldType.caption,
                    color = CriticalRed
                )
            } else if (
                (source == PositionSource.SOLAR_FIX || source == PositionSource.STAR_FIX) &&
                headingDegrees != null
            ) {
                Text(text = "heading ${headingDegrees.toInt()}°", style = FieldType.caption)
            }
        }
    }
}

@Composable
private fun PulsingDot(color: Color, fast: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "status-pulse")
    val durationMs = if (fast) 500 else 1800
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot-alpha"
    )
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = LodestarMotion.colorCrossfade,
        label = "dot-color"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(animatedColor)
    )
}
