package com.scurab.android.spotifyrc.usecase

import android.bluetooth.BluetoothAdapter
import javax.inject.Inject

class ScanBluetoothDevicesUseCase @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter
) {
    fun findDevices() {

    }
}