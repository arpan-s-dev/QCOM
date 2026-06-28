package com.medic.app.ui.calm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medic.app.data.FieldKitItem
import com.medic.app.data.GeoMath
import com.medic.app.data.HospitalWithBearing
import com.medic.app.nav.PositionSource
import com.medic.app.ui.AppUiState
import com.medic.app.ui.components.ChatMessage
import com.medic.app.ui.components.CompassRing
import com.medic.app.ui.components.Sender
import com.medic.app.ui.screens.OrientNavMode
import com.medic.app.ui.theme.*
import kotlin.math.roundToInt

// ----------------------------------------------------------------------------
// Ask the assistant
// ----------------------------------------------------------------------------

@Composable
fun AssistantScreen(
    state: AppUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(state.messages, key = { it.id }) { msg -> ChatBubble(msg) }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SgSurface)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message…", color = SgTextMuted) },
                shape = RoundedCornerShape(22.dp),
                colors = calmFieldColors()
            )
            Spacer(Modifier.width(10.dp))
            val hasText = state.inputText.isNotBlank()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (state.isListening) SgTeal else SgBlue)
                    .clickable { if (hasText) onSend() else onMicToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasText) Icons.AutoMirrored.Filled.Send else Icons.Filled.Mic,
                    contentDescription = if (hasText) "Send" else "Speak",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == Sender.USER
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) SgBlue else SgRaised)
                .padding(horizontal = 13.dp, vertical = 10.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isUser) Color.White else SgText,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Translate
// ----------------------------------------------------------------------------

@Composable
fun TranslateScreen(
    state: AppUiState,
    onMedicTextChange: (String) -> Unit,
    onTranslate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LangPill("English", Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SgTranslate.tile),
                contentAlignment = Alignment.Center
            ) { Text("⇄", color = SgTranslate.icon, fontSize = 18.sp) }
            LangPill("Spanish", Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))
        SgCard(fill = SgTranslate.tile) {
            Label("YOU SAY (ENGLISH)", SgTranslate.icon)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.medicText,
                onValueChange = onMedicTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Where does it hurt?", color = SgTranslate.subtitle) },
                textStyle = androidx.compose.ui.text.TextStyle(color = SgTranslate.title, fontSize = 17.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SgTranslate.icon,
                    unfocusedBorderColor = SgTranslate.chip,
                    cursorColor = SgTranslate.icon,
                    focusedContainerColor = Color.White.copy(alpha = 0.4f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.25f)
                )
            )
        }

        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SgTranslate.icon)
                .clickable(onClick = onTranslate)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Translate", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(12.dp))
        SgCard {
            Label("TRANSLATION (SPANISH)", SgTextSecondary)
            Spacer(Modifier.height(8.dp))
            val out = state.casualtyTranslation
            Text(
                text = out.ifBlank { "Type a phrase and tap Translate. The reply appears here for the other person to read." },
                color = if (out.isBlank()) SgTextMuted else SgText,
                fontSize = if (out.isBlank()) 14.sp else 19.sp,
                lineHeight = 26.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Runs on-device. A medic and an injured person can pass the phone back and forth.",
            color = SgTextMuted, fontSize = 12.sp
        )
    }
}

@Composable
private fun LangPill(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SgSurface)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = SgText, fontSize = 15.sp, fontWeight = FontWeight.Medium) }
}

// ----------------------------------------------------------------------------
// Find north
// ----------------------------------------------------------------------------

@Composable
fun FindNorthScreen(
    state: AppUiState,
    onOrientNavModeChange: (OrientNavMode) -> Unit,
    onUseMyLocation: () -> Unit,
    onSightSun: () -> Unit,
    onPickNightSkyImage: () -> Unit,
    onSetSpoof: (Boolean) -> Unit
) {
    val night = state.orientNavMode == OrientNavMode.NIGHT_SKY
    val spoofed = state.positionState.spoofDetected
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PositionSourceChip(state)
        if (spoofed) {
            Spacer(Modifier.height(10.dp))
            SpoofBanner()
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SgSurface)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Approximate location", color = SgTextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                Text(positionLine(state), color = SgText, fontSize = 14.sp, lineHeight = 19.sp)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SgRaised)
                    .clickable(onClick = onUseMyLocation)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text("Update", color = SgText, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
        }
        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (night) Color(0xFF0A1A2E) else SgSurface)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val heading = state.liveHeadingDeg ?: state.correctedHeadingDeg
                CompassRing(
                    headingDeg = heading ?: 0.0,
                    sunAzimuthDeg = if (!night) state.sunAzimuthDeg else null,
                    targetBearingDeg = state.nearestHospitals.firstOrNull()?.bearingDegrees
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (heading != null)
                        "${heading.roundToInt()}° ${GeoMath.bearingToCardinal(heading)}"
                    else "Point north",
                    color = SgText, fontSize = 30.sp, fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (night) "Import a night-sky photo to read true north"
                    else "Hold the phone flat and turn slowly",
                    color = if (night) Color(0xFF85B7EB) else SgTextSecondary, fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SgSurface)
                .padding(4.dp)
        ) {
            ToggleHalf("Day", Icons.Filled.WbSunny, selected = !night, Modifier.weight(1f)) {
                onOrientNavModeChange(OrientNavMode.SOLAR)
            }
            ToggleHalf("Night", Icons.Filled.DarkMode, selected = night, Modifier.weight(1f)) {
                onOrientNavModeChange(OrientNavMode.NIGHT_SKY)
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SgFindNorth.tile)
                .clickable { if (night) onPickNightSkyImage() else onSightSun() }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (night) "Import night-sky photo" else "Sight the sun to calibrate",
                color = SgFindNorth.title, fontSize = 15.sp, fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Auto-detects day or night. Toggle is here for the demo.",
            color = SgTextMuted, fontSize = 12.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        DemoSpoofControl(spoofed = spoofed, onSetSpoof = onSetSpoof)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PositionSourceChip(state: AppUiState) {
    val (label, bg, fg) = when {
        state.positionState.spoofDetected ->
            Triple("GPS spoofed · dead reckoning", SgMedical.tile, SgMedical.icon)
        state.positionState.source == PositionSource.GPS_TRUSTED ->
            Triple("GPS trusted", SgHospital.tile, SgHospital.icon)
        state.positionState.source == PositionSource.DEAD_RECKONING ->
            Triple("Dead reckoning", SgFindNorth.tile, SgFindNorth.icon)
        else ->
            Triple("Celestial fix only", SgFindNorth.tile, SgFindNorth.icon)
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SpoofBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SgMedical.tile)
            .padding(12.dp)
    ) {
        Text("GPS signal spoofed", color = SgMedical.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Position is frozen to the last trusted fix. Use the compass below to continue with dead reckoning.",
            color = SgMedical.subtitle,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DemoSpoofControl(spoofed: Boolean, onSetSpoof: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Demo control", color = SgTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (spoofed) SgHospital.tile else SgRaised)
                .clickable { onSetSpoof(!spoofed) }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (spoofed) "Restore GPS" else "Simulate GPS spoof",
                color = if (spoofed) SgHospital.icon else SgText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ToggleHalf(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SgBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Color.White else SgTextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = if (selected) Color.White else SgTextSecondary, fontSize = 15.sp)
    }
}

// ----------------------------------------------------------------------------
// Medical help
// ----------------------------------------------------------------------------

private data class FirstAidStep(val n: Int, val text: String)

private val severeBleeding = listOf(
    FirstAidStep(1, "Press hard on the wound with a clean cloth."),
    FirstAidStep(2, "Keep pressing for 10 minutes without lifting."),
    FirstAidStep(3, "Raise the wound above the heart and wrap it.")
)

@Composable
fun MedicalScreen(state: AppUiState, onAddWoundPhoto: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        WoundPhotoCard(state, onAddWoundPhoto)
        Spacer(Modifier.height(14.dp))

        SgCard(fill = SgMedical.tile) {
            Text("Severe bleeding", color = SgMedical.title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text("Follow these steps in order", color = SgMedical.subtitle, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        severeBleeding.forEach { step ->
            Row(modifier = Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(SgMedical.chip),
                    contentAlignment = Alignment.Center
                ) { Text("${step.n}", color = SgMedical.icon, fontWeight = FontWeight.Medium, fontSize = 14.sp) }
                Spacer(Modifier.width(12.dp))
                Text(step.text, color = SgText, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("What's in my kit", color = SgText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

        val items = state.fieldKitItems
        if (items.isEmpty()) {
            listOf("Bandages", "Gauze pads", "Antiseptic", "Tape", "Scissors", "Painkillers")
                .chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { name -> KitChip(name, false, Modifier.weight(1f)) {} }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
        } else {
            items.indices.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { i ->
                        KitChip(items[i].name, selected == i, Modifier.weight(1f)) {
                            selected = if (selected == i) null else i
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
            selected?.let { i -> KitDetail(items[i]) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (items.isEmpty()) "Reference only — not a diagnosis or prescription."
            else "Tap an item to see how to use it safely.",
            color = SgTextMuted, fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WoundPhotoCard(state: AppUiState, onAddWoundPhoto: () -> Unit) {
    SgCard {
        Text("Check a wound with a photo", color = SgText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Add a photo of the injury and the assistant flags possible infection signs. Reference only.",
            color = SgTextSecondary, fontSize = 13.sp, lineHeight = 18.sp
        )
        Spacer(Modifier.height(12.dp))

        val img = state.woundImage
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = "Wound photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SgMedical.icon)
                .clickable(onClick = onAddWoundPhoto)
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (img != null) "Replace photo" else "Add wound photo",
                color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium
            )
        }

        if (state.woundAnalyzing) {
            Spacer(Modifier.height(10.dp))
            Text("Analyzing photo…", color = SgTextSecondary, fontSize = 13.sp)
        }
        state.woundAssessment?.let { assessment ->
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SgRaised)
                    .padding(12.dp)
            ) {
                Text("Infection check", color = SgMedical.subtitle, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text(assessment, color = SgText, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun KitChip(name: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SgMedical.tile else SgRaised)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(name, color = if (selected) SgMedical.title else SgText, fontSize = 13.sp)
    }
}

@Composable
private fun KitDetail(item: FieldKitItem) {
    Spacer(Modifier.height(4.dp))
    SgCard {
        Text(item.name, color = SgText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(item.whatItIsFor, color = SgTextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(6.dp))
        Text(item.howToUseSafely, color = SgText, fontSize = 14.sp, lineHeight = 20.sp)
        if (item.warning.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("⚠ ${item.warning}", color = SgMedical.subtitle, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
    Spacer(Modifier.height(8.dp))
}

// ----------------------------------------------------------------------------
// Nearby hospital
// ----------------------------------------------------------------------------

@Composable
fun HospitalScreen(
    state: AppUiState,
    onUseMyLocation: () -> Unit,
    onGuideToCompass: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        val top = state.nearestHospitals.firstOrNull()
        if (top != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SgHospital.tile)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("↗", color = SgHospital.icon, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Head ${cardinalWords(GeoMath.bearingToCardinal(top.bearingDegrees))}",
                        color = SgHospital.title, fontSize = 17.sp, fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Closest hospital is ${"%.1f".format(top.distanceKm)} km away",
                        color = SgHospital.subtitle, fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Text(positionLine(state), color = if (state.hasDeviceFix) SgTextSecondary else SgFindNorth.subtitle, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SgRaised)
                .clickable(onClick = onUseMyLocation)
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (state.hasDeviceFix) "Update my location" else "Use my location",
                color = SgText, fontSize = 15.sp, fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(14.dp))
        if (state.nearestHospitals.isEmpty()) {
            Text(
                "No position yet. Tap “Use my location” to estimate the nearest hospital.",
                color = SgText, fontSize = 14.sp
            )
        } else {
            state.nearestHospitals.forEachIndexed { index, entry ->
                HospitalCard(entry, primary = index == 0, onGuide = onGuideToCompass)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "⊙ Saved offline · straight-line estimate, not turn-by-turn routing.",
            color = SgTextMuted, fontSize = 12.sp
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HospitalCard(entry: HospitalWithBearing, primary: Boolean, onGuide: () -> Unit) {
    val cardinal = GeoMath.bearingToCardinal(entry.bearingDegrees)
    val walkMin = (entry.distanceKm / 5.0 * 60.0).roundToInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SgSurface)
            .border(0.5.dp, SgBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(entry.hospital.name, color = SgText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text(
            "↗ ${"%.1f".format(entry.distanceKm)} km · $cardinal · ~$walkMin min walk",
            color = SgTextSecondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (primary) SgTeal else SgRaised)
                .clickable(onClick = onGuide)
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                "Guide me there",
                color = if (primary) Color.White else SgText,
                fontSize = 13.sp, fontWeight = FontWeight.Medium
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Shared helpers
// ----------------------------------------------------------------------------

@Composable
private fun Label(text: String, color: Color) {
    Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
}

@Composable
private fun calmFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SgBlue,
    unfocusedBorderColor = SgBorder,
    cursorColor = SgBlue,
    focusedContainerColor = SgRaised,
    unfocusedContainerColor = SgRaised,
    focusedTextColor = SgText,
    unfocusedTextColor = SgText
)

private fun cardinalWords(cardinal: String): String = when (cardinal) {
    "N" -> "north"; "NE" -> "north-east"; "E" -> "east"; "SE" -> "south-east"
    "S" -> "south"; "SW" -> "south-west"; "W" -> "west"; "NW" -> "north-west"
    else -> cardinal
}

private fun positionLine(state: AppUiState): String {
    if (!state.hasDeviceFix) {
        return "Using a cached approximate position. Tap below for a real GPS fix."
    }
    val lat = state.positionState.lastTrustedLat
    val lon = state.positionState.lastTrustedLon
    val coords = if (lat != null && lon != null) "%.4f, %.4f".format(lat, lon) else "located"
    val acc = state.deviceFixAccuracyM?.let { " ±${it.roundToInt()} m" } ?: ""
    return "Your position ~$coords$acc"
}
