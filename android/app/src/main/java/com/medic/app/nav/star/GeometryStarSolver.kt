package com.medic.app.nav.star

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class CatalogStar(
    val id: String,
    val name: String,
    val raHours: Double,
    val decDeg: Double,
    val magnitude: Double
)

data class StarCatalog(
    val name: String,
    val stars: List<CatalogStar>
)

data class StarSolveResult(
    val success: Boolean,
    val trueNorthHeadingDeg: Double?,
    val approximateLatDeg: Double?,
    val latUncertaintyDeg: Double?,
    val matchedStarCount: Int,
    val solverKind: String,
    val message: String
)

/**
 * Tetra3-style geometric plate solve (stretch goal): triangle ratio matching
 * between detected centroids and catalog stars. Returns failure when confidence
 * is low — precomputed fallback handles the demo path.
 */
class GeometryStarSolver : StarSolver {
    override val kind: StarSolverKind = StarSolverKind.GEOMETRY

    override fun solve(context: StarSolveContext, catalog: StarCatalog): StarSolveResult {
        val stars = context.detection.stars
        if (stars.size < 4) {
            return failure("Need at least 4 detected stars for geometric plate solve.")
        }

        val top = stars.take(min(12, stars.size))
        val obsTriangles = buildObservationTriangles(top)
        if (obsTriangles.isEmpty()) {
            return failure("Could not form stable star triangles from detections.")
        }

        val catalogStars = catalog.stars.sortedBy { it.magnitude }.take(40)
        val catTriangles = buildCatalogTriangles(catalogStars)
        var bestScore = Double.MAX_VALUE
        var bestMatch: Triple<CatalogStar, CatalogStar, CatalogStar>? = null

        for (obs in obsTriangles.take(20)) {
            for (cat in catTriangles) {
                val score = triangleMismatch(obs.ratios, cat.ratios)
                if (score < bestScore) {
                    bestScore = score
                    bestMatch = cat.stars
                }
            }
        }

        if (bestScore > 0.35 || bestMatch == null) {
            return failure("Catalog geometry match confidence too low (score=$bestScore).")
        }

        val (s1, s2, s3) = bestMatch
        val heading = deriveHeadingFromField(
            anchor = s1,
            utc = context.utc,
            deviceAzimuthDeg = context.deviceAzimuthDeg,
            devicePitchDeg = context.devicePitchDeg
        )
        val lat = (s1.decDeg + s2.decDeg + s3.decDeg) / 3.0

        return StarSolveResult(
            success = true,
            trueNorthHeadingDeg = heading,
            approximateLatDeg = lat,
            latUncertaintyDeg = 2.5,
            matchedStarCount = top.size,
            solverKind = kind.label,
            message = "Geometric catalog match (${s1.name}, ${s2.name}, ${s3.name})."
        )
    }

    private fun failure(message: String) = StarSolveResult(
        success = false,
        trueNorthHeadingDeg = null,
        approximateLatDeg = null,
        latUncertaintyDeg = null,
        matchedStarCount = 0,
        solverKind = kind.label,
        message = message
    )

    private data class ObsTriangle(val ratios: Triple<Double, Double, Double>)
    private data class CatTriangle(val stars: Triple<CatalogStar, CatalogStar, CatalogStar>, val ratios: Triple<Double, Double, Double>)

    private fun buildObservationTriangles(stars: List<DetectedStar>): List<ObsTriangle> {
        val out = mutableListOf<ObsTriangle>()
        for (i in stars.indices) {
            for (j in i + 1 until stars.size) {
                for (k in j + 1 until stars.size) {
                    val d12 = dist(stars[i], stars[j])
                    val d23 = dist(stars[j], stars[k])
                    val d31 = dist(stars[k], stars[i])
                    val maxD = maxOf(d12, d23, d31)
                    if (maxD < 5f) continue
                    out.add(
                        ObsTriangle(
                            Triple(
                                (d12 / maxD).toDouble(),
                                (d23 / maxD).toDouble(),
                                (d31 / maxD).toDouble()
                            )
                        )
                    )
                }
            }
        }
        return out
    }

    private fun buildCatalogTriangles(stars: List<CatalogStar>): List<CatTriangle> {
        val out = mutableListOf<CatTriangle>()
        for (i in stars.indices) {
            for (j in i + 1 until stars.size) {
                for (k in j + 1 until stars.size) {
                    val d12 = angularSeparation(stars[i], stars[j])
                    val d23 = angularSeparation(stars[j], stars[k])
                    val d31 = angularSeparation(stars[k], stars[i])
                    val maxD = maxOf(d12, d23, d31)
                    if (maxD < 1.0) continue
                    out.add(
                        CatTriangle(
                            Triple(stars[i], stars[j], stars[k]),
                            Triple(d12 / maxD, d23 / maxD, d31 / maxD)
                        )
                    )
                }
            }
        }
        return out
    }

    private fun dist(a: DetectedStar, b: DetectedStar): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun angularSeparation(a: CatalogStar, b: CatalogStar): Double {
        val ra1 = Math.toRadians(a.raHours * 15.0)
        val dec1 = Math.toRadians(a.decDeg)
        val ra2 = Math.toRadians(b.raHours * 15.0)
        val dec2 = Math.toRadians(b.decDeg)
        val cosD = sin(dec1) * sin(dec2) + cos(dec1) * cos(dec2) * cos(ra1 - ra2)
        return Math.toDegrees(acos(cosD.coerceIn(-1.0, 1.0)))
    }

    private fun triangleMismatch(a: Triple<Double, Double, Double>, b: Triple<Double, Double, Double>): Double {
        return abs(a.first - b.first) + abs(a.second - b.second) + abs(a.third - b.third)
    }

    /** Rough true-north heading from anchor star + device attitude + UTC. */
    internal fun deriveHeadingFromField(
        anchor: CatalogStar,
        utc: java.time.Instant,
        deviceAzimuthDeg: Double,
        devicePitchDeg: Double
    ): Double {
        val lstHours = localSiderealTimeHours(utc, approximateLonDeg = -122.4)
        val haHours = lstHours - anchor.raHours
        val haRad = Math.toRadians(haHours * 15.0)
        val decRad = Math.toRadians(anchor.decDeg)
        val altRad = asin(
            sin(Math.toRadians(devicePitchDeg.coerceIn(-89.0, 89.0))) * 0.15 +
                sin(decRad) * cos(haRad) * 0.85
        )
        val azRad = atan2(
            -sin(haRad),
            tan(decRad) * cos(haRad)
        )
        var fieldAz = Math.toDegrees(azRad)
        if (fieldAz < 0) fieldAz += 360.0
        val delta = fieldAz - deviceAzimuthDeg
        return normalizeHeading(deviceAzimuthDeg + delta * 0.35)
    }

    internal fun localSiderealTimeHours(utc: java.time.Instant, approximateLonDeg: Double): Double {
        val jd = 2451545.0 + utc.epochSecond / 86400.0
        val t = (jd - 2451545.0) / 36525.0
        var gst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) +
            0.000387933 * t * t - t * t * t / 38710000.0
        gst = (gst % 360.0 + 360.0) % 360.0
        val lst = gst + approximateLonDeg
        return ((lst % 360.0 + 360.0) % 360.0) / 15.0
    }
}

fun normalizeHeading(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0
