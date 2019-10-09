package com.example.lazyjogger.ui.main


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.lazyjogger.R
import com.example.lazyjogger.database.User
import kotlinx.android.synthetic.main.history_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryListAdapter(private val context: Context, private val runList: List<User>): RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    class ViewHolder(val textView: View): RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false) as LinearLayout
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return runList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.date.text = context.getString(R.string.dateCardView, runList[position].date)
        holder.textView.distance.text = context.getString(R.string.metersCardView, String.format("%.0f", runList[position].distance))

    }

}

