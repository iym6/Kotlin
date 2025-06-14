package ru.ilyamorozov.lab22

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.*
import kotlin.random.Random

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var statusText: TextView
    private lateinit var distanceText: TextView
    private var targetLatitude: Double = 0.0
    private var targetLongitude: Double = 0.0
    private var isTargetSet: Boolean = false
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        distanceText = findViewById(R.id.distance_text)
        val newPointButton: Button = findViewById(R.id.new_point_button)
        val settingsButton: Button = findViewById(R.id.settings_button)

        newPointButton.setOnClickListener { generateNewTarget() }
        settingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                1f,
                this
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                3000,
                10f,
                this
            )
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun generateNewTarget() {
        lastKnownLocation?.let { location ->
            val radius = 0.0016
            targetLatitude = location.latitude + (Random.nextDouble() * 2 - 1) * radius
            targetLongitude = location.longitude + (Random.nextDouble() * 2 - 1) * radius
            isTargetSet = true
            statusText.text = getString(R.string.hint_text)
            statusText.setTextColor(getColor(android.R.color.holo_blue_light))
            updateDistanceDisplay(location)
        } ?: run {
            statusText.text = getString(R.string.waiting)
        }
    }

    override fun onLocationChanged(location: Location) {
        lastKnownLocation = location

        if (!isTargetSet) {
            generateNewTarget()
            return
        }

        updateDistanceDisplay(location)

        val distance = calculateDistance(
            location.latitude,
            location.longitude,
            targetLatitude,
            targetLongitude
        )

        if (distance <= 100) {
            statusText.text = getString(R.string.success_text)
            statusText.setTextColor(getColor(android.R.color.holo_green_light))
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun updateDistanceDisplay(location: Location) {
        if (isTargetSet) {
            val distance = calculateDistance(
                location.latitude,
                location.longitude,
                targetLatitude,
                targetLongitude
            )
            distanceText.text = getString(R.string.distance_text, distance.toInt())
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            statusText.text = getString(R.string.location_permission_denied)
        }
    }
}