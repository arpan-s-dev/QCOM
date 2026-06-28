package com.medic.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.medic.app.nav.DeviceOrientationReader
import com.medic.app.nav.SpoofDetector
import com.medic.app.ui.MainViewModel
import com.medic.app.ui.components.LodestarShell
import com.medic.app.ui.theme.MedicOfflineTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var orientationReader: DeviceOrientationReader
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null
    private var lastTrustedFix: SpoofDetector.GpsFix? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            startLocationUpdates()
        } else {
            viewModel.updatePositionState(gpsAvailable = false, gpsSpoofed = false)
        }
    }

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
        locationManager = getSystemService(LocationManager::class.java)
        viewModel.updatePositionState(gpsAvailable = false, gpsSpoofed = false)

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

    override fun onStart() {
        super.onStart()
        ensureLocationUpdates()
    }

    override fun onStop() {
        stopLocationUpdates()
        super.onStop()
    }

    private fun ensureLocationUpdates() {
        if (hasLocationPermission()) {
            startLocationUpdates()
            return
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        stopLocationUpdates()
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                handleLocation(location)
            }

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    viewModel.updatePositionState(gpsAvailable = false, gpsSpoofed = false)
                }
            }
        }
        locationListener = listener

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1_000L,
                1f,
                listener,
                Looper.getMainLooper()
            )
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let(::handleLocation)
        } else {
            viewModel.updatePositionState(gpsAvailable = false, gpsSpoofed = false)
        }
    }

    private fun stopLocationUpdates() {
        locationListener?.let(locationManager::removeUpdates)
        locationListener = null
    }

    private fun handleLocation(location: Location) {
        val nextFix = SpoofDetector.GpsFix(
            lat = location.latitude,
            lon = location.longitude,
            timestampMs = location.time
        )
        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
        val jumpDetected = lastTrustedFix?.let { SpoofDetector.isImplausibleJump(it, nextFix) } ?: false
        val gpsSpoofed = isMock || jumpDetected

        if (!gpsSpoofed) {
            lastTrustedFix = nextFix
            viewModel.updateRoughLocation(nextFix.lat, nextFix.lon)
        }
        viewModel.updatePositionState(gpsAvailable = true, gpsSpoofed = gpsSpoofed)
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }
}
