package com.scurab.android.spotifyrc.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.spotify.android.appremote.internal.SpotifyLocator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {

    val isBlueToothEnabled: Boolean get() = bluetoothAdapter.isEnabled

    val hasLocationPermission: Boolean
        get() = ContextCompat
            .checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun isBluetoothDeviceBound(mac: String): Boolean {
        return bluetoothAdapter.bondedDevices.firstOrNull { it.address == mac } != null
    }

    fun isSpotifyAppInstalled(): Boolean {
        return SpotifyLocator().isSpotifyInstalled(context)
    }
}