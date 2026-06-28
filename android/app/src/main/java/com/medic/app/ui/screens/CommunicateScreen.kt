package com.medic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.medic.app.ui.theme.*

/**
 * P2.9 (translation) + P2.10 (SOS card), gated by the spec's "only if 1-2
 * are solid" -- both are intentionally minimal single-screen flows that
 * reuse AiService.translate()/generate() rather than introducing new
 * infrastructure, so they don't compete for time against Phase 1/2.
 */
@Composable
fun CommunicateScreen(
    medicText: String,
    onMedicTextChange: (String) -> Unit,
    casualtyTranslation: String,
    onTranslate: () -> Unit,
    sosInjury: String,
    sosPosition: String,
    sosPeopleAffected: String,
    sosNeeds: String,
    sosSeverity: String,
    onGenerateSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InkBlack)
            .padding(16.dp)
    ) {
        Text(text = "COMMUNICATE", style = FieldType.heading, color = OffWhite)
        Spacer(modifier = Modifier.height(16.dp))

        TranslationCard(
            medicText = medicText,
            onMedicTextChange = onMedicTextChange,
            casualtyTranslation = casualtyTranslation,
            onTranslate = onTranslate
        )

        Spacer(modifier = Modifier.height(20.dp))

        SosCard(
            injury = sosInjury,
            position = sosPosition,
            peopleAffected = sosPeopleAffected,
            needs = sosNeeds,
            severity = sosSeverity,
            onGenerate = onGenerateSos
        )
    }
}

@Composable
private fun TranslationCard(
    medicText: String,
    onMedicTextChange: (String) -> Unit,
    casualtyTranslation: String,
    onTranslate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .padding(14.dp)
    ) {
        Text(text = "MEDIC ↔ CASUALTY", style = FieldType.statusLabel, color = SignalTeal)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = medicText,
            onValueChange = onMedicTextChange,
            label = { Text("Type in your language", style = FieldType.caption) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = FieldType.body.copy(color = OffWhite)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onTranslate, modifier = Modifier.fillMaxWidth()) {
            Text("TRANSLATE")
        }
        if (casualtyTranslation.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(InkBlack)
                    .padding(10.dp)
            ) {
                Text(text = casualtyTranslation, style = FieldType.body, color = OffWhite)
            }
        }
    }
}

@Composable
private fun SosCard(
    injury: String,
    position: String,
    peopleAffected: String,
    needs: String,
    severity: String,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PanelDark)
            .padding(14.dp)
    ) {
        Text(text = "SOS / DISTRESS SUMMARY", style = FieldType.statusLabel, color = CriticalRed)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Drafts a short structured summary from your conversation so far -- " +
                "meant to be read aloud or shown to a rescuer quickly.",
            style = FieldType.caption
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
            Text("GENERATE SOS SUMMARY")
        }
        if (injury.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(InkBlack)
                    .padding(10.dp)
            ) {
                Column {
                    SosField("INJURY", injury)
                    SosField("APPROX POSITION", position)
                    SosField("PEOPLE AFFECTED", peopleAffected)
                    SosField("IMMEDIATE NEEDS", needs)
                    SosField("SEVERITY", severity)
                }
            }
        }
    }
}

@Composable
private fun SosField(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "$label: ", style = FieldType.statusLabel, color = NeutralGray)
        Text(text = value, style = FieldType.body, color = OffWhite)
    }
}
