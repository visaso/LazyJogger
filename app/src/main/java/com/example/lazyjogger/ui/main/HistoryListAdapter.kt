package com.example.lazyjogger.ui.main


import android.content.Context
import android.graphics.Color
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.lazyjogger.ColorUtils.ColorCalculator
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

/**
 * Adapter that fetches items from the database and shows them on the UI
 * Also handles the calculation of values from database to a more readable format.
 */

class HistoryListAdapter(private val context: Context, private val runList: List<User>) :
    RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(val cardView: View) : RecyclerView.ViewHolder(cardView)

    companion object {
        const val greenHeartBeat = 60
        const val redHeartBeat = 180
    }

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

            map.apply {
                uiThread {
                    setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(16.0)
                    controller.setCenter(checkCameraZoom(geoPoints))
                }
            }
            val cc = ColorCalculator()
            for (i in geoPoints.indices) {
                if (i == 0) {
                    Log.d("Skip this one", "Skip")
                } else {
                    val newLine = Polyline()
                    newLine.color =
                        Color.parseColor(cc.calculateColor((item.hrList[i] - greenHeartBeat) / (redHeartBeat - greenHeartBeat).toFloat()))
                    newLine.addPoint(item.geoPoints[i - 1])
                    newLine.addPoint(item.geoPoints[i])
                    map.overlays.add(newLine)
                    map.invalidate()
                }
            }
        }

        holder.cardView.date.text = context.getString(R.string.dateCardView, item.date)
        holder.cardView.distance.text =
            context.getString(R.string.metersCardView, String.format("%.0f", item.distance))
        holder.cardView.timeText.text = context.getString(R.string.timecardview, item.timeSpent)

        holder.cardView.avgHeartrate.text =
            context.getString(R.string.average_bpm, calcAverage(item.hrList).toString())
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

    private fun calcAverage(list: List<Int>): Int {
        var hrSum = 0
        for (i in list) {
            hrSum += i
        }
        return hrSum / list.size
    }
}

