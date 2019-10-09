package com.example.lazyjogger.ui.main


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.lazyjogger.DetailActivity
import com.example.lazyjogger.R
import com.example.lazyjogger.database.User
import kotlinx.android.synthetic.main.history_item.view.*

class HistoryListAdapter(private val context: Context, private val runList: List<User>): RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(val cardView: View): RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false) as LinearLayout
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return runList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cardView.date.text = context.getString(R.string.dateCardView, runList[position].date)
        holder.cardView.distance.text = context.getString(R.string.metersCardView, String.format("%.0f", runList[position].distance))
        holder.cardView.testText.text = runList[position].geoPoints[1].latitude.toString()

        holder.cardView.setOnClickListener {
            val i = Intent(context, DetailActivity::class.java)
            i.putExtra("item", (position + 1).toLong())
            context.startActivity(i)
        }
    }

}

