package com.scurab.android.spotifyrc.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.scurab.android.spotifyrc.util.JsonConverter
import com.scurab.android.spotifyrc.commands.Command
import com.scurab.android.spotifyrc.model.IHasBackingMap
import com.scurab.android.spotifyrc.model.PlayerStateKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread


class BluetoothServer(
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
) {

    private var serverSocket: BluetoothServerSocket? = null
    private val jsonConverter = JsonConverter()
    var latestState: PlayerStateKt? = null
    var isRunning: Boolean = false; private set
    var onCommandListener: ((Command) -> Unit)? = null

    private val sockets = Collections.synchronizedSet(mutableSetOf<BluetoothSocket>())
    private val _devices = MutableLiveData<Int>().also { it.value = 0 }
    val devices: LiveData<Int> = _devices

    fun start() {
        if (isRunning) return
        isRunning = true
        Log.d(TAG, "Starting server coroutine")
        GlobalScope.launch(Dispatchers.IO) {
            while (isRunning) {
                Log.d(TAG, "Server coroutine loop restart")
                try {
                    serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                        "SpotifyRC",
                        uuid
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d(TAG, "Server coroutine unable to create server socket:${e.message}")
                }
                serverSocket?.let { serverSocket ->
                    try {
                        while (isRunning) {
                            val socket = serverSocket.accept()
                            Log.d(TAG, "connection src:${socket.remoteDevice.name}")
                            handleSocket(socket)
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "err:${e.message}")
                    }
                }
                serverSocket = null
                if (isRunning) {
                    delay(TIMEOUT)
                }
            }
        }
    }

    fun stop() {
        kotlin.runCatching {
            isRunning = false
            serverSocket?.close()
            serverSocket = null
        }
    }

    private fun handleSocket(socket: BluetoothSocket) {
        sockets.add(socket)
        GlobalScope.launch(Dispatchers.IO) {
            _devices.inc()
            kotlin.runCatching {
                latestState?.let { sendToClient(it) }
                val array = ByteArray(8096)
                while (socket.isConnected && isRunning) {
                    val inputStream = socket.inputStream
                    val read = inputStream.read(array)
                    val message = String(array, 0, read)
                    Log.d(TAG, "message:'$message' src:${socket.remoteDevice.name}")
                    val cmd = jsonConverter.fromJson<Command>(message)
                    onCommandListener?.invoke(cmd)
                }
            }
            _devices.dec()
            sockets.remove(socket)
            Log.d(TAG, "Disconnected src:${socket.remoteDevice.name}")
        }
    }

    private fun MutableLiveData<Int>.inc() {
        synchronized(this) {
            postValue((value ?: 0) + 1)
        }
    }

    private fun MutableLiveData<Int>.dec() {
        synchronized(this) {
            postValue(value?.let { it - 1 } ?: 0)
        }
    }

    fun sendToClient(item: IHasBackingMap) {
        if (sockets.isEmpty()) {
            return
        }
        val className = item::class.java.name
        item.data["name"] = className
        val json = jsonConverter.toJson(item).toByteArray()
        sockets.forEach {
            try {
                it.outputStream.write(json)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val TAG = "BluetoothServer"
        val uuid = UUID.fromString("119fc016-5090-4089-b3a0-4ce52abf464f")
        const val TIMEOUT = 5000L
    }
}