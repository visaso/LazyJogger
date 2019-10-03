package com.example.lazyjogger


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lazyjogger.bluetooth.BLEDevice
import com.example.lazyjogger.bluetooth.CustomListAdapter
import com.example.lazyjogger.bluetooth.GattHRClientCallback
import kotlinx.android.synthetic.main.activity_sensor.*

class SensorActivity : AppCompatActivity(), CustomListAdapter.ItemListener, GattHRClientCallback.HRCallback {
    override fun sendData(heartRate: Int) {
        runOnUiThread {
            //heartbeat.text = heartRate.toString()
            //val dataPoint = DataPoint(Calendar.getInstance().time, heartRate.toDouble())
        }
    }

    override fun onClick(bleDevice: BLEDevice) {
        val gattClientCallback = GattHRClientCallback(this)
        bleDevice.bluetoothDevice.connectGatt(this, false,
            gattClientCallback)
    }

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private val deviceList = ArrayList<BLEDevice>()
    private lateinit var adapter: CustomListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)
        adapter = CustomListAdapter(deviceList, this, this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        sensorFab.setOnClickListener {
            startScan()
        }
    }

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
                    val bleDevice = BLEDevice(device.name, device.address, result.rssi, result.isConnectable, result.device)
                    deviceList.add(bleDevice)
                    Log.d("DBG", "Device address: $deviceAddress (${result.isConnectable})")
                }
            }
        }
    }
}

