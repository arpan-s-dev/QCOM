package com.medic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.medic.app.data.FieldKitItem
import com.medic.app.ui.components.ChatMessage
import com.medic.app.ui.components.ChatSurface
import com.medic.app.ui.theme.*

enum class TreatSubMode { TRIAGE, FIELD_KIT }

@Composable
fun TreatScreen(
    subMode: TreatSubMode,
    onSubModeChange: (TreatSubMode) -> Unit,
    messages: List<ChatMessage>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onMicToggle: () -> Unit,
    fieldKitDisclaimer: String,
    fieldKitItems: List<FieldKitItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TreatSubSwitcher(subMode = subMode, onSubModeChange = onSubModeChange)
        when (subMode) {
            TreatSubMode.TRIAGE -> ChatSurface(
                messages = messages,
                inputText = inputText,
                onInputChange = onInputChange,
                onSend = onSend,
                isListening = isListening,
                onMicToggle = onMicToggle,
                modifier = Modifier.fillMaxSize()
            )
            TreatSubMode.FIELD_KIT -> FieldKitScreen(
                disclaimer = fieldKitDisclaimer,
                items = fieldKitItems,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TreatSubSwitcher(
    subMode: TreatSubMode,
    onSubModeChange: (TreatSubMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelDeep)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TreatSubTab(
            label = "TRIAGE",
            selected = subMode == TreatSubMode.TRIAGE,
            onClick = { onSubModeChange(TreatSubMode.TRIAGE) },
            modifier = Modifier.weight(1f)
        )
        TreatSubTab(
            label = "FIELD KIT",
            selected = subMode == TreatSubMode.FIELD_KIT,
            onClick = { onSubModeChange(TreatSubMode.FIELD_KIT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TreatSubTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) SignalOrange else PanelMoss)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FieldType.statusLabel,
            color = if (selected) PanelDeep else NeutralGray
        )
    }
}
