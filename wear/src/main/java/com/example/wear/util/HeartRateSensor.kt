package com.example.wear.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class HeartRateSensor(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    private var callback: ((Int) -> Unit)? = null
    private var isStarted = false

    fun start(onHeartRate: (Int) -> Unit): Boolean {
        callback = onHeartRate
        if (heartSensor == null) return false
        if (isStarted) return true
        isStarted = sensorManager.registerListener(
            this,
            heartSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        return isStarted
    }

    fun stop() {
        if (isStarted) {
            sensorManager.unregisterListener(this)
            isStarted = false
        }
        callback = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            val bpm = event.values.firstOrNull()?.toInt() ?: return
            callback?.invoke(bpm)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}
