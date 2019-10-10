package com.example.lazyjogger.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lazyjogger.R
import kotlinx.android.synthetic.main.list_item.view.*

data class BLEDevice(val name: String?, val mac: String, var strength: Int, var isConnectable: Boolean, val bluetoothDevice: BluetoothDevice)

/**
 * Adapter to show found BLE-devices on the UI
 */

class CustomListAdapter(private val items : ArrayList<BLEDevice>, val context: Context, private val itemListener: ItemListener) : RecyclerView.Adapter<CustomListAdapter.ViewHolder>() {

    interface ItemListener {
        fun onClick(bleDevice: BLEDevice)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].name != null) {
            holder.listItem.name.text = items[position].name
        } else {
            holder.listItem.name.text = context.getString(R.string.unknown)
        }
        holder.listItem.mac.text = items[position].mac
        holder.listItem.str.text = items[position].strength.toString()
        holder.listItem.isEnabled = items[position].isConnectable
        if (holder.listItem.isEnabled) {
            holder.listItem.name.setTextColor((Color.BLUE))
        } else {
            holder.listItem.name.setTextColor((Color.RED))
        }

        holder.listItem.setOnClickListener {
            itemListener.onClick(items[position])
        }
    }

    class ViewHolder(textView: View) : RecyclerView.ViewHolder(textView) {
        val listItem = textView
    }
}