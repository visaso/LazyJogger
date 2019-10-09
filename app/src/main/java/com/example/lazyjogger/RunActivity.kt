package com.example.lazyjogger

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.lazyjogger.database.User
import com.example.lazyjogger.database.UserDB
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_run.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class RunActivity : AppCompatActivity(), SensorEventListener {

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("Stepsensor accuracy", accuracy.toString())
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == sStepCounter) {
            stepCounter++
            stepCounterText.text = getString(R.string.stepcounter, stepCounter.toString())
            Log.d("Stepthingy", event?.values?.get(0).toString())
        }
    }

    private lateinit var mCompassOverlay: CompassOverlay
    private lateinit var mapRunning: MapView
    private lateinit var polyline: Polyline
    private var startingOrientation = 0.0.toFloat()

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var previousLocation: GeoPoint
    private var previousTime: Long = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var orientationProvider: InternalCompassOrientationProvider

    private var distanceTraveled: Double = 0.0

    private lateinit var sm: SensorManager
    private var sStepCounter: Sensor? = null
    private var stepCounter = 0

    private var date = Calendar.getInstance().time


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sStepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
        setupSensors()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null && !this::previousLocation.isInitialized) {
                previousLocation = GeoPoint(task.result!!.latitude, task.result!!.longitude)
                previousTime = System.currentTimeMillis()
            }
        }
        orientationProvider = InternalCompassOrientationProvider(this)

        val db = UserDB.get(this)
        val endRun = findViewById<ImageButton>(R.id.endRun)
        endRun.setOnClickListener {
            doAsync {
                val dateFormatter = SimpleDateFormat("EEE, dd.MM.yyyy, kk:mm", Locale.getDefault())
                val date = dateFormatter.format(date)
                val id = db.userDao().insert(
                    User(
                        0,
                        "Testi",
                        "Juttu",
                        distanceTraveled,
                        date,
                        stepCounter,
                        "123123"
                    )
                )

                uiThread {
                    Toast.makeText(applicationContext, "$id added", Toast.LENGTH_SHORT).show()
                }
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
                    val speed = location.speed
                    //Toast.makeText(applicationContext, "${location.latitude}", Toast.LENGTH_SHORT)
                    //    .show()
                    addPoint(loc, time, speed)
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
        stepCounterText.text = stepCounter.toString()

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

    private fun setupSensors() {
        sStepCounter?.also {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
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

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        sm.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }


    private fun setupMap(map: MapView) {

        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map).apply {
            enableMyLocation()
        }
        mCompassOverlay = CompassOverlay(this, map).apply {
            enableCompass()
            isPointerMode = true
        }
        polyline = Polyline(map)
        map.apply {
            setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            //clipToOutline = true
            setMultiTouchControls(true)
            // setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(18.0)
            //controller.setCenter(GeoPoint(60.17, 25.95))
            overlays.add(mLocationOverlay)
            overlays.add(polyline)
            overlays.add(mCompassOverlay)
            mapOrientation = startingOrientation
        }

    }

    private fun addPoint(geoPoint: GeoPoint, currentTime: Long, speed: Float) {
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
        val bearing = previousLocation.bearingTo(geoPoint)
        distanceTraveled += distanceDelta

        val formatVelocity = String.format("%.1f", speed * 3.6)
        //Log.d("ELAPSED TIME", elapsedTime.toString())
        //Log.d("Current speed", formatVelocity)

        val distanceToKm = distanceTraveled / 1000
        val formatDistance = String.format("%.2f", distanceToKm)
        //Log.d("Distance traveled", distanceToKm.toString())

        distanceText.text = getString(R.string.distance, formatDistance)
        speedText.text = getString(R.string.speedText, formatVelocity)

        if (distanceDelta > 1) {
            mapRunning.mapOrientation = bearing.toFloat()
        }
        Log.d("Map", mapRunning.mapOrientation.toString())

        Log.d("Compass", mCompassOverlay.orientation.toString())
        newLine.addPoint(previousLocation)
        newLine.addPoint(geoPoint)
        mapRunning.controller.setCenter(geoPoint)
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
        return "#$hexRed$hexGreen$hexBlue"

    }
}
