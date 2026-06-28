package com.medic.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.medic.app.ui.theme.*

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
            .background(FieldGreen)
            .padding(16.dp)
    ) {
        Text(text = "COMMUNICATE", style = FieldType.heading, color = Bone)
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
            .clip(RoundedCornerShape(8.dp))
            .background(PanelMoss)
            .padding(14.dp)
    ) {
        Text(text = "MEDIC ↔ CASUALTY", style = FieldType.statusLabel, color = SignalOrange)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = medicText,
            onValueChange = onMedicTextChange,
            label = { Text("Type in your language", style = FieldType.caption) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = FieldType.body.copy(color = Bone),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SignalOrange,
                unfocusedBorderColor = PanelBorder,
                cursorColor = SignalOrange,
                focusedContainerColor = PanelDeep,
                unfocusedContainerColor = PanelDeep
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        FieldButton(
            text = "TRANSLATE",
            onClick = onTranslate,
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedVisibility(
            visible = casualtyTranslation.isNotBlank(),
            enter = fadeIn(LodestarMotion.messageFade) +
                slideInVertically(
                    animationSpec = LodestarMotion.messageSlide,
                    initialOffsetY = { it / 4 }
                )
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDeep)
                        .padding(10.dp)
                ) {
                    Text(text = casualtyTranslation, style = FieldType.body, color = Bone)
                }
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
            .clip(RoundedCornerShape(8.dp))
            .background(PanelMoss)
            .padding(14.dp)
    ) {
        Text(text = "SOS / DISTRESS SUMMARY", style = FieldType.statusLabel, color = CriticalRed)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Drafts a short structured summary from your conversation so far — " +
                "meant to be read aloud or shown to a rescuer quickly.",
            style = FieldType.caption
        )
        Spacer(modifier = Modifier.height(10.dp))
        FieldButton(
            text = "GENERATE SOS SUMMARY",
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth(),
            variant = FieldButtonVariant.ALERT
        )
        AnimatedVisibility(
            visible = injury.isNotBlank(),
            enter = fadeIn(LodestarMotion.messageFade) +
                slideInVertically(
                    animationSpec = LodestarMotion.messageSlide,
                    initialOffsetY = { it / 4 }
                )
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDeep)
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
}

@Composable
private fun SosField(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "$label: ", style = FieldType.statusLabel, color = NeutralGray)
        Text(text = value, style = FieldType.body, color = Bone)
    }
}
