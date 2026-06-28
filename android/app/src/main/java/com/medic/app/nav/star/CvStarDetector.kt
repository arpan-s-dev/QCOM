package com.medic.app.nav.star

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Classical computer-vision star detection: threshold bright pixels, cluster
 * into blobs, emit centroids. Suitable for offline plate-solving input.
 */
class CvStarDetector(
    private val brightnessPercentile: Int = 985, // top ~1.5% of pixels
    private val minBlobPixels: Int = 4,
    private val maxBlobPixels: Int = 400,
    private val mergeRadiusPx: Float = 10f
) : StarDetector {

    override val name: String = "cv-threshold-centroid"

    override fun detect(grayscalePixels: IntArray, width: Int, height: Int): StarDetectionResult {
        require(grayscalePixels.size == width * height) { "Pixel buffer size mismatch" }

        val threshold = computeThreshold(grayscalePixels)
        val visited = BooleanArray(grayscalePixels.size)
        val blobs = mutableListOf<Blob>()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                if (visited[idx] || grayscalePixels[idx] < threshold) continue
                val blob = floodFill(grayscalePixels, visited, width, height, x, y, threshold)
                if (blob.count in minBlobPixels..maxBlobPixels) {
                    blobs.add(blob)
                }
            }
        }

        val merged = mergeNearby(blobs)
        val maxBright = merged.maxOfOrNull { it.brightnessSum }?.toFloat() ?: 1f
        val stars = merged.map { b ->
            DetectedStar(
                x = b.cx,
                y = b.cy,
                relativeBrightness = (b.brightnessSum / maxBright).coerceIn(0f, 1f)
            )
        }.sortedByDescending { it.relativeBrightness }

        return StarDetectionResult(stars, width, height, name)
    }

    fun detect(bitmap: Bitmap): StarDetectionResult {
        val scaled = scaleDown(bitmap, 960)
        val w = scaled.width
        val h = scaled.height
        val gray = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val c = scaled.getPixel(x, y)
                gray[y * w + x] = (
                    Color.red(c) * 0.299 +
                        Color.green(c) * 0.587 +
                        Color.blue(c) * 0.114
                ).toInt()
            }
        }
        if (scaled !== bitmap) scaled.recycle()
        return detect(gray, w, h)
    }

    private data class Blob(val cx: Float, val cy: Float, val count: Int, val brightnessSum: Long)

    private fun computeThreshold(pixels: IntArray): Int {
        val hist = IntArray(256)
        pixels.forEach { hist[it.coerceIn(0, 255)]++ }
        val target = pixels.size * brightnessPercentile / 1000
        var cumulative = 0
        for (i in 255 downTo 0) {
            cumulative += hist[i]
            if (cumulative >= target) return max(40, i)
        }
        return 180
    }

    private fun floodFill(
        pixels: IntArray,
        visited: BooleanArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        threshold: Int
    ): Blob {
        var sumX = 0.0
        var sumY = 0.0
        var count = 0
        var brightnessSum = 0L
        val stack = ArrayDeque<Pair<Int, Int>>()
        stack.add(startX to startY)
        while (stack.isNotEmpty()) {
            val (x, y) = stack.removeLast()
            if (x !in 0 until width || y !in 0 until height) continue
            val idx = y * width + x
            if (visited[idx] || pixels[idx] < threshold) continue
            visited[idx] = true
            count++
            sumX += x
            sumY += y
            brightnessSum += pixels[idx]
            stack.add(x + 1 to y)
            stack.add(x - 1 to y)
            stack.add(x to y + 1)
            stack.add(x to y - 1)
        }
        return Blob(
            cx = (sumX / count).toFloat(),
            cy = (sumY / count).toFloat(),
            count = count,
            brightnessSum = brightnessSum
        )
    }

    private fun mergeNearby(blobs: List<Blob>): List<Blob> {
        if (blobs.isEmpty()) return blobs
        val merged = mutableListOf<Blob>()
        val used = BooleanArray(blobs.size)
        for (i in blobs.indices) {
            if (used[i]) continue
            var cx = blobs[i].cx
            var cy = blobs[i].cy
            var count = blobs[i].count
            var bright = blobs[i].brightnessSum
            used[i] = true
            for (j in i + 1 until blobs.size) {
                if (used[j]) continue
                val dx = blobs[j].cx - cx
                val dy = blobs[j].cy - cy
                if (sqrt(dx * dx + dy * dy) <= mergeRadiusPx) {
                    val total = count + blobs[j].count
                    cx = (cx * count + blobs[j].cx * blobs[j].count) / total
                    cy = (cy * count + blobs[j].cy * blobs[j].count) / total
                    bright += blobs[j].brightnessSum
                    count = total
                    used[j] = true
                }
            }
            merged.add(Blob(cx, cy, count, bright))
        }
        return merged
    }

    private fun scaleDown(source: Bitmap, maxDim: Int): Bitmap {
        val maxSide = max(source.width, source.height)
        if (maxSide <= maxDim) return source
        val scale = maxDim.toFloat() / maxSide
        val nw = max(1, (source.width * scale).toInt())
        val nh = max(1, (source.height * scale).toInt())
        return Bitmap.createScaledBitmap(source, nw, nh, true)
    }
}
