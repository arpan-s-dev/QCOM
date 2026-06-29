package com.medic.app.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medic.app.ui.theme.*

@Composable
fun DemoScenarioSheet(
    visible: Boolean,
    activeScenarioId: String?,
    onDismiss: () -> Unit,
    onRunScenario: (DemoScenario) -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SgSurface,
        title = {
            Text("Demo scenarios", color = SgText, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        },
        text = {
            Column {
                Text(
                    "Tap a scenario. Works offline — no mic or NPU.",
                    color = SgTextMuted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DemoScenarios.all, key = { it.id }) { scenario ->
                        val selected = scenario.id == activeScenarioId
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) SgAssistant.tile else SgRaised)
                                .clickable {
                                    onRunScenario(scenario)
                                    onDismiss()
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                scenario.title,
                                color = if (selected) SgAssistant.title else SgText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(scenario.subtitle, color = SgTextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SgBlue)
            }
        }
    )
}
