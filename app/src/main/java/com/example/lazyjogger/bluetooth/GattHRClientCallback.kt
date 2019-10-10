package com.example.lazyjogger.bluetooth

import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import android.util.Log
import java.util.*
import android.bluetooth.BluetoothGattDescriptor
import kotlin.experimental.and

@Suppress("PrivatePropertyName")
class GattHRClientCallback(private val hrCallback: HRCallback) : BluetoothGattCallback() {

    interface HRCallback {
        fun sendData(heartRate: Int)
    }

    private val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
    private val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902)
    /* Generates 128-bit UUID from the Protocol Identifier (16-bit number)
    * and the BASE_UUID (00000000-0000-1000-8000-00805F9B34FB)
    */
    private fun convertFromInteger(i: Int): UUID {
        val msb = 0x0000000000001000L
        val lsb = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(msb or (value shl 32), lsb)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (status == BluetoothGatt.GATT_FAILURE) {
            Log.d("DBG", "GATT connection failure")
            return
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.d("DBG", "GATT connection success")
            return
        }
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.d("DBG", "Connected GATT service")
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.d("DBG", "Disconnected GATT service")
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status != BluetoothGatt.GATT_SUCCESS) {
            return
        }
        Log.d("DBG", "onServicesDiscovered()")
        for (gattService in gatt.services) {
            Log.d("DBG", "Service ${gattService.uuid}")
            if (gattService.uuid == HEART_RATE_SERVICE_UUID) {
                Log.d("DBG", "BINGO!!!")
                for (gattCharacteristic in gattService.characteristics)
                    Log.d("DBG", "Characteristic ${gattCharacteristic.uuid}")
                val characteristic = gatt.getService(HEART_RATE_SERVICE_UUID)
                    .getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                gatt.setCharacteristicNotification(characteristic, true)

                val bluetoothGattDescriptor =
                    characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                bluetoothGattDescriptor.value = ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(bluetoothGattDescriptor)
            }
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        //Log.d("DBG", "onDescriptorWrite")
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val char = characteristic.value
        if (char != null) {
            //Log.d("DBG", characteristic.value.size.toString())
            char.forEach {
                Log.d("Test", it.toInt().toString())
            }
            if ((char[0] and 1).toInt() != 0) {
                hrCallback.sendData(characteristic.getIntValue(FORMAT_UINT16, 1))
            } else {
                hrCallback.sendData(characteristic.getIntValue(FORMAT_UINT8, 1))
            }
        }
    }
}
