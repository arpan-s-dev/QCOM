package com.medic.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color

object LodestarMotion {
    val colorCrossfade = tween<Color>(durationMillis = 500)
    const val tabEnterMillis: Int = 280
    const val tabExitMillis: Int = 180
    const val messageEnterMillis: Int = 320
    val compassSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val micPulse = tween<Float>(durationMillis = 700)
}
