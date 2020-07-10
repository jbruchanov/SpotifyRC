package com.scurab.android.spotifyrc.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.scurab.android.spotifyrc.AppPrefs
import com.scurab.android.spotifyrc.lifecycle.LiveQueue
import com.scurab.android.spotifyrc.lifecycle.MutableLiveQueue.Companion.navigationQueue
import com.scurab.android.spotifyrc.util.DeviceInfoProvider

class HomeViewModel @ViewModelInject constructor(
    private val deviceInfoProvider: DeviceInfoProvider,
    private val prefs: AppPrefs
) : ViewModel() {

    private val _navigation = navigationQueue<HomeNavigationToken>()
    val navigation: LiveQueue<HomeNavigationToken> = _navigation

    fun onServerClicked() {
        val token = when {
            !deviceInfoProvider.isBlueToothEnabled -> HomeNavigationToken.ErrorBluetoothOff
            else -> HomeNavigationToken.StartServer
        }
        _navigation.emit(token)
    }

    fun onClientClicked() {
        val token = when {
            !deviceInfoProvider.isBlueToothEnabled -> HomeNavigationToken.ErrorBluetoothOff
            prefs.selectedDeviceName == null -> HomeNavigationToken.ErrorNoServerSelected
            !deviceInfoProvider.isBluetoothDeviceBound(prefs.selectedDeviceMac!!) -> HomeNavigationToken.ErrorConnectToSelectedDeviceFirst
            else -> HomeNavigationToken.OpenClient
        }
        _navigation.emit(token)
    }

    fun onScannerClicked() {
        val token = when {
            !deviceInfoProvider.isBlueToothEnabled -> HomeNavigationToken.ErrorBluetoothOff
            !deviceInfoProvider.hasLocationPermission -> HomeNavigationToken.ErrorNeedLocationPermission
            else -> HomeNavigationToken.OpenBluetoothScanner
        }
        _navigation.emit(token)
    }
}

enum class HomeNavigationToken {
    StartServer,
    OpenClient,
    OpenBluetoothScanner,
    ErrorNoServerSelected,
    ErrorBluetoothOff,
    ErrorConnectToSelectedDeviceFirst,
    ErrorNeedLocationPermission
}