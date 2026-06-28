package com.medic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medic.app.data.GeoMath
import com.medic.app.data.HospitalWithBearing
import com.medic.app.ui.theme.*

/**
 * Nearest-hospital guidance from an approximate fix. Leads with one clear
 * destination — name, distance, and a plain cardinal direction to walk —
 * then lists the next two. Distances are straight-line estimates from an
 * offline list, not turn-by-turn routing.
 */
@Composable
fun HospitalsNearPanel(
    nearestHospitals: List<HospitalWithBearing>,
    hasDeviceFix: Boolean,
    deviceLat: Double?,
    deviceLon: Double?,
    accuracyMeters: Float?,
    provider: String?,
    fixAgeMs: Long?,
    onUseMyLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelMoss)
            .padding(14.dp)
    ) {
        Text(
            text = "NEAREST HOSPITAL",
            style = FieldType.statusLabel,
            color = SignalOrange
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = positionLine(hasDeviceFix, deviceLat, deviceLon, accuracyMeters, provider, fixAgeMs),
            style = FieldType.caption,
            color = if (hasDeviceFix) Bone else SeriousAmber
        )

        Spacer(modifier = Modifier.height(10.dp))
        FieldButton(
            text = if (hasDeviceFix) "UPDATE MY LOCATION" else "USE MY LOCATION",
            onClick = onUseMyLocation,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        val top = nearestHospitals.firstOrNull()
        if (top == null) {
            Text(
                text = "No position yet. Tap USE MY LOCATION, or allow location access, to estimate the nearest hospital.",
                style = FieldType.body,
                color = Bone
            )
        } else {
            NearestHero(top)
            val rest = nearestHospitals.drop(1)
            if (rest.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Other hospitals nearby", style = FieldType.caption, color = NeutralGray)
                rest.forEach { entry ->
                    Spacer(modifier = Modifier.height(8.dp))
                    HospitalRow(entry)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Straight-line estimate from an offline list — not turn-by-turn routing.",
            style = FieldType.caption,
            color = NeutralGray
        )
    }
}

@Composable
private fun NearestHero(entry: HospitalWithBearing) {
    val cardinal = GeoMath.bearingToCardinal(entry.bearingDegrees)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PanelDeep)
            .padding(14.dp)
    ) {
        Text(text = entry.hospital.name, style = FieldType.statusValue, color = Bone)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "➤",
                style = FieldType.heading,
                color = SignalOrange,
                modifier = Modifier.width(34.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "HEAD ${cardinalWords(cardinal)}",
                    style = FieldType.heading,
                    color = SignalOrange
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "%.1f km · %d° true".format(entry.distanceKm, entry.bearingDegrees.toInt()),
                    style = FieldType.body,
                    color = Bone
                )
            }
        }
    }
}

@Composable
private fun HospitalRow(entry: HospitalWithBearing) {
    val cardinal = GeoMath.bearingToCardinal(entry.bearingDegrees)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PanelDeep)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "➤",
            style = FieldType.body,
            color = SignalOrange,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.hospital.name, style = FieldType.body, color = Bone)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%.1f km · %d° ($cardinal)".format(entry.distanceKm, entry.bearingDegrees.toInt()),
                style = FieldType.caption,
                color = NeutralGray
            )
        }
    }
}

private fun cardinalWords(cardinal: String): String = when (cardinal) {
    "N" -> "NORTH"
    "NE" -> "NORTH-EAST"
    "E" -> "EAST"
    "SE" -> "SOUTH-EAST"
    "S" -> "SOUTH"
    "SW" -> "SOUTH-WEST"
    "W" -> "WEST"
    "NW" -> "NORTH-WEST"
    else -> cardinal
}

private fun positionLine(
    hasDeviceFix: Boolean,
    lat: Double?,
    lon: Double?,
    accuracyMeters: Float?,
    provider: String?,
    fixAgeMs: Long?
): String {
    if (!hasDeviceFix || lat == null || lon == null) {
        return "Using a cached approximate position. Tap below to estimate your real location from the GPS receiver."
    }
    val coords = "%.4f, %.4f".format(lat, lon)
    val acc = accuracyMeters?.let { " ±${it.toInt()} m" } ?: ""
    val src = provider?.let { " · ${it.uppercase()}" } ?: ""
    val age = ageLabel(fixAgeMs)
    return "Your position ~$coords$acc$src$age"
}

private fun ageLabel(fixAgeMs: Long?): String {
    if (fixAgeMs == null) return ""
    val minutes = fixAgeMs / 60_000
    return when {
        minutes < 1 -> " · live"
        minutes < 60 -> " · ${minutes}m old (cached)"
        else -> " · ${minutes / 60}h old (cached)"
    }
}
