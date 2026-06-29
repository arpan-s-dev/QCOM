package com.medic.app.ui.calm

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medic.app.demo.DemoScenarioSheet
import com.medic.app.ui.AppUiState
import com.medic.app.ui.screens.OrientNavMode
import com.medic.app.ui.theme.*
import kotlinx.coroutines.delay

enum class SgScreen(val title: String) {
    HOME("SafeGuide"),
    ASSISTANT("Assistant"),
    TRANSLATE("Translate"),
    LOCATION("My location"),
    MEDICAL("Medical help"),
    HOSPITAL("Nearby hospital")
}

/**
 * Calm consumer shell. The app opens with a brief intro animation, then a Home
 * that greets the user and puts the assistant front and center, with a
 * persistent bottom bar whose raised middle button is the assistant. All
 * business logic still lives in MainViewModel.
 */
@Composable
fun SafeGuideApp(
    state: AppUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicToggle: () -> Unit,
    onOrientNavModeChange: (OrientNavMode) -> Unit,
    onUseMyLocation: () -> Unit,
    onSightSun: () -> Unit,
    onPickNightSkyImage: () -> Unit,
    onAddWoundPhoto: () -> Unit,
    onSetSpoof: (Boolean) -> Unit,
    onMedicTextChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onRunDemoScenario: (com.medic.app.demo.DemoScenario) -> Unit,
    onClearDemoNavigation: () -> Unit,
    onExitDemoMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var screen by remember { mutableStateOf(SgScreen.HOME) }
    var showIntro by remember { mutableStateOf(true) }
    var showDemoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.demoNavigateTo) {
        state.demoNavigateTo?.let { key ->
            runCatching { SgScreen.valueOf(key) }.getOrNull()?.let { screen = it }
            onClearDemoNavigation()
        }
    }
    LaunchedEffect(Unit) {
        delay(1150)
        showIntro = false
    }

    Crossfade(targetState = showIntro, animationSpec = tween(500), label = "intro", modifier = modifier) { intro ->
        if (intro) {
            IntroSplash()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SgBg)
                    .statusBarsPadding()
            ) {
                if (screen == SgScreen.HOME) {
                    HomeTopBar(
                        demoActive = state.demoModeActive,
                        onDemoClick = { showDemoSheet = true }
                    )
                } else {
                    ScreenTopBar(
                        title = screen.title,
                        onBack = { screen = SgScreen.HOME },
                        demoActive = state.demoModeActive,
                        onDemoClick = { showDemoSheet = true }
                    )
                }

                if (state.demoBanner != null) {
                    DemoBanner(
                        text = state.demoBanner,
                        onDismiss = onExitDemoMode
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (screen) {
                        SgScreen.HOME -> HomeScreen(onSelect = { screen = it })
                        SgScreen.ASSISTANT -> AssistantScreen(state, onInputChange, onSend, onMicToggle)
                        SgScreen.TRANSLATE -> TranslateScreen(state, onMedicTextChange, onTranslate)
                        SgScreen.LOCATION -> FindNorthScreen(state, onOrientNavModeChange, onUseMyLocation, onSightSun, onPickNightSkyImage, onSetSpoof)
                        SgScreen.MEDICAL -> MedicalScreen(state, onAddWoundPhoto)
                        SgScreen.HOSPITAL -> HospitalScreen(
                            state = state,
                            onUseMyLocation = onUseMyLocation,
                            onGuideToCompass = { screen = SgScreen.LOCATION }
                        )
                    }
                }

                SgBottomBar(current = screen, onSelect = { screen = it })
            }

            DemoScenarioSheet(
                visible = showDemoSheet,
                activeScenarioId = state.activeDemoScenarioId,
                onDismiss = { showDemoSheet = false },
                onRunScenario = onRunDemoScenario
            )
        }
    }
}

@Composable
private fun IntroSplash() {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val scale by animateFloatAsState(if (started) 1f else 0.72f, animationSpec = tween(650), label = "splash-scale")
    val alpha by animateFloatAsState(if (started) 1f else 0f, animationSpec = tween(650), label = "splash-alpha")

    Box(
        modifier = Modifier.fillMaxSize().background(SgBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha).scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SgHospital.tile),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = SgHospital.icon, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("SafeGuide", color = SgText, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text("Offline survival assistant", color = SgTextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun HomeTopBar(demoActive: Boolean, onDemoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "SafeGuide", color = SgTextSecondary, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        OfflineStatusRow(demoActive = demoActive, onDemoClick = onDemoClick)
    }
}

@Composable
private fun ScreenTopBar(title: String, onBack: () -> Unit, demoActive: Boolean, onDemoClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SgText)
        }
        Spacer(Modifier.width(6.dp))
        Text(text = title, color = SgText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        OfflineStatusRow(demoActive = demoActive, onDemoClick = onDemoClick)
    }
}

@Composable
private fun DemoBanner(text: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SgAssistant.tile)
            .clickable(onClick = onDismiss)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("DEMO", color = SgAssistant.icon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(text, color = SgAssistant.title, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("×", color = SgTextMuted, fontSize = 16.sp)
    }
}

@Composable
fun OfflineStatusRow(demoActive: Boolean, onDemoClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DemoChip(active = demoActive, onClick = onDemoClick)
        OfflineBadge()
    }
}

@Composable
fun DemoChip(active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) SgBlue.copy(alpha = 0.22f) else SgRaised)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (active) "Demo on" else "Demo",
            color = if (active) SgBlue else SgTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OfflineBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SgHospital.tile)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(SgHospital.icon)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = "Offline ready", color = SgHospital.icon, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HomeScreen(onSelect: (SgScreen) -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val enter by animateFloatAsState(if (shown) 1f else 0f, animationSpec = tween(520), label = "home-enter")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .alpha(enter)
            .offset(y = ((1f - enter) * 18f).dp)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text(text = "How can I help you?", color = SgText, fontSize = 26.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(text = "Ask me anything, or pick a tool. No signal needed.", color = SgTextMuted, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))

        // Assistant hero — the center of the experience
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SgAssistant.tile)
                .clickable { onSelect(SgScreen.ASSISTANT) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SgAssistant.chip),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = SgAssistant.icon, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Ask the assistant", color = SgAssistant.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("Talk or type a question", color = SgAssistant.subtitle, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Tools", color = SgTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile("Translate", "Across languages", Icons.Filled.Translate, SgTranslate) { onSelect(SgScreen.TRANSLATE) }
            QuickTile("My location", "Position and north", Icons.Filled.Explore, SgFindNorth) { onSelect(SgScreen.LOCATION) }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile("Medical help", "First aid and kit", Icons.Filled.FavoriteBorder, SgMedical) { onSelect(SgScreen.MEDICAL) }
            QuickTile("Nearby hospital", "Closest care", Icons.Filled.LocalHospital, SgHospital) { onSelect(SgScreen.HOSPITAL) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RowScope.QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: SgAccent,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(accent.tile)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.chip),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent.icon, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, color = accent.title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = accent.subtitle, fontSize = 12.sp)
    }
}

/** Shared raised card used across the calm feature screens. */
@Composable
fun SgCard(modifier: Modifier = Modifier, fill: Color = SgSurface, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(fill)
            .padding(14.dp),
        content = content
    )
}

/**
 * Bottom navigation with the assistant as a raised middle button, flanked by
 * the four tools. Persistent on every screen including Home.
 */
@Composable
private fun SgBottomBar(current: SgScreen, onSelect: (SgScreen) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(SgSurface)) {
        HorizontalDivider(thickness = 0.5.dp, color = SgBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            SideNavItem(SgScreen.TRANSLATE, "Translate", Icons.Filled.Translate, current, onSelect, Modifier.weight(1f))
            SideNavItem(SgScreen.LOCATION, "Location", Icons.Filled.Explore, current, onSelect, Modifier.weight(1f))
            CenterAssistantItem(selected = current == SgScreen.ASSISTANT, onClick = { onSelect(SgScreen.ASSISTANT) }, modifier = Modifier.weight(1f))
            SideNavItem(SgScreen.MEDICAL, "Medical", Icons.Filled.FavoriteBorder, current, onSelect, Modifier.weight(1f))
            SideNavItem(SgScreen.HOSPITAL, "Hospital", Icons.Filled.LocalHospital, current, onSelect, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SideNavItem(
    screen: SgScreen,
    label: String,
    icon: ImageVector,
    current: SgScreen,
    onSelect: (SgScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (screen == current) SgBlue else SgTextMuted
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelect(screen) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = tint, fontSize = 11.sp)
    }
}

@Composable
private fun CenterAssistantItem(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(58.dp)
                .clip(CircleShape)
                .background(SgBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Assistant", tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Text(
            "Assistant",
            color = if (selected) SgBlue else SgTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.offset(y = (-8).dp)
        )
    }
}
