package com.scurab.android.spotifyrc.spotify

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.scurab.android.spotifyrc.model.PlayerStateKt
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.ImageUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException


class SpotifyLocalClient(
    private val app: Context,
    private val clientId: String
) : ISpotifyClient {

    private var spotify: SpotifyAppRemote? = null
    private val _playerState = MutableLiveData<PlayerStateKt>()
    override val playerState: LiveData<PlayerStateKt> = _playerState
    private val _state = MutableLiveData<ConnectingState>()
    val state: LiveData<ConnectingState> = _state
    override val connectedState: ConnectingState = _state.value ?: ConnectingState.Disconnected

    override fun connect() {
        spotify?.let { SpotifyAppRemote.disconnect(it) }
        _state.value = ConnectingState.Connecting
        try {
            Log.d(TAG, "connect()")
            SpotifyAppRemote.connect(
                app,
                ConnectionParams.Builder(clientId)
                    .setRedirectUri("scurab://callback")
                    .showAuthView(false)
                    .build(),
                object : ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        Log.d(TAG, "Connected")
                        _state.value = ConnectingState.Connecting
                        spotify = spotifyAppRemote.also {
                            it.playerApi.subscribeToPlayerState().setEventCallback { state ->
                                Log.d(TAG, "Update state:${state}")
                                if (state != null) {
                                    _playerState.value = PlayerStateKt(state)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable) {
                        Log.d(TAG, "Connection error:${error}")
                        spotify = null
                        _state.value = ConnectingState.Disconnected
                    }
                })
        } catch (e: Throwable) {
            _state.value = ConnectingState.Disconnected
        }
    }

    override fun close() {
        Log.d(TAG, "close()")
        SpotifyAppRemote.disconnect(spotify)
        spotify = null
        _state.value = ConnectingState.Disconnected
    }

    override suspend fun isPaused(): Boolean {
        return spotify?.playerApi?.playerState.fetch { isPaused }
    }

    override suspend fun resume() {
        return spotify?.playerApi.call { resume() }
    }

    override suspend fun pause() {
        return spotify?.playerApi.call { pause() }
    }

    suspend fun loadImage(uri: ImageUri): Bitmap {
        return spotify?.imagesApi?.getImage(uri).fetch { this }
    }

    override suspend fun playNext() {
        spotify?.playerApi.call { skipNext() }
    }

    override suspend fun playPrevious() {
        spotify?.playerApi.call { skipPrevious() }
    }

    private suspend fun <R, T> R?.call(block: R.() -> T): T {
        return suspendCancellableCoroutine { continuation ->
            this?.also {
                val result = block.invoke(it)
                continuation.resumeWith(Result.success(result))
            } ?: continuation.resumeWithException(NullPointerException("playerApi"))
        }
    }

    private suspend fun <R, T> CallResult<R>?.fetch(block: R.() -> T): T {
        return suspendCancellableCoroutine { continuation ->
            this?.also {
                it.setResultCallback { target ->
                    val result = block.invoke(target)
                    continuation.resumeWith(Result.success(result))
                }
            } ?: continuation.resumeWithException(NullPointerException())
        }
    }

    companion object {
        const val TAG = "SpotifyClient"
    }
}