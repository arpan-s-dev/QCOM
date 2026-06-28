package com.medic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.medic.app.nav.DeviceOrientationReader
import com.medic.app.ui.MainViewModel
import com.medic.app.ui.components.AppSection
import com.medic.app.ui.components.ChatSurface
import com.medic.app.ui.components.SectionSwitcher
import com.medic.app.ui.components.StatusStrip
import com.medic.app.ui.screens.CommunicateScreen
import com.medic.app.ui.screens.OrientScreen
import com.medic.app.ui.theme.MedicOfflineTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicOfflineTheme {
                val state by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                // Orientation sensor is Activity-lifecycle-bound (start/stop
                // tied to this composition's lifecycle via DisposableEffect),
                // not owned by the ViewModel, since it's plain Android sensor
                // plumbing rather than app state that should survive config
                // changes on its own.
                val orientationReader = remember { DeviceOrientationReader(context) }
                DisposableEffect(Unit) {
                    orientationReader.start()
                    onDispose { orientationReader.stop() }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // P2.0b: the signature status strip, ALWAYS visible
                    // regardless of which section is active below.
                    StatusStrip(
                        positionSource = state.positionState.source,
                        spoofDetected = state.positionState.spoofDetected,
                        headingDegrees = state.positionState.headingDegrees,
                        airplaneModeOn = state.airplaneModeOn
                    )

                    SectionSwitcher(
                        current = state.section,
                        onSelect = viewModel::onSectionSelected
                    )

                    when (state.section) {
                        AppSection.TREAT -> ChatSurface(
                            messages = state.messages,
                            inputText = state.inputText,
                            onInputChange = viewModel::onInputChange,
                            onSend = viewModel::onSend,
                            isListening = state.isListening,
                            onMicToggle = viewModel::onMicToggle,
                            modifier = Modifier.fillMaxSize()
                        )

                        AppSection.ORIENT -> OrientScreen(
                            positionSource = state.positionState.source,
                            sunAzimuthDeg = state.sunAzimuthDeg,
                            sunElevationDeg = state.sunElevationDeg,
                            correctedHeadingDeg = state.correctedHeadingDeg,
                            onSightSun = {
                                // Read whatever the rotation-vector sensor reports
                                // right now and hand it to the ViewModel, which
                                // does the actual sun-azimuth-vs-device-bearing
                                // correction math (SolarCompass).
                                viewModel.onSightSun(orientationReader.currentBearingDeg)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        AppSection.COMMUNICATE -> CommunicateScreen(
                            medicText = state.medicText,
                            onMedicTextChange = viewModel::onMedicTextChange,
                            casualtyTranslation = state.casualtyTranslation,
                            onTranslate = { viewModel.onTranslate() },
                            sosInjury = state.sosSummary.injury,
                            sosPosition = state.sosSummary.position,
                            sosPeopleAffected = state.sosSummary.peopleAffected,
                            sosNeeds = state.sosSummary.needs,
                            sosSeverity = state.sosSummary.severity,
                            onGenerateSos = viewModel::onGenerateSos,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
