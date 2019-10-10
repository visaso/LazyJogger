package com.example.lazyjogger.ui.main


import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.lazyjogger.R
import com.example.lazyjogger.RunActivity
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Show current location and preview in menu
 */
class RunMenuFragment : Fragment(), LocationListener {

    private lateinit var map: MapView

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var previousLocation: GeoPoint

    override fun onLocationChanged(p0: Location?) {
        Log.d("New latitude", "latitude: ${p0?.latitude}")
        Log.d("New longitude", "longitude: ${p0?.longitude}")
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null && !this::previousLocation.isInitialized) {
                previousLocation = GeoPoint(task.result!!.latitude, task.result!!.longitude)
            }
        }


        locationRequest = LocationRequest().apply {
            interval = 2500
            smallestDisplacement = 10f
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val loc = GeoPoint(location.latitude, location.longitude)
                    map.controller.setCenter(GeoPoint(location.latitude, location.longitude))
                    previousLocation = loc
                }
            }
        }
        startLocationUpdates()
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_run_menu, container, false)
        map = v.findViewById(R.id.map)
        setupMap(map)

        val startRun = v.findViewById<Button>(R.id.startRun)

        startRun.setOnClickListener {
            val intent = Intent(context, RunActivity::class.java)
            stopLocationUpdates()
            startActivity(intent)
        }

        return v
    }

    private fun setupMap(map: MapView) {
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map).apply {
            enableMyLocation()
        }
        map.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            clipToOutline = true
            setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(18.0)
            overlays.add(mLocationOverlay)
        }
    }


    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): RunMenuFragment {
            return RunMenuFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }


}
