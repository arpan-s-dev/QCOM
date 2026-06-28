package com.medic.app.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset

object LodestarMotion {
    val colorCrossfade: AnimationSpec<Color> = tween(durationMillis = 500)
    val dpCrossfade: AnimationSpec<Dp> = tween(durationMillis = 500)
    val tabEnter: FiniteAnimationSpec<Float> = tween(durationMillis = 280)
    val tabExit: FiniteAnimationSpec<Float> = tween(durationMillis = 180)
    val messageFade: FiniteAnimationSpec<Float> = tween(durationMillis = 320)
    val messageSlide: FiniteAnimationSpec<IntOffset> = tween(durationMillis = 320)
    val compassSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val micPulse: TweenSpec<Float> = tween(durationMillis = 700)
}
