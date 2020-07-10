package com.scurab.android.spotifyrc.spotify

import androidx.lifecycle.LiveData
import com.scurab.android.spotifyrc.model.PlayerStateKt

interface ISpotifyClient {
    val playerState: LiveData<PlayerStateKt>
    fun connect()

    fun close()

    val connectedState: ConnectingState

    suspend fun isPaused(): Boolean

    suspend fun resume()

    suspend fun pause()

    suspend fun playNext()

    suspend fun playPrevious()
}
