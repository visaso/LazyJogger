package com.example.lazyjogger.ui.main


import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.lazyjogger.R
import com.example.lazyjogger.database.User
import com.example.lazyjogger.database.UserDB
import com.google.android.gms.location.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 */
class RunMenuFragment : Fragment(), LocationListener {

    private lateinit var map: MapView
    private lateinit var polyline: Polyline

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onLocationChanged(p0: Location?) {
        Log.d("New latitude", "latitude: ${p0?.latitude}")
        Log.d("New longitude", "longitude: ${p0?.longitude}")
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val items = ArrayList<OverlayItem>()
        items.add(OverlayItem("Title", "Description", GeoPoint(0.0, 0.0)))
        val context = context
        Configuration.getInstance().load(context,
            PreferenceManager.getDefaultSharedPreferences(context))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                Log.d("LOCATION", "latitude: ${task.result!!.latitude}")
                Log.d("LOCATION", "longitude: ${task.result!!.longitude}")
                map.controller.setCenter(GeoPoint(task.result!!.latitude, task.result!!.longitude))
                addPoint(GeoPoint(task.result!!.latitude, task.result!!.longitude))
            }
        }

        locationRequest = LocationRequest()
        locationRequest.interval = 1500
        locationRequest.smallestDisplacement = 1f
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    Toast.makeText(context, "${location.latitude}", Toast.LENGTH_SHORT).show()
                    addPoint(GeoPoint(location.latitude, location.longitude))
                }
            }
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_run_menu, container, false)
        map = v.findViewById(R.id.map)
        setupMap(map)

        val db = UserDB.get(v.context)
        val startRun = v.findViewById<Button>(R.id.startRun)
        startRun.setOnClickListener {
            doAsync {
                val id = db.userDao().insert(User(0, "Testi", "Juttu"))

                uiThread {
                    Toast.makeText(context, "$id added", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return v
    }

    private fun setupMap(map: MapView) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.clipToOutline = true
        //map.setMultiTouchControls(false)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.controller.setZoom(18.0)
        map.controller.setCenter(GeoPoint(60.17, 25.95))
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
        mLocationOverlay.enableMyLocation()
        map.overlays.add(mLocationOverlay)
        polyline = Polyline(map)
//        var startPoint = GeoPoint(60.271585, 24.826612)
//        var endPoint = GeoPoint(60.297056, 24.917141)
//        polyline.addPoint(startPoint)
//        polyline.addPoint(endPoint)
        map.overlays.add(polyline)

    }

    private fun addPoint(geoPoint: GeoPoint) {
        val r = Random
        val lol = r.nextInt(5)
        when (lol) {
            1 -> polyline.color = Color.RED
            2 -> polyline.color = Color.GREEN
            else -> polyline.color = Color.BLUE
        }
        //polyline.color = Color.RED
        polyline.addPoint(geoPoint)
        //map.overlays.add(polyline)
        map.overlays.add(polyline)
        map.invalidate()
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
