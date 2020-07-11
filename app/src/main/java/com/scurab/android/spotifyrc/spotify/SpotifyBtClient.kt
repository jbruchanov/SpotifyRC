package com.scurab.android.spotifyrc.spotify

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.scurab.android.spotifyrc.AppPrefs
import com.scurab.android.spotifyrc.commands.Command
import com.scurab.android.spotifyrc.ext.readPacket
import com.scurab.android.spotifyrc.ext.sendPacket
import com.scurab.android.spotifyrc.model.IHasBackingMap
import com.scurab.android.spotifyrc.model.Packet
import com.scurab.android.spotifyrc.model.PlayerStateKt
import com.scurab.android.spotifyrc.service.BluetoothServer
import com.scurab.android.spotifyrc.util.JsonConverter
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.concurrent.thread

class SpotifyBtClient @Inject constructor(
    private val adapter: BluetoothAdapter,
    private val appPrefs: AppPrefs,
    private val jsonConverter: JsonConverter
) : ISpotifyClient {

    private val ping = jsonConverter.toJson(Command.Ping()).toByteArray()

    private var socket: BluetoothSocket? = null

    private val _state = MutableLiveData<ConnectingState>().also { it.value = ConnectingState.Disconnected }
    val state: LiveData<ConnectingState> get() = _state
    private val _playerState = MutableLiveData<PlayerStateKt>()
    override val playerState: LiveData<PlayerStateKt> get() = _playerState
    private val isRunning = AtomicBoolean(false)
    override val connectedState: ConnectingState get() = _state.value ?: ConnectingState.Disconnected

    private val _image = MutableLiveData<Pair<Bitmap?, ByteArray?>>()
    override val image: LiveData<Pair<Bitmap?, ByteArray?>> = _image

    override fun connect() {
        isRunning.set(true)
        val mac = appPrefs.selectedDeviceMac ?: throw IllegalStateException("No selected device")
        Log.d(TAG, "Connect(), hasSocket:${socket != null}")
        if (socket == null) {
            _state.value = ConnectingState.Connecting
            val socket = adapter.bondedDevices
                ?.firstOrNull { it.address == mac }
                ?.createRfcommSocketToServiceRecord(BluetoothServer.uuid)

            Log.d(TAG, "Connect, foundSocket:${socket != null}")
            if (socket != null) {
                handle(socket)
            } else {
                _state.value = ConnectingState.Disconnected
            }
        }
    }

    override fun close() {
        _state.value = ConnectingState.Disconnected
        isRunning.set(false)
        socket?.close()
        socket = null
    }

    override suspend fun isPaused(): Boolean = playerState.value?.isPaused ?: true

    override suspend fun resume() {
        sendCommand(Command.Resume())
    }

    override suspend fun pause() {
        sendCommand(Command.Pause())
    }

    override suspend fun playNext() {
        sendCommand(Command.Next())
    }

    override suspend fun playPrevious() {
        sendCommand(Command.Previous())
    }

    override suspend fun play(id: String) {
        sendCommand(Command.Play(id))
    }

    private fun onReceivedMessage(item: Any) {
        when (item) {
            is PlayerStateKt -> _playerState.postValue(item)
            is Packet.Bitmap -> _image.postValue(Pair(item.data, null))
        }
    }

    private fun sendCommand(item: IHasBackingMap) {
        val json = jsonConverter.toJson(item).toByteArray()
        socket?.outputStream?.sendPacket(Packet.TYPE_JSON, json) ?: throw NullPointerException("Not connected?!")
    }

    private fun handle(socket: BluetoothSocket) {
        GlobalScope.launch(Dispatchers.IO) {
            var pingJob: Job? = null
            Log.d(TAG, "socketHandler started")
            val buffer = ByteArray(8)
            try {
                socket.connect()
                //pingJob = startPingJob(socket)
                _state.postValue(ConnectingState.Connected)
                this@SpotifyBtClient.socket = socket
                while (isRunning.get() && socket.isConnected) {
                    val packet = socket.inputStream.readPacket(buffer)
                    Log.d(TAG, "socketHandler msg:${packet}")
                    when (packet) {
                        is Packet.Json -> {
                            when(packet.type) {
                                Packet.TYPE_JSON -> onReceivedMessage(jsonConverter.fromJson(packet.string))
                                Packet.TYPE_JSON_STATE -> onReceivedMessage(jsonConverter.fromJson(packet.string, PlayerStateKt::class.java))
                                Packet.TYPE_BITMAP -> onReceivedMessage(packet)
                            }
                        }
                        is Packet.Bitmap -> _image.postValue(Pair(packet.data, null))
                        else -> throw IllegalStateException("Invalid packet:${packet}")
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "socketHandler exception:${e.message}")
            }
            this@SpotifyBtClient.socket = null
            isRunning.set(false)
            _state.postValue(ConnectingState.Disconnected)
            Log.d(TAG, "socketHandler ended")
            pingJob?.cancel()
        }
    }

    private fun startPingJob(socket: BluetoothSocket): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    delay(BluetoothServer.TIMEOUT * 2)
                    socket.outputStream.sendPacket(Packet.TYPE_JSON, ping)
                    Log.d(TAG, "Ping server OK")
                } catch (e: Exception) {
                    Log.d(TAG, "Ping server failed")
                }
            }
        }
    }

    companion object {
        private const val TAG = "SpotifyBtClient"
    }
}