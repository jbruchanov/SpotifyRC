package com.scurab.android.spotifyrc.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.scurab.android.spotifyrc.AppPrefs
import com.scurab.android.spotifyrc.uistate.BluetoothScannerUiState
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext


class BluetoothScannerViewModel @ViewModelInject constructor(
    @ActivityContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val appPrefs: AppPrefs
) : ViewModel() {

    private val foundDevices = mutableSetOf<BluetoothDevice>()
    private val _devices = MutableLiveData<List<BluetoothDevice>>()
    val devices: LiveData<List<BluetoothDevice>> = _devices.distinctUntilChanged()

    private val _uiState = MutableLiveData<BluetoothScannerUiState>().also {
        it.value = BluetoothScannerUiState(false, appPrefs.selectedDeviceName, appPrefs.selectedDeviceMac)
    }
    val uiState: LiveData<BluetoothScannerUiState> = _uiState

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?.let { bluetoothDevice ->
                            foundDevices.add(bluetoothDevice)
                            _devices.postValue(foundDevices.toList().sortedBy { (it.name ?: "zzz") + "_" + it.address })
                        }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED ->
                    _uiState.value = BluetoothScannerUiState(true, appPrefs.selectedDeviceName, appPrefs.selectedDeviceMac)
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->
                    _uiState.value = BluetoothScannerUiState(false, appPrefs.selectedDeviceName, appPrefs.selectedDeviceMac)
            }
        }
    }

    fun onStart() {
        context.registerReceiver(broadcastReceiver, IntentFilter().also {
            it.addAction(BluetoothDevice.ACTION_FOUND)
            it.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            it.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        })
        bluetoothAdapter.startDiscovery()
    }

    fun onStop() {
        bluetoothAdapter.cancelDiscovery()
        context.unregisterReceiver(broadcastReceiver)
    }

    fun onDeviceSelected(device: BluetoothDevice) {
        appPrefs.selectedDeviceName = device.name ?: "No Name"
        appPrefs.selectedDeviceMac = device.address
        _uiState.value = BluetoothScannerUiState(bluetoothAdapter.isDiscovering, appPrefs.selectedDeviceName, appPrefs.selectedDeviceMac)
    }

    fun restartDiscovery() {
        bluetoothAdapter.startDiscovery()
    }
}