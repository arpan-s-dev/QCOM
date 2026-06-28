package com.medic.app

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.facebook.soloader.SoLoader
import com.medic.app.nav.ApproximateLocationProvider
import com.medic.app.nav.DeviceOrientationReader
import com.medic.app.ui.MainViewModel
import com.medic.app.ui.calm.SafeGuideApp
import com.medic.app.ui.theme.SafeGuideTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var orientationReader: DeviceOrientationReader
    private lateinit var locationProvider: ApproximateLocationProvider

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

    private val pickMedicalImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onMedicalImageSelected(it) }
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) refreshDeviceLocation()
    }

    /** Ensure we have permission, then read the device's position into the VM. */
    private fun ensureDeviceLocation() {
        if (locationProvider.hasPermission()) {
            refreshDeviceLocation()
        } else {
            requestLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** Push the cached fix immediately (instant, offline), then a fresh one. */
    private fun refreshDeviceLocation() {
        locationProvider.lastKnownApprox()?.let { fix ->
            viewModel.updateRoughLocation(
                fix.latitude, fix.longitude, fix.accuracyMeters, fix.provider, fix.ageMillis
            )
        }
        locationProvider.requestFreshFix(onFix = { fix ->
            viewModel.updateRoughLocation(
                fix.latitude, fix.longitude, fix.accuracyMeters, fix.provider, fix.ageMillis
            )
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoLoader.init(this, false)
        try {
            Os.setenv("ADSP_LIBRARY_PATH", applicationInfo.nativeLibraryDir, true)
            Os.setenv("LD_LIBRARY_PATH", applicationInfo.nativeLibraryDir, true)
        } catch (e: ErrnoException) {
            Log.e("MainActivity", "QNN native library path setup failed", e)
        }
        orientationReader = DeviceOrientationReader(this)
        locationProvider = ApproximateLocationProvider(this)
        ensureDeviceLocation()

        setContent {
            SafeGuideTheme {
                val state by viewModel.uiState.collectAsState()

                DisposableEffect(Unit) {
                    orientationReader.start()
                    onDispose { orientationReader.stop() }
                }

                // Stream the live compass heading into the UI ~10x/sec.
                LaunchedEffect(Unit) {
                    while (true) {
                        viewModel.updateLiveHeading(orientationReader.currentBearingDeg)
                        kotlinx.coroutines.delay(100)
                    }
                }

                SafeGuideApp(
                    state = state,
                    onInputChange = viewModel::onInputChange,
                    onSend = viewModel::onSend,
                    onMicToggle = viewModel::onMicToggle,
                    onOrientNavModeChange = viewModel::onOrientNavModeChange,
                    onUseMyLocation = { ensureDeviceLocation() },
                    onSightSun = { viewModel.onSightSun(orientationReader.currentBearingDeg) },
                    onPickNightSkyImage = {
                        pickNightSkyImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddWoundPhoto = {
                        pickMedicalImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onSetSpoof = viewModel::setSpoofDemo,
                    onMedicTextChange = viewModel::onMedicTextChange,
                    onTranslate = { viewModel.onTranslate() }
                )
            }
        }
    }
}
