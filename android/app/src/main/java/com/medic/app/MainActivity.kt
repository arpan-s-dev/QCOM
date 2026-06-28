package com.medic.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.medic.app.nav.DeviceOrientationReader
import com.medic.app.ui.MainViewModel
import com.medic.app.ui.components.LodestarShell
import com.medic.app.ui.theme.MedicOfflineTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var orientationReader: DeviceOrientationReader

    private val pickNightSkyImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onNightSkyImageSelected(
                uri = it,
                deviceAzimuthDeg = orientationReader.currentBearingDeg,
                devicePitchDeg = orientationReader.currentPitchDeg
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orientationReader = DeviceOrientationReader(this)

        setContent {
            MedicOfflineTheme {
                val state by viewModel.uiState.collectAsState()

                DisposableEffect(Unit) {
                    orientationReader.start()
                    onDispose { orientationReader.stop() }
                }

                LodestarShell(
                    state = state,
                    onSectionSelected = viewModel::onSectionSelected,
                    onTreatSubModeChange = viewModel::onTreatSubModeChange,
                    onInputChange = viewModel::onInputChange,
                    onSend = viewModel::onSend,
                    onMicToggle = viewModel::onMicToggle,
                    onOrientNavModeChange = viewModel::onOrientNavModeChange,
                    onSightSun = { viewModel.onSightSun(orientationReader.currentBearingDeg) },
                    onPickNightSkyImage = {
                        pickNightSkyImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onMedicTextChange = viewModel::onMedicTextChange,
                    onTranslate = { viewModel.onTranslate() },
                    onGenerateSos = viewModel::onGenerateSos
                )
            }
        }
    }
}
