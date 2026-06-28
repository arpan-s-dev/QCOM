package com.medic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.medic.app.data.HospitalWithBearing
import com.medic.app.nav.PositionSource
import com.medic.app.ui.components.CompassRing
import com.medic.app.ui.theme.*

enum class OrientNavMode {
    SOLAR,
    NIGHT_SKY
}

data class StarNavUiState(
    val processing: Boolean = false,
    val detectedStars: Int = 0,
    val message: String? = null,
    val approximateLat: Double? = null,
    val latUncertainty: Double? = null,
    val solverKind: String? = null
)

@Composable
fun OrientScreen(
    positionSource: PositionSource,
    orientNavMode: OrientNavMode,
    onOrientNavModeChange: (OrientNavMode) -> Unit,
    sunAzimuthDeg: Double?,
    sunElevationDeg: Double?,
    correctedHeadingDeg: Double?,
    starNav: StarNavUiState,
    onPickNightSkyImage: () -> Unit,
    nearestHospitals: List<HospitalWithBearing>,
    hasDeviceFix: Boolean,
    deviceLat: Double?,
    deviceLon: Double?,
    deviceFixAccuracyM: Float?,
    deviceFixProvider: String?,
    deviceFixAgeMs: Long?,
    onUseMyLocation: () -> Unit,
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
            text = orientBanner(positionSource, orientNavMode),
            style = FieldType.caption,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Primary task: where am I → nearest hospital → which way to walk.
        HospitalsNearPanel(
            nearestHospitals = nearestHospitals,
            hasDeviceFix = hasDeviceFix,
            deviceLat = deviceLat,
            deviceLon = deviceLon,
            accuracyMeters = deviceFixAccuracyM,
            provider = deviceFixProvider,
            fixAgeMs = deviceFixAgeMs,
            onUseMyLocation = onUseMyLocation
        )

        Spacer(modifier = Modifier.height(20.dp))

        CompassRing(
            headingDeg = correctedHeadingDeg ?: 0.0,
            sunAzimuthDeg = if (orientNavMode == OrientNavMode.SOLAR) sunAzimuthDeg else null,
            targetBearingDeg = nearestHospitals.firstOrNull()?.bearingDegrees
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Orange marker points to the nearest hospital. Turn the phone until the brass needle lines up, then walk that way.",
            style = FieldType.caption,
            color = NeutralGray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OrientModeSwitcher(
            mode = orientNavMode,
            onModeChange = onOrientNavModeChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (orientNavMode) {
            OrientNavMode.SOLAR -> SolarOrientPanel(
                sunElevationDeg = sunElevationDeg,
                sunAzimuthDeg = sunAzimuthDeg,
                onSightSun = onSightSun
            )
            OrientNavMode.NIGHT_SKY -> NightSkyOrientPanel(
                starNav = starNav,
                correctedHeadingDeg = correctedHeadingDeg,
                onPickNightSkyImage = onPickNightSkyImage
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun orientBanner(source: PositionSource, mode: OrientNavMode): String {
    return when (mode) {
        OrientNavMode.NIGHT_SKY -> when (source) {
            PositionSource.GPS_TRUSTED ->
                "GPS available — night-sky heading is a backup when jammed or at night."
            PositionSource.DEAD_RECKONING ->
                "GPS unavailable. Import a night-sky photo to derive true north from the star field."
            PositionSource.SOLAR_FIX, PositionSource.STAR_FIX ->
                "No reliable position fix. Star-field plate solve provides heading only."
        }
        OrientNavMode.SOLAR -> when (source) {
            PositionSource.GPS_TRUSTED ->
                "GPS is available — solar compass is a backup, not primary, right now."
            PositionSource.DEAD_RECKONING ->
                "GPS unavailable. Dead reckoning active. Solar compass can correct heading drift."
            PositionSource.SOLAR_FIX, PositionSource.STAR_FIX ->
                "No reliable position fix. Point the phone at the sun to derive true north."
        }
    }
}

@Composable
private fun OrientModeSwitcher(
    mode: OrientNavMode,
    onModeChange: (OrientNavMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PanelDeep)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OrientModeTab(
            label = "Solar (day)",
            selected = mode == OrientNavMode.SOLAR,
            onClick = { onModeChange(OrientNavMode.SOLAR) },
            modifier = Modifier.weight(1f)
        )
        OrientModeTab(
            label = "Night sky (stars)",
            selected = mode == OrientNavMode.NIGHT_SKY,
            onClick = { onModeChange(OrientNavMode.NIGHT_SKY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OrientModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) PanelMoss else PanelDeep)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FieldType.body,
            color = if (selected) Bone else NeutralGray
        )
    }
}

@Composable
private fun SolarOrientPanel(
    sunElevationDeg: Double?,
    sunAzimuthDeg: Double?,
    onSightSun: () -> Unit
) {
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
}

@Composable
private fun NightSkyOrientPanel(
    starNav: StarNavUiState,
    correctedHeadingDeg: Double?,
    onPickNightSkyImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PanelMoss)
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Import an outdoor night-sky photo. The app detects star points and plate-solves against a bundled catalog — no network, no ML training.",
                style = FieldType.body,
                color = Bone
            )
            Spacer(modifier = Modifier.height(10.dp))
            FieldButton(
                text = if (starNav.processing) "PROCESSING…" else "IMPORT NIGHT-SKY PHOTO",
                onClick = onPickNightSkyImage,
                enabled = !starNav.processing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Heading from star field — accuracy depends on image quality.",
        style = FieldType.caption,
        color = NeutralGray
    )

    if (starNav.processing) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Detecting stars and matching catalog…", style = FieldType.caption)
    }

    starNav.message?.let { msg ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = msg,
            style = FieldType.caption,
            color = if (correctedHeadingDeg != null) Bone else SeriousAmber
        )
    }

    if (starNav.detectedStars > 0) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Detected ${starNav.detectedStars} star points" +
                (starNav.solverKind?.let { " • solver: $it" } ?: ""),
            style = FieldType.caption
        )
    }

    if (correctedHeadingDeg != null && starNav.approximateLat != null) {
        Spacer(modifier = Modifier.height(8.dp))
        val unc = starNav.latUncertainty ?: 1.0
        Text(
            text = "Approx. latitude ${"%.2f".format(starNav.approximateLat)}° ± ${"%.1f".format(unc)}° — not GPS-grade",
            style = FieldType.caption,
            color = NeutralGray
        )
    }
}
