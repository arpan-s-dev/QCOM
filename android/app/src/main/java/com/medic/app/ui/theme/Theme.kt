package com.medic.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Lodestar field-instrument identity: a moss-dark canopy background, bone
 * map-paper text, and signal-orange for anything that demands attention.
 * The compass needle uses brass — the one deliberate material reference to
 * analog navigation tools carried in a medic's kit.
 */

// Core palette (brief-mandated)
val FieldGreen = Color(0xFF1A2E1F)
val Bone = Color(0xFFE8E4D9)
val SignalOrange = Color(0xFFFF6B2B)

// Surfaces
val PanelMoss = Color(0xFF243828)
val PanelBorder = Color(0xFF3A4F3E)
val PanelDeep = Color(0xFF152218)

// Legacy aliases — keeps severity/nav components stable during migration
val InkBlack = FieldGreen
val OffWhite = Bone
val PanelDark = PanelMoss
val SignalTeal = SignalOrange

// Status severity colors (shared with SafetyTree.Severity)
val CriticalRed = Color(0xFFE0392F)
val SeriousAmber = Color(0xFFE0962F)
val ModerateYellow = Color(0xFFD7C548)
val MinorGreen = Color(0xFF6BA87A)
val NeutralGray = Color(0xFF9A9A8E)

// Position source colors
val GpsTrustedGreen = Color(0xFF6BA87A)
val DeadReckoningAmber = Color(0xFFE0962F)
val SolarFixBlue = Color(0xFF6B9FD4)

// Signature accent — brass compass needle on forest field
val CompassBrass = Color(0xFFC4A35A)

private val LodestarColorScheme = darkColorScheme(
    background = FieldGreen,
    surface = PanelMoss,
    primary = SignalOrange,
    onPrimary = PanelDeep,
    secondary = CompassBrass,
    onSecondary = PanelDeep,
    onBackground = Bone,
    onSurface = Bone,
    error = CriticalRed,
    outline = PanelBorder
)

// Display = instrument readout; body = conversational copy
val DisplayFontFamily = FontFamily.Monospace
val BodyFontFamily = FontFamily.SansSerif

object FieldType {
    val statusLabel = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.8.sp
    )
    val statusValue = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
    val heading = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp
    )
    val body = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val caption = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = NeutralGray
    )
}

@Composable
fun MedicOfflineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LodestarColorScheme,
        content = content
    )
}
