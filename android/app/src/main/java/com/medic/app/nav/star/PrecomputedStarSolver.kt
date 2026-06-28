package com.medic.app.nav.star

import android.content.Context
import org.json.JSONObject

/**
 * Demo-safe fallback: match filename substrings or content hash prefix against
 * bundled star_demo_fallback.json so the stage demo cannot fail.
 */
class PrecomputedStarSolver(context: Context) : StarSolver {
    private val entries: List<FallbackEntry> = loadEntries(context)

    override val kind: StarSolverKind = StarSolverKind.PRECOMPUTED

    override fun solve(context: StarSolveContext, catalog: StarCatalog): StarSolveResult {
        val fileName = context.imageFileName?.lowercase().orEmpty()
        val hash = context.contentHashHex.lowercase()
        val starCount = context.detection.stars.size

        for (entry in entries) {
            val filenameMatch = entry.filenameContains.any { fileName.contains(it) }
            val hashMatch = entry.contentHashPrefix?.let { hash.startsWith(it) } == true
            val starsOk = entry.minDetectedStars?.let { starCount >= it } ?: true
            if ((filenameMatch || hashMatch) && starsOk) {
                return StarSolveResult(
                    success = true,
                    trueNorthHeadingDeg = entry.headingDeg,
                    approximateLatDeg = entry.approximateLatDeg,
                    latUncertaintyDeg = entry.latUncertaintyDeg,
                    matchedStarCount = entry.matchedStarCount,
                    solverKind = kind.label,
                    message = entry.message
                )
            }
        }
        return StarSolveResult(
            success = false,
            trueNorthHeadingDeg = null,
            approximateLatDeg = null,
            latUncertaintyDeg = null,
            matchedStarCount = 0,
            solverKind = kind.label,
            message = "No precomputed fallback matched this image."
        )
    }

    private data class FallbackEntry(
        val filenameContains: List<String>,
        val contentHashPrefix: String?,
        val minDetectedStars: Int?,
        val headingDeg: Double,
        val approximateLatDeg: Double,
        val latUncertaintyDeg: Double,
        val matchedStarCount: Int,
        val message: String
    )

    private fun loadEntries(context: Context): List<FallbackEntry> {
        val json = context.assets.open("star_demo_fallback.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("entries")
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val names = o.optJSONArray("filename_contains")
                val nameList = buildList {
                    if (names != null) {
                        for (j in 0 until names.length()) add(names.getString(j).lowercase())
                    }
                }
                add(
                    FallbackEntry(
                        filenameContains = nameList,
                        contentHashPrefix = o.optString("content_hash_prefix", "")
                            .takeIf { it.isNotBlank() }?.lowercase(),
                        minDetectedStars = if (o.has("min_detected_stars")) o.getInt("min_detected_stars") else null,
                        headingDeg = o.getDouble("true_north_heading_deg"),
                        approximateLatDeg = o.getDouble("approximate_lat_deg"),
                        latUncertaintyDeg = o.getDouble("lat_uncertainty_deg"),
                        matchedStarCount = o.getInt("matched_star_count"),
                        message = o.optString("message", "Precomputed demo result")
                    )
                )
            }
        }
    }
}
