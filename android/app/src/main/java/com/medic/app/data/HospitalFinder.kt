package com.medic.app.data

/**
 * Offline nearest-hospital ranking from an approximate fix — no APIs, no routing.
 */
object HospitalFinder {

    fun nearest(
        hospitals: List<Hospital>,
        approxLat: Double,
        approxLon: Double,
        count: Int = 3
    ): List<HospitalWithBearing> {
        return hospitals
            .map { h ->
                HospitalWithBearing(
                    hospital = h,
                    distanceKm = GeoMath.distanceKm(approxLat, approxLon, h.latitude, h.longitude),
                    bearingDegrees = GeoMath.bearingDegrees(approxLat, approxLon, h.latitude, h.longitude)
                )
            }
            .sortedBy { it.distanceKm }
            .take(count)
    }
}
