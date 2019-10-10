package com.example.lazyjogger

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lazyjogger.ColorUtils.ColorCalculator
import com.example.lazyjogger.bluetooth.BLEDevice
import com.example.lazyjogger.bluetooth.CustomListAdapter
import com.example.lazyjogger.bluetooth.GattHRClientCallback
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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RunActivity : AppCompatActivity(), SensorEventListener, GattHRClientCallback.HRCallback,
    CustomListAdapter.ItemListener {

    override fun sendData(heartRate: Int) {
        Log.d("Heartrate", currentHeartBeat.toString())
        currentHeartBeat = heartRate
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

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

    private var geoPointList: MutableList<GeoPoint> = mutableListOf()

    private var currentHeartBeat: Int = 60
    private var greenHeartBeat: Int = 60
    private var redHeartBeat: Int = 180
    private var heartBeatList: MutableList<Int> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        startScan.setOnClickListener {
            startScan()
        }

        adapter = CustomListAdapter(deviceList, this, this)
        sensorRecycler.layoutManager = LinearLayoutManager(this)
        sensorRecycler.adapter = adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

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
                val geoPoint = GeoPoint(task.result!!.latitude, task.result!!.longitude)
                previousLocation = geoPoint
                previousTime = System.currentTimeMillis()
                geoPointList.add(geoPoint)
                heartBeatList.add(0)
            }
        }
        orientationProvider = InternalCompassOrientationProvider(this)

        val db = UserDB.get(this)
        val endRun = findViewById<ImageButton>(R.id.endRun)
        endRun.setOnClickListener {
            doAsync {
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
                val date = dateFormatter.format(date)
                val geoPointList = geoPointList.toList()
                val hrList = heartBeatList.toList()
                db.userDao().insert(
                    User(
                        0,
                        "Testi",
                        "Juttu",
                        distanceTraveled,
                        date,
                        stepCounter,
                        geoPointList,
                        timer.text.toString(),
                        hrList
                    )
                )

                uiThread {
                    Toast.makeText(applicationContext, "Run finished!", Toast.LENGTH_SHORT).show()
                }
            }
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
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
                    val speed = location.speed
                    addPoint(loc, speed)
                    previousLocation = loc
                    previousTime = System.currentTimeMillis()
                    geoPointList.add(loc)
                    heartBeatList.add(currentHeartBeat)
                }
            }
        }
        startLocationUpdates()
        stepCounterText.text = stepCounter.toString()

        timer.start()
        timer.format = "00:%s"
        timer.setOnChronometerTickListener { timer ->
            timer.format = "00:%s"
        }

        Log.d("Time passed", timer.text.toString())

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
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(18.0)
            overlays.add(mLocationOverlay)
            overlays.add(polyline)
            overlays.add(mCompassOverlay)
            mapOrientation = startingOrientation
        }
    }

    private fun addPoint(geoPoint: GeoPoint, speed: Float) {
        val newLine = Polyline()
        val cc = ColorCalculator()
        Log.d(
            "Color",
            cc.calculateColor((currentHeartBeat - greenHeartBeat) / (redHeartBeat - greenHeartBeat).toFloat())
        )
        newLine.color =
            Color.parseColor(cc.calculateColor((currentHeartBeat - greenHeartBeat) / (redHeartBeat - greenHeartBeat).toFloat()))
        val distanceDelta = previousLocation.distanceToAsDouble(geoPoint)
        distanceTraveled += distanceDelta

        val formatVelocity = String.format("%.1f", speed * 3.6)

        val distanceToKm = distanceTraveled / 1000
        val formatDistance = String.format("%.2f", distanceToKm)

        distanceText.text = getString(R.string.distance, formatDistance)
        speedText.text = getString(R.string.speedText, formatVelocity)
        heartbeatText.text = getString(R.string.heartbeattext, currentHeartBeat.toString())

        newLine.addPoint(previousLocation)
        newLine.addPoint(geoPoint)
        mapRunning.controller.setCenter(geoPoint)
        mapRunning.overlays.add(newLine)
        mapRunning.invalidate()
    }

    override fun onClick(bleDevice: BLEDevice) {
        val gattClientCallback = GattHRClientCallback(this)
        bleDevice.bluetoothDevice.connectGatt(
            this, false,
            gattClientCallback
        )
        deviceList.clear()
        adapter.notifyDataSetChanged()
    }

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private val deviceList = ArrayList<BLEDevice>()
    private lateinit var adapter: CustomListAdapter

    private var mScanResults: HashMap<String, ScanResult>? = null
    private var mScanning = false
    private lateinit var mScanCallback: BtLeScanCallback
    private lateinit var mBluetoothLeScanner: BluetoothLeScanner

    companion object {
        const val SCAN_PERIOD: Long = 7000
    }

    private fun startScan() {
        Log.d("DBG", "Scan start")
        mScanResults = HashMap()
        mScanCallback = BtLeScanCallback()
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        val filter: List<ScanFilter>? = null
        val mHandler = Handler()
        mHandler.postDelayed({ stopScan() }, SCAN_PERIOD)
        mScanning = true
        mBluetoothLeScanner.startScan(filter, settings, mScanCallback)
    }

    private fun stopScan() {
        mScanning = false
        mBluetoothLeScanner.stopScan(mScanCallback)
    }

    private inner class BtLeScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
            adapter.notifyDataSetChanged()
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
            adapter.notifyDataSetChanged()
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("DBG", "BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            if (device.address != null && device.name != null) {

                val deviceAddress = device.address
                val curDevice = deviceList.find { a -> a.mac == device.address }
                if (curDevice != null) {
                    curDevice.strength = result.rssi
                    curDevice.isConnectable = result.isConnectable
                } else {
                    val bleDevice = BLEDevice(
                        device.name,
                        device.address,
                        result.rssi,
                        result.isConnectable,
                        result.device
                    )
                    deviceList.add(bleDevice)
                    Log.d("DBG", "Device address: $deviceAddress (${result.isConnectable})")
                }
            }
        }
    }
}
