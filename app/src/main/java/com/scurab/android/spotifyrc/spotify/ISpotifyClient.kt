package com.scurab.android.spotifyrc.spotify

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.scurab.android.spotifyrc.model.PlayerStateKt

interface ISpotifyClient {
    val playerState: LiveData<PlayerStateKt>
    val image: LiveData<Pair<Bitmap?, ByteArray?>>
    val connectedState: ConnectingState

    fun connect()

    fun close()

    suspend fun isPaused(): Boolean

    suspend fun resume()

    suspend fun pause()

    suspend fun playNext()

    suspend fun playPrevious()
}
