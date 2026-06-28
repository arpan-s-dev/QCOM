package com.medic.app.ui.components

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
 * THE SIGNATURE ELEMENT. Always visible, never collapsible, never buried in
 * a menu -- this strip is the app's promise made visible: "we know where
 * we think you are, and we know we have no signal." It sits pinned at the
 * top of every screen regardless of which section (TREAT/ORIENT/COMMUNICATE)
 * is active.
 *
 * Two halves:
 *  - left: position source pill with a pulsing dot (pulse rate communicates
 *    "this is live," color communicates trust level)
 *  - right: AIRPLANE MODE badge -- bright, deliberately a little proud of
 *    itself, because working with zero connectivity is the whole point.
 */

@Composable
fun StatusStrip(
    positionSource: PositionSource,
    spoofDetected: Boolean,
    headingDegrees: Float?,
    airplaneModeOn: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PositionSourcePill(positionSource, spoofDetected, headingDegrees)
        Spacer(modifier = Modifier.width(8.dp))
        AirplaneModeBadge(airplaneModeOn)
    }
}

@Composable
private fun PositionSourcePill(
    source: PositionSource,
    spoofDetected: Boolean,
    headingDegrees: Float?
) {
    val (color, label) = when (source) {
        PositionSource.GPS_TRUSTED -> GpsTrustedGreen to "GPS_TRUSTED"
        PositionSource.DEAD_RECKONING -> DeadReckoningAmber to "DEAD_RECKONING"
        PositionSource.SOLAR_FIX -> SolarFixBlue to "SOLAR_FIX"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(InkBlack)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        PulsingDot(color = color, fast = spoofDetected)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = FieldType.statusLabel, color = color)
            if (spoofDetected) {
                Text(text = "SPOOF_DETECTED — frozen to last trusted fix", style = FieldType.caption, color = CriticalRed)
            } else if (source == PositionSource.SOLAR_FIX && headingDegrees != null) {
                Text(text = "heading ${headingDegrees.toInt()}°", style = FieldType.caption)
            }
        }
    }
}

/**
 * Pulse rate is meaningful, not decorative: a calm ~1.8s breathing pulse
 * means "tracking normally," a fast ~0.5s pulse means "something's wrong,
 * pay attention" (used when spoofDetected). This mirrors how a cardiac
 * monitor's beep tempo communicates urgency without needing to be read.
 */
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
    Box(
        modifier = Modifier
            .size(10.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun AirplaneModeBadge(on: Boolean) {
    val bg = if (on) SignalTeal else PanelBorder
    val fg = if (on) InkBlack else NeutralGray
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (on) "✈ AIRPLANE MODE" else "✈ ONLINE",
            style = FieldType.statusLabel,
            color = fg
        )
    }
}
