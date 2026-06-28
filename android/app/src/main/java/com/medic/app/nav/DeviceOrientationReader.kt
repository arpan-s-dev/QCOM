package com.medic.app.nav

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Thin wrapper around the rotation-vector sensor to get the device's current
 * compass bearing (degrees, 0=N clockwise) for the "point phone at the sun,
 * tap SIGHT SUN" flow. Kept separate from VoiceLoopManager/AiService since
 * this is plain Android sensor plumbing, not AI or audio.
 *
 * Activity-lifecycle-bound: call start() in onResume, stop() in onPause to
 * avoid leaking the sensor listener.
 */
class DeviceOrientationReader(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    @Volatile
    var currentBearingDeg: Double = 0.0
        private set

    /** Device pitch in degrees — used with accelerometer/rotation vector for star solve. */
    @Volatile
    var currentPitchDeg: Double = 0.0
        private set

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            // orientation[0] is azimuth in radians, -pi..pi, 0 = magnetic north
            val degrees = Math.toDegrees(orientation[0].toDouble())
            currentBearingDeg = (degrees + 360.0) % 360.0
            currentPitchDeg = Math.toDegrees(orientation[1].toDouble())
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* no-op */ }
    }

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }

    /** True if a rotation-vector sensor is actually present on this device. */
    fun isAvailable(): Boolean = rotationSensor != null
}
