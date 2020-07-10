package com.scurab.android.spotifyrc.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.scurab.android.spotifyrc.service.BluetoothServer
import com.scurab.android.spotifyrc.spotify.ConnectingState
import com.scurab.android.spotifyrc.spotify.SpotifyBtClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClientViewModel @ViewModelInject constructor(
//    private val spotify: SpotifyLocalClient
    private val spotify: SpotifyBtClient
) : ViewModel() {

    private var isReconnecting = false
    val connectingState = spotify.state.map {
        it.takeIf { !(it == ConnectingState.Disconnected && isReconnecting) } ?: ConnectingState.Connecting
    }.distinctUntilChanged()
    val playerState = spotify.playerState
    val imageUrl = spotify.playerState.map {
        it.trackImageUri
    }.distinctUntilChanged()

    val image: LiveData<Bitmap> = MediatorLiveData<Bitmap>().let { mediator ->
        mediator.addSource(imageUrl) {
//            if (it != null) {
//                viewModelScope.launch {
//                    mediator.value = spotify.loadImage(ImageUri(it))
//                }
//            } else {
//                mediator.value = null
//            }
        }
        mediator
    }

    fun playPause() {
        viewModelScope.launch {
            if (spotify.isPaused()) {
                spotify.resume()
            } else {
                spotify.pause()
            }
        }
    }

    fun playNext() {
        viewModelScope.launch {
            spotify.playNext()
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            spotify.playPrevious()
        }
    }

    fun connect() {
        spotify.connect()
    }

    fun disconnect() {
        spotify.close()
    }

    fun reconnect() {
        viewModelScope.launch {
            isReconnecting = true
            while (true) {
                Log.d("ClientViewModel", "ReconnectLoop:${spotify.connectedState}")
                delay(BluetoothServer.TIMEOUT)
                when (spotify.connectedState) {
                    ConnectingState.Disconnected -> spotify.connect()
                    ConnectingState.Connecting -> {
                        //nothing
                    }
                    ConnectingState.Connected -> {
                        Log.d("ClientViewModel", "ReconnectLoop:${spotify.connectedState}")
                        isReconnecting = false
                        return@launch
                    }
                }
            }
        }
    }
}