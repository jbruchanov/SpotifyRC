package com.scurab.android.spotifyrc.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.scurab.android.spotify.api.model.Item
import com.scurab.android.spotifyrc.AppPrefs
import com.scurab.android.spotifyrc.lifecycle.LiveQueue
import com.scurab.android.spotifyrc.lifecycle.MutableLiveQueue.Companion.navigationQueue
import com.scurab.android.spotifyrc.uistate.HomeUiState
import com.scurab.android.spotifyrc.util.DeviceInfoProvider
import com.spotify.sdk.android.auth.AuthorizationResponse

class HomeViewModel @ViewModelInject constructor(
    private val deviceInfoProvider: DeviceInfoProvider,
    private val prefs: AppPrefs
) : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>().also {
        it.value = HomeUiState(prefs.localAccessToken != null)
    }
    val uiState: LiveData<HomeUiState> = _uiState.distinctUntilChanged()

    private val _navigation = navigationQueue<HomeNavigationToken>()
    val navigation: LiveQueue<HomeNavigationToken> = _navigation

    fun onServerClicked() {
        val token = when {
            !deviceInfoProvider.isSpotifyAppInstalled() -> HomeNavigationToken.ErrorNoSpotifyApp
            prefs.localAccessToken == null -> HomeNavigationToken.ErrorLoginFirst
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

    fun onSpotifyClicked() {
        val isLoggedIn = prefs.localAccessToken != null
        val token = if (isLoggedIn) {
            prefs.localAccessToken = null
            HomeNavigationToken.SpotifyLoggedOut
        } else HomeNavigationToken.SpotifyLogin
        _navigation.emit(token)
        _uiState.value = HomeUiState(prefs.localAccessToken != null)
    }

    fun onSpotifyResult(response: AuthorizationResponse) {
        if (response.type == AuthorizationResponse.Type.TOKEN && response.accessToken != null) {
            prefs.localAccessToken = response.accessToken
            _navigation.emit(HomeNavigationToken.SpotifyOk)
        } else {
            _navigation.emit(HomeNavigationToken.SpotifyError)
        }
        _uiState.value = HomeUiState(prefs.localAccessToken != null)
    }

    fun onItemClicked(it: Item) {

    }
}

enum class HomeNavigationToken {
    StartServer,
    OpenClient,
    OpenBluetoothScanner,
    SpotifyLogin,
    SpotifyLoggedOut,
    SpotifyOk,
    SpotifyError,
    ErrorNoSpotifyApp,
    ErrorNoServerSelected,
    ErrorBluetoothOff,
    ErrorConnectToSelectedDeviceFirst,
    ErrorNeedLocationPermission,
    ErrorLoginFirst
}