package com.medic.app.nav

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.*

/**
 * NOAA/Meeus solar position algorithm. This is a direct logic port of
 * scripts/verify_solar_math.py, which was run and sanity-checked in the
 * build sandbox against ~37.3N, 121.9W (San Jose) for late June 2026:
 *   - solar noon azimuth ~179.5 deg (expect ~180, due south)        PASS
 *   - sunrise azimuth ~60.1 deg (expect ENE, 50-80 range)           PASS
 *   - sunset azimuth ~300.1 deg (expect WNW, 270-310 range)         PASS
 *   - midnight elevation ~-27.1 deg (expect negative)               PASS
 * See STATUS.md / verify_solar_math.py output for the full run.
 *
 * This Kotlin file has NOT itself been compiled/run (no Android toolchain
 * in the build sandbox) -- it's a careful logic-for-logic port of code that
 * WAS run and verified. Treat any future change to either file as
 * incomplete until the other is updated to match.
 */
object SolarMath {

    data class SunPosition(val azimuthDeg: Double, val elevationDeg: Double)

    fun julianDay(instant: Instant): Double {
        val utc = instant.atZone(ZoneOffset.UTC)
        var y = utc.year
        var m = utc.monthValue
        val d = utc.dayOfMonth
        val fracDay = (utc.hour + utc.minute / 60.0 + utc.second / 3600.0) / 24.0
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + fracDay + b - 1524.5
    }

    /**
     * Returns sun azimuth (deg clockwise from true north, 0=N/90=E/180=S/270=W)
     * and elevation (deg above horizon, negative = below) for the given
     * instant and observer lat/lon. No atmospheric refraction correction --
     * fine for a hiking compass, not for precision astronomy.
     */
    fun solarPosition(instant: Instant, latDeg: Double, lonDeg: Double): SunPosition {
        val jd = julianDay(instant)
        val jc = (jd - 2451545.0) / 36525.0

        val geomMeanLongSun = (280.46646 + jc * (36000.76983 + jc * 0.0003032)).mod(360.0)
        val geomMeanAnomSun = 357.52911 + jc * (35999.05029 - 0.0001537 * jc)
        val eccentEarthOrbit = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc)

        val mrad = Math.toRadians(geomMeanAnomSun)
        val sunEqOfCenter = (
            sin(mrad) * (1.914602 - jc * (0.004817 + 0.000014 * jc))
            + sin(2 * mrad) * (0.019993 - 0.000101 * jc)
            + sin(3 * mrad) * 0.000289
        )

        val sunTrueLong = geomMeanLongSun + sunEqOfCenter
        val omega = 125.04 - 1934.136 * jc
        val sunAppLong = sunTrueLong - 0.00569 - 0.00478 * sin(Math.toRadians(omega))

        val meanObliqEcliptic = 23 + (26 + (21.448 - jc * (46.815 + jc * (0.00059 - jc * 0.001813))) / 60) / 60
        val obliqCorr = meanObliqEcliptic + 0.00256 * cos(Math.toRadians(omega))

        val sunDeclin = Math.toDegrees(
            asin(sin(Math.toRadians(obliqCorr)) * sin(Math.toRadians(sunAppLong)))
        )

        val yTerm = tan(Math.toRadians(obliqCorr / 2)).pow(2)
        val eqTime = 4 * Math.toDegrees(
            yTerm * sin(2 * Math.toRadians(geomMeanLongSun))
            - 2 * eccentEarthOrbit * sin(mrad)
            + 4 * eccentEarthOrbit * yTerm * sin(mrad) * cos(2 * Math.toRadians(geomMeanLongSun))
            - 0.5 * yTerm * yTerm * sin(4 * Math.toRadians(geomMeanLongSun))
            - 1.25 * eccentEarthOrbit * eccentEarthOrbit * sin(2 * mrad)
        )

        val utc = instant.atZone(ZoneOffset.UTC)
        val timeOffsetMinutes = utc.hour * 60 + utc.minute + utc.second / 60.0
        var solarTimeMin = (timeOffsetMinutes + eqTime + 4 * lonDeg).mod(1440.0)

        val hourAngle = if (solarTimeMin >= 0) (solarTimeMin / 4) - 180 else (solarTimeMin / 4) + 180

        val latRad = Math.toRadians(latDeg)
        val declinRad = Math.toRadians(sunDeclin)
        val hourAngleRad = Math.toRadians(hourAngle)

        var cosZenith = sin(latRad) * sin(declinRad) + cos(latRad) * cos(declinRad) * cos(hourAngleRad)
        cosZenith = cosZenith.coerceIn(-1.0, 1.0)
        val zenithRad = acos(cosZenith)
        val elevationDeg = 90 - Math.toDegrees(zenithRad)

        val elevationRad = Math.toRadians(elevationDeg)
        val denom = cos(latRad) * cos(elevationRad)
        var cosAz = if (denom != 0.0) {
            (sin(declinRad) - sin(latRad) * sin(elevationRad)) / denom
        } else 0.0
        cosAz = cosAz.coerceIn(-1.0, 1.0)
        var azimuthDeg = Math.toDegrees(acos(cosAz))

        if (hourAngle > 0) {
            azimuthDeg = 360 - azimuthDeg
        }

        return SunPosition(azimuthDeg, elevationDeg)
    }
}

/**
 * Derives a true-north heading by having the user point their phone at the
 * sun. Given the known sun azimuth (from time + rough lat/lon) and the
 * phone's current compass/magnetometer bearing toward wherever it's
 * pointed, true north = (deviceBearingToSun - sunAzimuth) corrected mod 360
 * -- i.e. if the sun is at azimuth 120 (ENE-ish) and the user is holding
 * the phone pointed at azimuth-reading X when sighting the sun, the offset
 * between X and 120 is the magnetometer's error, which we can subtract out
 * for every other heading the device reports afterward.
 *
 * This matters in places with magnetic interference or where magnetic
 * declination tables are unavailable -- the sun doesn't lie.
 */
class SolarCompass(private val latDeg: Double, private val lonDeg: Double) {

    fun currentSunPosition(instant: Instant = Instant.now()): SolarMath.SunPosition =
        SolarMath.solarPosition(instant, latDeg, lonDeg)

    /**
     * [deviceBearingWhenSightingSun] is whatever raw (possibly inaccurate)
     * compass bearing the device reported at the moment the user visually
     * aligned the phone with the sun. Returns the correction offset to add
     * to all subsequent raw device bearings to get a true-north-referenced
     * heading.
     */
    fun computeMagnetometerCorrection(
        deviceBearingWhenSightingSun: Double,
        instant: Instant = Instant.now()
    ): Double {
        val sunAzimuth = currentSunPosition(instant).azimuthDeg
        var correction = sunAzimuth - deviceBearingWhenSightingSun
        // normalize to [-180, 180] for a sane "rotate by this much" value
        correction = ((correction + 180).mod(360.0)) - 180
        return correction
    }

    fun trueHeading(rawDeviceBearing: Double, correctionOffset: Double): Double {
        return (rawDeviceBearing + correctionOffset).mod(360.0)
    }

    /** True if the sun is usable for sighting right now (above horizon with margin). */
    fun isSunUsable(instant: Instant = Instant.now(), minElevationDeg: Double = 5.0): Boolean {
        return currentSunPosition(instant).elevationDeg >= minElevationDeg
    }
}
