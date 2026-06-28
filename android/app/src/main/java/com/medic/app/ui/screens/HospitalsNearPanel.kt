package com.medic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * Offline SF hospital list ranked by great-circle distance from an approximate fix.
 * Framed as guidance only — not live routing or "THE nearest."
 */
@Composable
fun HospitalsNearPanel(
    nearestHospitals: List<HospitalWithBearing>,
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
            text = "Hospitals near your approximate area",
            style = FieldType.statusLabel,
            color = SignalOrange
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Position is approximate (GPS denied). Distances are straight-line estimates — not turn-by-turn routing.",
            style = FieldType.caption,
            color = NeutralGray
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (nearestHospitals.isEmpty()) {
            Text(
                text = "No cached position yet. A last-known GPS fix is needed before hospital distances can be estimated.",
                style = FieldType.body,
                color = Bone
            )
        } else {
            nearestHospitals.forEachIndexed { index, entry ->
                if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                HospitalRow(entry)
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
            style = FieldType.heading,
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
