package com.medic.app.data

import kotlin.math.*

/**
 * Great-circle distance and initial bearing — offline, no map tiles or APIs.
 */
object GeoMath {

    private const val EARTH_RADIUS_KM = 6371.0

    /** Haversine distance in kilometres. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /** Initial compass bearing from point 1 to point 2, degrees 0–360 (true north). */
    fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    fun bearingToCardinal(degrees: Double): String = when {
        degrees >= 337.5 || degrees < 22.5 -> "N"
        degrees < 67.5 -> "NE"
        degrees < 112.5 -> "E"
        degrees < 157.5 -> "SE"
        degrees < 202.5 -> "S"
        degrees < 247.5 -> "SW"
        degrees < 292.5 -> "W"
        else -> "NW"
    }
}
