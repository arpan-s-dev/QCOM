package com.medic.app.nav

import org.junit.Test
import org.junit.Assert.assertEquals

class PositionStateMachineTest {

    @Test
    fun `gps available and not spoofed stays trusted`() {
        val state = PositionState(source = PositionSource.GPS_TRUSTED)
        val next = PositionStateMachine.transition(state, gpsAvailable = true, gpsSpoofed = false)
        assertEquals(PositionSource.GPS_TRUSTED, next.source)
        assertEquals(false, next.spoofDetected)
    }

    @Test
    fun `gps spoofed falls back to dead reckoning and flags spoof`() {
        val state = PositionState(source = PositionSource.GPS_TRUSTED)
        val next = PositionStateMachine.transition(state, gpsAvailable = true, gpsSpoofed = true)
        assertEquals(PositionSource.DEAD_RECKONING, next.source)
        assertEquals(true, next.spoofDetected)
    }

    @Test
    fun `gps unavailable without spoof falls back to dead reckoning without spoof flag`() {
        val state = PositionState(source = PositionSource.GPS_TRUSTED)
        val next = PositionStateMachine.transition(state, gpsAvailable = false, gpsSpoofed = false)
        assertEquals(PositionSource.DEAD_RECKONING, next.source)
        assertEquals(false, next.spoofDetected)
    }

    @Test
    fun `dead reckoning falls back to solar fix after threshold`() {
        val state = PositionState(source = PositionSource.DEAD_RECKONING, drElapsedSeconds = 650)
        val next = PositionStateMachine.transition(state, gpsAvailable = false, gpsSpoofed = false)
        assertEquals(PositionSource.SOLAR_FIX, next.source)
    }

    @Test
    fun `dead reckoning stays dead reckoning before threshold`() {
        val state = PositionState(source = PositionSource.DEAD_RECKONING, drElapsedSeconds = 100)
        val next = PositionStateMachine.transition(state, gpsAvailable = false, gpsSpoofed = false)
        assertEquals(PositionSource.DEAD_RECKONING, next.source)
    }

    @Test
    fun `gps returning after dead reckoning restores trusted state`() {
        val state = PositionState(source = PositionSource.DEAD_RECKONING, drElapsedSeconds = 300)
        val next = PositionStateMachine.transition(state, gpsAvailable = true, gpsSpoofed = false)
        assertEquals(PositionSource.GPS_TRUSTED, next.source)
        assertEquals(0L, next.drElapsedSeconds)
    }
}
