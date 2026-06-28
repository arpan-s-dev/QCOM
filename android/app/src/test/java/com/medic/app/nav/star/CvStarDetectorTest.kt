package com.medic.app.nav.star

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CvStarDetectorTest {

    private val detector = CvStarDetector(brightnessPercentile = 950)

    @Test
    fun `detects bright point centroids on synthetic frame`() {
        val w = 64
        val h = 64
        val pixels = IntArray(w * h) { 20 }
        fun dot(x: Int, y: Int, value: Int = 250) {
            for (dy in -1..1) {
                for (dx in -1..1) {
                    val px = x + dx
                    val py = y + dy
                    if (px in 0 until w && py in 0 until h) {
                        pixels[py * w + px] = value
                    }
                }
            }
        }
        dot(10, 12)
        dot(40, 20)
        dot(50, 50)

        val result = detector.detect(pixels, w, h)
        assertTrue(result.stars.size >= 2)
        assertEquals(w, result.imageWidth)
        assertEquals(h, result.imageHeight)
    }

    @Test
    fun `filters noise when frame is uniformly dark`() {
        val w = 32
        val h = 32
        val pixels = IntArray(w * h) { 15 }
        val result = detector.detect(pixels, w, h)
        assertTrue(result.stars.isEmpty())
    }
}
