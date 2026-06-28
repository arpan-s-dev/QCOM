package com.medic.app.nav.star

import java.time.Instant

/** Context passed into plate solvers — image metadata + sensors + time. */
data class StarSolveContext(
    val detection: StarDetectionResult,
    val imageFileName: String?,
    val contentHashHex: String,
    val devicePitchDeg: Double,
    val deviceAzimuthDeg: Double,
    val utc: Instant
)

enum class StarSolverKind(val label: String) {
    PRECOMPUTED("precomputed"),
    GEOMETRY("geometry")
}

/**
 * Plate-solve / catalog match interface — swap precomputed demo fallback vs
 * real geometric solver without changing the pipeline.
 */
interface StarSolver {
    val kind: StarSolverKind
    fun solve(context: StarSolveContext, catalog: StarCatalog): StarSolveResult
}
