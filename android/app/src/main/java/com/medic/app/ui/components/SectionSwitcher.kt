package com.medic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medic.app.ui.theme.*

enum class AppSection(val label: String) {
    TREAT("TREAT"),
    ORIENT("ORIENT"),
    COMMUNICATE("COMMUNICATE")
}

@Composable
fun SectionSwitcher(
    current: AppSection,
    onSelect: (AppSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(InkBlack)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppSection.values().forEach { section ->
            val selected = section == current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) SignalTeal else PanelDark)
                    .clickable { onSelect(section) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = section.label,
                    style = FieldType.statusLabel.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) InkBlack else NeutralGray
                )
            }
        }
    }
}
