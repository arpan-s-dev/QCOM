package com.medic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.medic.app.core.Severity
import com.medic.app.ui.theme.*

enum class Sender { USER, ASSISTANT, SYSTEM }

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val severity: Severity? = null,   // set on ASSISTANT messages that came from the safety tree
    val citedChunkIds: List<String> = emptyList(),
    val disclaimerShown: Boolean = false
)

@Composable
fun ChatSurface(
    messages: List<ChatMessage>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onMicToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(msg)
            }
        }
        ChatInputBar(
            inputText = inputText,
            onInputChange = onInputChange,
            onSend = onSend,
            isListening = isListening,
            onMicToggle = onMicToggle
        )
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.sender == Sender.USER
    val bubbleColor = when (msg.sender) {
        Sender.USER -> PanelBorder
        Sender.ASSISTANT -> PanelDark
        Sender.SYSTEM -> InkBlack
    }
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (msg.severity != null) {
            SeverityTag(msg.severity)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .widthIn(max = 320.dp)
                .padding(12.dp)
        ) {
            Column {
                Text(text = msg.text, style = FieldType.body, color = OffWhite)
                if (msg.citedChunkIds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "sources: " + msg.citedChunkIds.joinToString(", "),
                        style = FieldType.caption
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityTag(severity: Severity) {
    val (color, label) = when (severity) {
        Severity.CRITICAL -> CriticalRed to "CRITICAL"
        Severity.SERIOUS -> SeriousAmber to "SERIOUS"
        Severity.MODERATE -> ModerateYellow to "MODERATE"
        Severity.MINOR -> MinorGreen to "MINOR"
        Severity.UNKNOWN -> NeutralGray to "UNCLEAR — describe more"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, style = FieldType.statusLabel, color = color)
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onMicToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelDark)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MicButton(isListening = isListening, onToggle = onMicToggle)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Describe the injury…", style = FieldType.body, color = NeutralGray) },
            textStyle = FieldType.body.copy(color = OffWhite),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SignalTeal,
                unfocusedBorderColor = PanelBorder
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSend) {
            Icon(Icons.Filled.Send, contentDescription = "Send", tint = SignalTeal)
        }
    }
}

@Composable
private fun MicButton(isListening: Boolean, onToggle: () -> Unit) {
    val bg = if (isListening) CriticalRed else PanelBorder
    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = if (isListening) "Stop listening" else "Start voice input",
                tint = if (isListening) Color.White else SignalTeal
            )
        }
    }
}
