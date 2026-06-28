package com.medic.app.nav.star

/**
 * Detected star centroid in image pixel space (origin top-left).
 * [relativeBrightness] is normalized 0..1 within the frame.
 */
data class DetectedStar(
    val x: Float,
    val y: Float,
    val relativeBrightness: Float
)

/** Result of classical CV star detection — no ML training. */
data class StarDetectionResult(
    val stars: List<DetectedStar>,
    val imageWidth: Int,
    val imageHeight: Int,
    val detectorName: String
)

/**
 * Star detection step — structured so an NPU-backed detector could replace
 * the default classical-CV implementation later.
 */
interface StarDetector {
    val name: String
    fun detect(grayscalePixels: IntArray, width: Int, height: Int): StarDetectionResult
}
