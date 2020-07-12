package com.scurab.android.spotifyrc.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.scurab.android.spotifyrc.lifecycle.MutableLiveQueue.Companion.navigationQueue
import com.scurab.android.spotifyrc.model.STrack
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
    val image = spotify.image
    val navigation = navigationQueue<ClientNavigationToken>()

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
                delay(BluetoothServer.TIMEOUT)
            }
        }
    }

    fun onTrackClick(track: STrack) {
        viewModelScope.launch {
            spotify.play(track.uri)
        }
    }

    fun search() {
        navigation.emit(ClientNavigationToken.Search)
    }
}

enum class ClientNavigationToken {
    Search
}