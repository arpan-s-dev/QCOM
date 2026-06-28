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
 * Design intent: this is a field-medical instrument, not a consumer app.
 * Dark-first (battery + outdoor sun glare + low-light triage scenes),
 * status color is information not decoration -- the same red/amber/green
 * vocabulary used on the safety tree severities is reused everywhere so a
 * panicked user learns the color language once and it holds throughout.
 *
 * Signature element: the status strip's pulsing position-source dot +
 * AIRPLANE MODE badge, always visible, never buried in a menu.
 */

// Background / surface
val InkBlack = Color(0xFF0B0E0D)        // near-black, slight green undertone -- "night-vision adjacent" without being literal
val PanelDark = Color(0xFF141816)
val PanelBorder = Color(0xFF262E2A)

// Status severity colors (shared with SafetyTree.Severity)
val CriticalRed = Color(0xFFE0392F)
val SeriousAmber = Color(0xFFE0962F)
val ModerateYellow = Color(0xFFD7C548)
val MinorGreen = Color(0xFF4FAE6A)
val NeutralGray = Color(0xFF8A938F)

// Position source colors
val GpsTrustedGreen = Color(0xFF4FAE6A)
val DeadReckoningAmber = Color(0xFFE0962F)
val SolarFixBlue = Color(0xFF4F8FE0)

// Accent / signature
val SignalTeal = Color(0xFF2FD0C2)      // used sparingly: the "we're tracking something live" accent
val OffWhite = Color(0xFFF1F4F2)

private val FieldColorScheme = darkColorScheme(
    background = InkBlack,
    surface = PanelDark,
    primary = SignalTeal,
    onPrimary = InkBlack,
    onBackground = OffWhite,
    onSurface = OffWhite,
    error = CriticalRed
)

// Two-role type system: a condensed display face for status/headers
// (mono-leaning, technical -- evokes instrument readouts) and a humanist
// body face for anything the user reads at length (chat answers).
// Using system fallbacks since custom font files aren't bundled here --
// swap these FontFamily.Default calls for actual bundled fonts
// (e.g. "Space Grotesk" / "IBM Plex Mono" for display, "Inter" for body)
// when assets are added to res/font.
val DisplayFontFamily = FontFamily.Monospace
val BodyFontFamily = FontFamily.SansSerif

object FieldType {
    val statusLabel = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp
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
        fontSize = 20.sp
    )
    val body = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
    val caption = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = NeutralGray
    )
}

@Composable
fun MedicOfflineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FieldColorScheme,
        content = content
    )
}
