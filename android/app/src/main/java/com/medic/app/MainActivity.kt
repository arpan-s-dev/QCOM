package com.medic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.medic.app.nav.DeviceOrientationReader
import com.medic.app.ui.MainViewModel
import com.medic.app.ui.components.LodestarShell
import com.medic.app.ui.theme.MedicOfflineTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicOfflineTheme {
                val state by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                val orientationReader = remember { DeviceOrientationReader(context) }
                DisposableEffect(Unit) {
                    orientationReader.start()
                    onDispose { orientationReader.stop() }
                }

                LodestarShell(
                    state = state,
                    orientationReader = orientationReader,
                    onSectionSelected = viewModel::onSectionSelected,
                    onInputChange = viewModel::onInputChange,
                    onSend = viewModel::onSend,
                    onMicToggle = viewModel::onMicToggle,
                    onSightSun = viewModel::onSightSun,
                    onMedicTextChange = viewModel::onMedicTextChange,
                    onTranslate = { viewModel.onTranslate() },
                    onGenerateSos = viewModel::onGenerateSos
                )
            }
        }
    }
}
