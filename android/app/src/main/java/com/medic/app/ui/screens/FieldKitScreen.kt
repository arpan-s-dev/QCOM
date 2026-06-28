package com.medic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.medic.app.data.FieldKitItem
import com.medic.app.ui.theme.*

@Composable
fun FieldKitScreen(
    disclaimer: String,
    items: List<FieldKitItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FieldGreen)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "WHAT'S IN MY KIT", style = FieldType.heading, color = Bone)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = disclaimer,
                style = FieldType.caption,
                color = SeriousAmber
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(items, key = { it.name }) { item ->
            FieldKitItemCard(item)
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun FieldKitItemCard(item: FieldKitItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PanelMoss)
            .padding(14.dp)
    ) {
        Text(text = item.name, style = FieldType.statusLabel, color = SignalOrange)
        Spacer(modifier = Modifier.height(6.dp))
        LabelBlock("For", item.whatItIsFor)
        Spacer(modifier = Modifier.height(4.dp))
        LabelBlock("Use safely", item.howToUseSafely)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.warning,
            style = FieldType.caption,
            color = if (item.isMedicine) SeriousAmber else NeutralGray
        )
    }
}

@Composable
private fun LabelBlock(label: String, body: String) {
    Text(text = label.uppercase(), style = FieldType.caption, color = NeutralGray)
        Text(text = body, style = FieldType.body, color = Bone)
}
