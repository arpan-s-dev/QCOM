package com.medic.app.nav

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Verified via parallel Python computation in the build sandbox (no Kotlin
 * compiler available there) -- see STATUS.md. Same three cases, all passed.
 */
class SpoofDetectorTest {

    @Test
    fun `realistic walking pace is not flagged`() {
        // ~50m in 60 seconds (~0.83 m/s, brisk walk)
        val a = SpoofDetector.GpsFix(37.3, -121.9, 0L)
        val b = SpoofDetector.GpsFix(37.30045, -121.9, 60_000L)
        assertFalse(SpoofDetector.isImplausibleJump(a, b))
    }

    @Test
    fun `teleport jump is flagged as implausible`() {
        // 10km in 1 second -- classic spoof demo jump
        val a = SpoofDetector.GpsFix(37.3, -121.9, 0L)
        val b = SpoofDetector.GpsFix(37.39, -121.9, 1_000L)
        assertTrue(SpoofDetector.isImplausibleJump(a, b))
    }

    @Test
    fun `borderline case just under threshold is not flagged`() {
        // 700m in 10s = exactly 70 m/s threshold; strictly-greater-than
        // comparison means exactly-at-threshold should NOT be flagged.
        val dLatFor700m = 700.0 / 111320.0
        val a = SpoofDetector.GpsFix(37.3, -121.9, 0L)
        val b = SpoofDetector.GpsFix(37.3 + dLatFor700m, -121.9, 10_000L)
        assertFalse(SpoofDetector.isImplausibleJump(a, b))
    }
}
