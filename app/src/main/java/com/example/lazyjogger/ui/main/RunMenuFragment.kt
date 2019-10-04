package com.example.lazyjogger.ui.main


import android.location.Location
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.lazyjogger.R
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.android.synthetic.main.fragment_run_menu.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus

/**
 * A simple [Fragment] subclass.
 */
class RunMenuFragment : Fragment(), LocationListener {

    private lateinit var map: MapView

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



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_run_menu, container, false)
        map = v.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.clipToOutline = true
        map.setMultiTouchControls(false)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.controller.setZoom(18.0)
        map.controller.setCenter(GeoPoint(60.17, 25.95))
        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)
        mLocationOverlay.enableMyLocation()
        map.overlays.add(mLocationOverlay)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationClient.lastLocation.addOnCompleteListener {
                task ->
            if (task.isSuccessful && task.result != null) {
                Log.d("LOCATION", "latitude: ${task.result!!.latitude}")
                Log.d("LOCATION", "longitude: ${task.result!!.longitude}")
                map.controller.setCenter(GeoPoint(task.result!!.latitude, task.result!!.longitude))
            }
        }
        return v
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
