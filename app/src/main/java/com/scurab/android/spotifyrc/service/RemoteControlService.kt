package com.scurab.android.spotifyrc.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.scurab.android.spotify.api.SpotifyApi
import com.scurab.android.spotifyrc.AppActivity
import com.scurab.android.spotifyrc.R
import com.scurab.android.spotifyrc.ext.zipWith
import com.scurab.android.spotifyrc.model.Packet
import com.scurab.android.spotifyrc.model.getSimpleTracks
import com.scurab.android.spotifyrc.spotify.SpotifyLocalClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class RemoteControlService : LifecycleService() {

    private val TAG = "RemoteControlService"
    private val bluetoothServer = BluetoothServer().also {
        it.onCommandListener = { cmd ->
            GlobalScope.launch(Dispatchers.Main) {
                cmd.invoke(spotifyClient)
            }
        }
    }

    @Inject
    lateinit var spotifyClient: SpotifyLocalClient

    @Inject
    lateinit var spotifyApi: SpotifyApi

    private val state by lazy {
        bluetoothServer.devices.zipWith(spotifyClient.state) { x, y -> Pair(x, "Spotify:$y") }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Action:${intent?.action}")
        when (intent?.action ?: "") {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }

        return onStartCommand
    }

    private var isStarted = false
    private fun start() {
        isStarted = true
        bluetoothServer.start()
        startForeground(1, buildNotification("Created"))
        GlobalScope.launch(Dispatchers.Main) {
            try {
                spotifyClient.connect()
            } catch (e: Throwable) {
                updateNotification(e.message ?: e::class.java.simpleName, 0)
            }
        }
        state.observe(this) {
            val (devices, state) = it
            updateNotification(state, devices)
        }

        bindSpotifyClient()
    }

    private fun bindSpotifyClient() {
        spotifyClient.image.observe(this) { bitmap ->
            val byteArray = bitmap.second
            bluetoothServer.latestBitmap = bitmap.second
            if (byteArray != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    bluetoothServer.sendToClient(Packet.TYPE_BITMAP, byteArray)
                }
            }
        }
        spotifyClient.playerState.observe(this) { state ->
            val firstState = bluetoothServer.latestState == null
            bluetoothServer.latestState = state
            if (firstState) {
                bluetoothServer.sendToClient(state)
            }

            val albumId = state.trackAlbumId
            if (albumId != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    kotlin.runCatching { spotifyApi.getAlbum(albumId) }.getOrNull()?.let { album ->
                        state.albumTracks = album.getSimpleTracks()
                        state.trackAlbumUrl = album.getBestImageUrl()
                    }
                    bluetoothServer.sendToClient(state)
                }
            } else if (!firstState) {
                bluetoothServer.sendToClient(state)
            }
        }
    }

    private fun stop() {
        isStarted = false
        spotifyClient.close()
        bluetoothServer.stop()
        stopForeground(true)
        stopSelf()
    }

    private fun updateNotification(msg: String, devices: Int) {
        if (isStarted) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)?.let {
                it.notify(1, buildNotification(msg, devices))
            }
        }
    }

    private fun buildNotification(msg: String, devices: Int = 0): Notification {
        return NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL
        )
            .setAutoCancel(true)
            .setContentText("${msg}, devices:${devices}")
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_stat_music)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    1,
                    Intent(this, AppActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                R.drawable.ic_stat_music,
                "Stop",
                PendingIntent.getService(this, 1, Intent(this, RemoteControlService::class.java).apply {
                    action = ACTION_STOP
                }, PendingIntent.FLAG_UPDATE_CURRENT)
            )
            .build()
    }

    override fun onDestroy() {
        stop()
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_CHANNEL = "bluetooth_server"
        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
    }
}