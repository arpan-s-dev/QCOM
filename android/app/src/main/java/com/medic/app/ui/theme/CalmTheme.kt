package com.medic.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * SafeGuide "calm" identity — the clean consumer look from the product mockup:
 * dark-neutral surfaces so a scared person isn't blinded at night, with soft
 * pastel feature tiles that read instantly. Distinct from the Lodestar
 * field-instrument theme; both ship in the same app.
 */

// Neutral surfaces
val SgBg = Color(0xFF0E0F13)
val SgSurface = Color(0xFF1B1D24)
val SgRaised = Color(0xFF242730)
val SgBorder = Color(0xFF2C2F38)

// Text
val SgText = Color(0xFFF2F3F5)
val SgTextSecondary = Color(0xFFA8ACB6)
val SgTextMuted = Color(0xFF6E727C)

// Actions
val SgBlue = Color(0xFF378ADD)
val SgTeal = Color(0xFF1D9E75)

/** A single feature's color set: pastel tile fill + icon chip + dark-on-light text. */
data class SgAccent(
    val tile: Color,
    val chip: Color,
    val icon: Color,
    val title: Color,
    val subtitle: Color
)

val SgAssistant = SgAccent(Color(0xFFE6F1FB), Color(0xFFB5D4F4), Color(0xFF0C447C), Color(0xFF042C53), Color(0xFF185FA5))
val SgTranslate = SgAccent(Color(0xFFEEEDFE), Color(0xFFCECBF6), Color(0xFF3C3489), Color(0xFF26215C), Color(0xFF534AB7))
val SgFindNorth = SgAccent(Color(0xFFFAEEDA), Color(0xFFFAC775), Color(0xFF633806), Color(0xFF412402), Color(0xFF854F0B))
val SgMedical = SgAccent(Color(0xFFFCEBEB), Color(0xFFF7C1C1), Color(0xFF791F1F), Color(0xFF501313), Color(0xFFA32D2D))
val SgHospital = SgAccent(Color(0xFFE1F5EE), Color(0xFF9FE1CB), Color(0xFF085041), Color(0xFF04342C), Color(0xFF0F6E56))

private val SafeGuideColors = darkColorScheme(
    background = SgBg,
    surface = SgSurface,
    primary = SgBlue,
    onPrimary = Color.White,
    secondary = SgTeal,
    onSecondary = Color.White,
    onBackground = SgText,
    onSurface = SgText
)

@Composable
fun SafeGuideTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SafeGuideColors, content = content)
}
