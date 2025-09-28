package com.example.wear.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import kotlin.math.max

class SpeedSensor(private val context: Context) : LocationListener {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var lastLocation: Location? = null
    private var totalDistanceMeters: Double = 0.0
    private var callback: ((speedKmh: Double, distanceMeters: Double) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun start(onUpdate: (Double, Double) -> Unit): Boolean {
        callback = onUpdate
        if (!hasPermission()) return false
        totalDistanceMeters = 0.0
        lastLocation = null
        try {
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> LocationManager.PASSIVE_PROVIDER
            }
            locationManager.requestLocationUpdates(provider, 2000L, 0f, this)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun stop() {
        try { locationManager.removeUpdates(this) } catch (_: Exception) {}
        callback = null
        lastLocation = null
        totalDistanceMeters = 0.0
    }

    private fun hasPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine
    }

    override fun onLocationChanged(location: Location) {
        if (lastLocation != null) {
            val delta = lastLocation!!.distanceTo(location).toDouble()
            val dtSec = max(1.0, (location.time - (lastLocation!!.time)).toDouble() / 1000.0)
            totalDistanceMeters += delta
            val speedMps = if (location.hasSpeed()) location.speed.toDouble() else delta / dtSec
            val speedKmh = speedMps * 3.6
            callback?.invoke(speedKmh, totalDistanceMeters)
        }
        lastLocation = location
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
    override fun onProviderEnabled(provider: String) { }
    override fun onProviderDisabled(provider: String) { }
}
