package com.medic.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object LodestarMotion {
    val colorCrossfade = tween(durationMillis = 500)
    val tabEnter = tween(durationMillis = 280)
    val tabExit = tween(durationMillis = 180)
    val messageEnter = tween(durationMillis = 320)
    val compassSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val micPulse = tween(durationMillis = 700)
}
