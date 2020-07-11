package com.scurab.android.spotifyrc.spotify

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import com.scurab.android.spotifyrc.App
import com.scurab.android.spotifyrc.model.PlayerStateKt
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.ImageUri
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resumeWithException

class SpotifyLocalClient(
    private val app: Context,
    private val clientId: String
) : ISpotifyClient {

    private var ignoreNextUpdateIfEmpty: Boolean = false
    private var spotify: SpotifyAppRemote? = null
    private val _playerState = MutableLiveData<PlayerStateKt>()
    override val playerState: LiveData<PlayerStateKt> = _playerState
    override val image: LiveData<Pair<Bitmap?, ByteArray?>> = MediatorLiveData<Pair<Bitmap?, ByteArray?>>().let { mediator ->
        mediator.addSource(playerState.map { it.trackImageUri }.distinctUntilChanged()) {
            if (it != null) {
                GlobalScope.launch {
                    loadImage(ImageUri(it))?.let { bitmap ->
                        val output = ByteArrayOutputStream(bitmap.byteCount)
                        if (bitmap.compress(Bitmap.CompressFormat.JPEG, 50, output)) {
                            mediator.postValue(Pair(null, output.toByteArray()))
                        }
                    }
                }
            } else {
                mediator.value = Pair(null, null)
            }
        }
        mediator
    }
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
                    .setRedirectUri(App.REDIRECT_URL)
                    .showAuthView(false)
                    .build(),
                object : ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        Log.d(TAG, "Connected")
                        _state.value = ConnectingState.Connected
                        spotify = spotifyAppRemote.also {
                            it.playerApi.subscribeToPlayerState().setEventCallback { state ->
                                Log.d(TAG, "Update state:${state}")
                                if (state != null) {
                                    if(!(ignoreNextUpdateIfEmpty && state.track == null)) {
                                        _playerState.value = PlayerStateKt(state)
                                    }
                                }
                                ignoreNextUpdateIfEmpty = false
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
        return spotify?.playerApi?.playerState?.fetch { isPaused } ?: true
    }

    suspend fun loadImage(uri: ImageUri): Bitmap? {
        return spotify?.imagesApi?.getImage(uri)?.fetch { this }
    }

    override suspend fun playNext() {
        spotify?.playerApi?.call { skipNext() }
    }

    override suspend fun playPrevious() {
        spotify?.playerApi?.call { skipPrevious() }
    }

    override suspend fun resume() {
        spotify?.playerApi?.call { resume() }
    }

    override suspend fun pause() {
        spotify?.playerApi?.call { pause() }
    }

    override suspend fun play(id: String) {
        spotify?.playerApi?.call {
            ignoreNextUpdateIfEmpty = true
            play(id)
        }
    }


    private suspend fun <R, T> R.call(block: R.() -> T): T {
        return suspendCancellableCoroutine { continuation ->
            this?.also {
                val result = block.invoke(it)
                continuation.resumeWith(Result.success(result))
            }
        }
    }

    private suspend fun <R, T> CallResult<R>.fetch(block: R.() -> T): T {
        return suspendCancellableCoroutine { continuation ->
            this?.also {
                it.setResultCallback { target ->
                    val result = block.invoke(target)
                    continuation.resumeWith(Result.success(result))
                }
            }
        }
    }

    companion object {
        const val TAG = "SpotifyClient"
    }
}