package com.medic.app.nav

/**
 * Position source state machine. Order reflects trust/preference, not time:
 * the app always prefers GPS_TRUSTED, falls back to DEAD_RECKONING when GPS
 * is unavailable or flagged as spoofed, and falls back further to
 * SOLAR_FIX (heading only, no absolute position) when DR has drifted too
 * long without a GPS/landmark correction to bound its error. STAR_FIX is
 * the night-time counterpart — heading from star-field plate solve.
 */
enum class PositionSource {
    GPS_TRUSTED,
    DEAD_RECKONING,
    SOLAR_FIX,
    STAR_FIX
}

data class PositionState(
    val source: PositionSource,
    val spoofDetected: Boolean = false,
    val lastTrustedLat: Double? = null,
    val lastTrustedLon: Double? = null,
    val drElapsedSeconds: Long = 0,
    val headingDegrees: Float? = null
)

/**
 * Pure state-transition function -- no Android/location APIs here, so it's
 * unit-testable on its own. The actual GPS/IMU reading happens in a
 * separate LocationManager-backed class that calls into this.
 */
object PositionStateMachine {

    // If dead-reckoning runs uncorrected longer than this, drift error is
    // assumed too large to trust as a "position" and we fall back to
    // SOLAR_FIX (heading-only guidance) instead of presenting a wrong dot
    // on the map with false confidence.
    private const val MAX_DR_SECONDS_BEFORE_SOLAR_FALLBACK = 600L // 10 minutes

    fun transition(current: PositionState, gpsAvailable: Boolean, gpsSpoofed: Boolean): PositionState {
        return when {
            gpsAvailable && !gpsSpoofed -> current.copy(
                source = PositionSource.GPS_TRUSTED,
                spoofDetected = false,
                drElapsedSeconds = 0
            )

            gpsSpoofed -> {
                // Freeze to last trusted fix, start/continue dead reckoning from there.
                current.copy(
                    source = PositionSource.DEAD_RECKONING,
                    spoofDetected = true
                )
            }

            current.source == PositionSource.GPS_TRUSTED -> {
                // GPS just became unavailable (not spoofed, just gone -- e.g. indoors/canyon).
                current.copy(source = PositionSource.DEAD_RECKONING, spoofDetected = false)
            }

            current.source == PositionSource.DEAD_RECKONING -> {
                if (current.drElapsedSeconds >= MAX_DR_SECONDS_BEFORE_SOLAR_FALLBACK) {
                    current.copy(source = PositionSource.SOLAR_FIX)
                } else {
                    current
                }
            }

            else -> current
        }
    }
}
