package com.medic.app.nav

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class LocationSignal(
    val trustedLat: Double? = null,
    val trustedLon: Double? = null,
    val gpsAvailable: Boolean,
    val gpsSpoofed: Boolean
)

class LocationManagerCollector(context: Context) {

    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastTrustedFix: SpoofDetector.GpsFix? = null

    fun hasLocationPermission(): Boolean {
        val fine = appContext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = appContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    fun isGpsUsable(): Boolean =
        hasLocationPermission() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    @SuppressLint("MissingPermission")
    fun signals(
        minUpdateMillis: Long = 2_000L,
        minUpdateDistanceMeters: Float = 5f
    ): Flow<LocationSignal> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationSignal(gpsAvailable = false, gpsSpoofed = false))
            close()
            return@callbackFlow
        }

        fun emitUnavailable() {
            trySend(
                LocationSignal(
                    trustedLat = lastTrustedFix?.lat,
                    trustedLon = lastTrustedFix?.lon,
                    gpsAvailable = false,
                    gpsSpoofed = false
                )
            )
        }

        fun handleLocation(location: Location) {
            if (location.provider != LocationManager.GPS_PROVIDER) return

            val nextFix = SpoofDetector.GpsFix(
                lat = location.latitude,
                lon = location.longitude,
                timestampMs = location.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            )
            val previousTrustedFix = lastTrustedFix
            val gpsSpoofed = previousTrustedFix?.let { SpoofDetector.isImplausibleJump(it, nextFix) } ?: false

            if (!gpsSpoofed) {
                lastTrustedFix = nextFix
            }

            val trustedFix = lastTrustedFix ?: nextFix
            trySend(
                LocationSignal(
                    trustedLat = trustedFix.lat,
                    trustedLon = trustedFix.lon,
                    gpsAvailable = true,
                    gpsSpoofed = gpsSpoofed
                )
            )
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = handleLocation(location)

            override fun onProviderEnabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let(::handleLocation)
                }
            }

            override fun onProviderDisabled(provider: String) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    emitUnavailable()
                }
            }
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            emitUnavailable()
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            minUpdateMillis,
            minUpdateDistanceMeters,
            listener,
            Looper.getMainLooper()
        )
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let(::handleLocation)

        awaitClose { locationManager.removeUpdates(listener) }
    }
}
