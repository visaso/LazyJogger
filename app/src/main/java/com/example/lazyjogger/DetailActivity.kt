package com.example.lazyjogger

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.lazyjogger.database.UserDB
import kotlinx.android.synthetic.main.activity_detail.*
import org.jetbrains.anko.activityUiThread
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Polyline
/*
class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val db = UserDB.get(this)
        val item = intent.getLongExtra("item", 0)
        Log.d("item", item.toString())

        doAsync {
            val run = db.userDao().getItem(item)
            val geoPoints = run.geoPoints
            val polyline = Polyline(map).apply {
                setPoints(geoPoints)
            }
            map.apply {
                setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
                //clipToOutline = true
                setMultiTouchControls(true)
                // setMultiTouchControls(false)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(16.0)
                //controller.setCenter(GeoPoint(60.17, 25.95))
                controller.setCenter(checkCameraZoom(geoPoints))
                overlays.add(polyline)
            }

            uiThread {
                distance.text = run.distance.toString()
            }
        }
    }

    private fun checkCameraZoom(points: List<GeoPoint>): GeoPoint {
        var furthestDistance = 0.0
        val startingPoint = points[0]
        var furthestPoint = points[0]
        for (p in points) {
            if (p.distanceToAsDouble(startingPoint) > furthestDistance) {
                furthestDistance = p.distanceToAsDouble(startingPoint)
                furthestPoint = p
            }
        }
        return GeoPoint.fromCenterBetween(startingPoint, furthestPoint)
    }
}
*/