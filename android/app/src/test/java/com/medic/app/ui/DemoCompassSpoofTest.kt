package com.medic.app.ui

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoCompassSpoofTest {

    @Test
    fun `spoofed heading is offset from raw heading`() {
        val spoofed = DemoCompassSpoof.distortedHeading(rawHeadingDeg = 15.0, tick = 1)
        assertNotEquals(15.0, spoofed, 0.001)
        assertTrue(spoofed in 0.0..360.0)
    }

    @Test
    fun `spoofed heading stays normalized through wraparound`() {
        val spoofed = DemoCompassSpoof.distortedHeading(rawHeadingDeg = 350.0, tick = 12, offsetDeg = 30.0)
        assertTrue(spoofed in 0.0..360.0)
    }
}
