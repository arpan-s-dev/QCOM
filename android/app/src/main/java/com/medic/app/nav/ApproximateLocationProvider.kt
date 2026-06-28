package com.medic.app.nav

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

/**
 * A single approximate position fix. [ageMillis] is how stale the fix is, so
 * the UI can be honest about "last cached GPS" vs. a live reading.
 */
data class ApproxFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val provider: String,
    val ageMillis: Long
)

/**
 * Reads the device's approximate position with plain [LocationManager] — no
 * Google Play Services, no network. GPS is receive-only hardware, so a
 * last-known fix is available even with the network down / airplane mode on;
 * that's exactly the warzone-disaster case this app targets.
 *
 * Two entry points:
 *  - [lastKnownApprox] returns the freshest cached fix instantly (may be old).
 *  - [requestFreshFix] asks an enabled provider for one new reading.
 *
 * Permission gating is the caller's job (MainActivity); every read here is
 * null-safe if permission is missing so it can never crash the screen.
 */
class ApproximateLocationProvider(private val context: Context) {

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasPermission(): Boolean {
        val fine = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
    }

    /** Freshest cached fix across all providers — instant, works offline. */
    @SuppressLint("MissingPermission")
    fun lastKnownApprox(): ApproxFix? {
        val lm = locationManager ?: return null
        if (!hasPermission()) return null
        return listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
            .mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }
            ?.toApproxFix()
    }

    /**
     * Requests a single fresh reading from GPS (falling back to network).
     * [onFix] is delivered on the main thread; [onUnavailable] fires when no
     * provider is enabled or permission is missing.
     */
    @SuppressLint("MissingPermission")
    fun requestFreshFix(onFix: (ApproxFix) -> Unit, onUnavailable: () -> Unit = {}) {
        val lm = locationManager
        if (lm == null || !hasPermission()) {
            onUnavailable(); return
        }
        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                onUnavailable(); return
            }
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lm.removeUpdates(this)
                onFix(location.toApproxFix())
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            @Deprecated("Required by LocationListener on older APIs")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        runCatching {
            lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        }.onFailure { onUnavailable() }
    }

    private fun Location.toApproxFix(): ApproxFix = ApproxFix(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else null,
        provider = provider ?: "unknown",
        ageMillis = (System.currentTimeMillis() - time).coerceAtLeast(0L)
    )
}
