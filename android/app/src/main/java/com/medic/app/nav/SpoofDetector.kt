package com.medic.app.nav

import kotlin.math.*

/**
 * Detects implausible GPS jumps by comparing the distance between
 * consecutive GPS fixes against what's physically possible given elapsed
 * time and a generous max-speed assumption (covers walking, running, and
 * vehicle transport -- this is a field medic/disaster-response context,
 * not flight tracking).
 *
 * This is intentionally a simple speed-gate heuristic, not a Kalman filter
 * or RAIM-style consistency check against multiple satellites/constellations.
 * That's a real limitation for v1: a slow, gradual spoof (meaconing that
 * drifts the position slowly rather than jumping it) would NOT be caught by
 * this gate. Flagged honestly in STATUS.md and DEMO.md.
 */
object SpoofDetector {

    // Generous upper bound: ~250 km/h covers ground vehicles and most
    // light aircraft, intentionally loose to minimize false positives in
    // a demo. Tighten per-deployment if the expected casualty population
    // is known to be on-foot only.
    private const val MAX_PLAUSIBLE_SPEED_MPS = 70.0 // ~250 km/h

    data class GpsFix(val lat: Double, val lon: Double, val timestampMs: Long)

    /** Haversine distance in meters between two lat/lon points. */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius, meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Returns true if [next] represents an implausible jump from [previous]
     * given elapsed time, i.e. it would require exceeding
     * MAX_PLAUSIBLE_SPEED_MPS to be a real GPS fix.
     */
    fun isImplausibleJump(previous: GpsFix, next: GpsFix): Boolean {
        val elapsedSeconds = (next.timestampMs - previous.timestampMs) / 1000.0
        if (elapsedSeconds <= 0) return false // can't evaluate, don't false-positive

        val distance = distanceMeters(previous.lat, previous.lon, next.lat, next.lon)
        val requiredSpeed = distance / elapsedSeconds
        return requiredSpeed > MAX_PLAUSIBLE_SPEED_MPS
    }
}
