package com.medic.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.medic.app.nav.DeviceOrientationReader
import com.medic.app.ui.AppUiState
import com.medic.app.ui.screens.CommunicateScreen
import com.medic.app.ui.screens.OrientScreen
import com.medic.app.ui.theme.LodestarMotion

private fun sectionIndex(section: AppSection): Int =
    AppSection.entries.indexOf(section)

@Composable
fun LodestarShell(
    state: AppUiState,
    orientationReader: DeviceOrientationReader,
    onSectionSelected: (AppSection) -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicToggle: () -> Unit,
    onSightSun: (Float) -> Unit,
    onMedicTextChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onGenerateSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        StatusStrip(
            positionSource = state.positionState.source,
            spoofDetected = state.positionState.spoofDetected,
            headingDegrees = state.positionState.headingDegrees,
            airplaneModeOn = state.airplaneModeOn
        )

        SectionSwitcher(
            current = state.section,
            onSelect = onSectionSelected
        )

        AnimatedContent(
            targetState = state.section,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val forward = sectionIndex(targetState) > sectionIndex(initialState)
                val offset = { fullWidth: Int -> if (forward) fullWidth / 4 else -fullWidth / 4 }
                (fadeIn(LodestarMotion.tabEnter) + slideInHorizontally(initialOffsetX = offset))
                    .togetherWith(fadeOut(LodestarMotion.tabExit) + slideOutHorizontally(targetOffsetX = offset))
            },
            label = "lodestar-section"
        ) { section ->
            when (section) {
                AppSection.TREAT -> ChatSurface(
                    messages = state.messages,
                    inputText = state.inputText,
                    onInputChange = onInputChange,
                    onSend = onSend,
                    isListening = state.isListening,
                    onMicToggle = onMicToggle,
                    modifier = Modifier.fillMaxSize()
                )

                AppSection.ORIENT -> OrientScreen(
                    positionSource = state.positionState.source,
                    sunAzimuthDeg = state.sunAzimuthDeg,
                    sunElevationDeg = state.sunElevationDeg,
                    correctedHeadingDeg = state.correctedHeadingDeg,
                    onSightSun = { onSightSun(orientationReader.currentBearingDeg) },
                    modifier = Modifier.fillMaxSize()
                )

                AppSection.COMMUNICATE -> CommunicateScreen(
                    medicText = state.medicText,
                    onMedicTextChange = onMedicTextChange,
                    casualtyTranslation = state.casualtyTranslation,
                    onTranslate = onTranslate,
                    sosInjury = state.sosSummary.injury,
                    sosPosition = state.sosSummary.position,
                    sosPeopleAffected = state.sosSummary.peopleAffected,
                    sosNeeds = state.sosSummary.needs,
                    sosSeverity = state.sosSummary.severity,
                    onGenerateSos = onGenerateSos,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
