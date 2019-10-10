package com.example.lazyjogger.ui.main


import android.content.Context
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.lazyjogger.R
import com.example.lazyjogger.database.User
import com.example.lazyjogger.database.UserDB
import kotlinx.android.synthetic.main.history_item.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Polyline

class HistoryListAdapter(private val context: Context, private val runList: List<User>) :
    RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(val cardView: View) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(
            R.layout.history_item,
            parent,
            false
        ) as LinearLayout
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return runList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = runList[position]
        val db = UserDB.get(context)
        val map = holder.cardView.map
        var open = false

        doAsync {
            val run = db.userDao().getItem((position + 1).toLong())
            val geoPoints = run.geoPoints
            val polyline = Polyline(map).apply {
                setPoints(geoPoints)
            }
            map.apply {
                uiThread {
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

            }
            //val i = Intent(context, DetailActivity::class.java)
            //i.putExtra("item", (position + 1).toLong())
            //context.startActivity(i)
        }

        holder.cardView.date.text = context.getString(R.string.dateCardView, item.date)
        holder.cardView.distance.text =
            context.getString(R.string.metersCardView, String.format("%.0f", item.distance))
        //holder.cardView.testText.text = item.geoPoints[1].latitude.toString()
        holder.cardView.timeText.text = context.getString(R.string.timecardview, item.timeSpent)

        holder.cardView.setOnClickListener {
            if (open) {
                open = false
                map.visibility = View.GONE
            } else {
                open = true
                TransitionManager.beginDelayedTransition(holder.cardView.cardView)
                map.visibility = View.VISIBLE

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

