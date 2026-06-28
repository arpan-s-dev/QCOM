package com.medic.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
            .background(FieldGreen)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AppSection.entries.forEach { section ->
            SectionTab(
                section = section,
                selected = section == current,
                onSelect = onSelect,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SectionTab(
    section: AppSection,
    selected: Boolean,
    onSelect: (AppSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) SignalOrange else PanelMoss,
        animationSpec = LodestarMotion.colorCrossfade,
        label = "tab-bg-${section.name}"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) PanelDeep else NeutralGray,
        animationSpec = LodestarMotion.colorCrossfade,
        label = "tab-fg-${section.name}"
    )
    val verticalPad by animateDpAsState(
        targetValue = if (selected) 12.dp else 10.dp,
        animationSpec = LodestarMotion.dpCrossfade,
        label = "tab-pad-${section.name}"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable { onSelect(section) }
            .padding(vertical = verticalPad),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = section.label,
            style = FieldType.statusLabel.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}
