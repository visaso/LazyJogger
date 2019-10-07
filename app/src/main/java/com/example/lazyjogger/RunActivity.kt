package com.example.lazyjogger

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_run.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

class RunActivity : AppCompatActivity() {

    private lateinit var mapRunning: MapView
    private lateinit var polyline: Polyline

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var previousLocation: GeoPoint
    private var previousTime: Long = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var distanceTraveled: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null && !this::previousLocation.isInitialized) {
                previousLocation = GeoPoint(task.result!!.latitude, task.result!!.longitude)
                previousTime = System.currentTimeMillis()
            }
        }

        locationRequest = LocationRequest().apply {
            interval = 1500
            smallestDisplacement = 0f
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val loc = GeoPoint(location.latitude, location.longitude)
                    val time = System.currentTimeMillis()
                    //Toast.makeText(applicationContext, "${location.latitude}", Toast.LENGTH_SHORT)
                    //    .show()
                    addPoint(loc, time)
                    previousLocation = loc
                    previousTime = System.currentTimeMillis()
                }
            }
        }
        startLocationUpdates()

        //currentTime.text = getString(R.string.time, "00:12:30")
        distanceText.text = getString(R.string.distance, "13.3 km")
        speedText.text = getString(R.string.speedText, "6.2")
        heartbeatText.text = getString(R.string.heartbeattext, "120 bpm")

        timer.start()
        timer.format = "00:%s"
        timer.setOnChronometerTickListener { timer ->
            timer.format = "00:%s"
        }



        mapRunning = findViewById(R.id.mapRunning)
        setupMap(mapRunning)
    }

    override fun onBackPressed() {
        stopLocationUpdates()
        super.onBackPressed()
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun setupMap(map: MapView) {
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map).apply {
            enableMyLocation()
        }
        polyline = Polyline(map)
        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            //clipToOutline = true
            setMultiTouchControls(true)
            // setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(18.0)
            //controller.setCenter(GeoPoint(60.17, 25.95))
            overlays.add(mLocationOverlay)
            overlays.add(polyline)
        }
    }

    private fun addPoint(geoPoint: GeoPoint, currentTime: Long) {
        val r = Random
        val lol = r.nextInt(10)
        val newLine = Polyline()

        when (lol) {
            1 -> newLine.color = Color.parseColor(calculateColor(0.1.toFloat()))
            2 -> newLine.color = Color.parseColor(calculateColor(0.2.toFloat()))
            3 -> newLine.color = Color.parseColor(calculateColor(0.3.toFloat()))
            4 -> newLine.color = Color.parseColor(calculateColor(0.4.toFloat()))
            5 -> newLine.color = Color.parseColor(calculateColor(0.5.toFloat()))
            6 -> newLine.color = Color.parseColor(calculateColor(0.6.toFloat()))
            7 -> newLine.color = Color.parseColor(calculateColor(0.6.toFloat()))
            8 -> newLine.color = Color.parseColor(calculateColor(0.7.toFloat()))
            9 -> newLine.color = Color.parseColor(calculateColor(0.8.toFloat()))
            else -> newLine.color = Color.parseColor(calculateColor(0.8.toFloat()))
        }
        val distanceDelta = previousLocation.distanceToAsDouble(geoPoint)
        distanceTraveled += distanceDelta


        val elapsedTime = (currentTime - previousTime) / 1000
        val velocityAsKmh = (distanceDelta/elapsedTime) * 3.6
        val formatVelocity = String.format("%.2f", velocityAsKmh)
        Log.d("ELAPSED TIME", elapsedTime.toString())
        Log.d("Current speed", formatVelocity)

        val distanceToKm = distanceTraveled / 1000
        val formatDistance = String.format("%.2f", distanceToKm)
        Log.d("Distance traveled", distanceToKm.toString())

        distanceText.text = getString(R.string.distance, formatDistance)
        speedText.text = getString(R.string.speedText, formatVelocity)

        newLine.addPoint(previousLocation)
        newLine.addPoint(geoPoint)
        mapRunning.controller.setCenter(geoPoint)
        //map.overlays.add(polyline)
        mapRunning.overlays.add(newLine)
        mapRunning.invalidate()
    }

    private fun calculateColor(strength: Float): String {
        val greenColor = Color.parseColor("#66d48f")
        val redColor = Color.parseColor("#B22222")
        val red = (greenColor.red + strength * (redColor.red - greenColor.red)).toInt()
        val green = (greenColor.green + strength * (redColor.green - greenColor.green)).toInt()
        val blue = (greenColor.blue + strength * (redColor.blue - greenColor.blue)).toInt()
        val hexRed = Integer.toHexString(red)
        val hexGreen = Integer.toHexString(green)
        val hexBlue = Integer.toHexString(blue)
        Log.d("RED", hexRed.toString())
        Log.d("GREEN", hexGreen.toString())
        Log.d("BLUE", hexBlue.toString())
        Log.d("COLOR", "#$hexRed$hexGreen$hexBlue")
        return "#$hexRed$hexGreen$hexBlue"

    }
}
