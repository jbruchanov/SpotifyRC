package com.scurab.android.spotifyrc.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.observe
import com.scurab.android.spotifyrc.AppActivity
import com.scurab.android.spotifyrc.R
import com.scurab.android.spotifyrc.ext.zipWith
import com.scurab.android.spotifyrc.spotify.SpotifyLocalClient
import kotlinx.coroutines.*

class RemoteControlService : LifecycleService() {

    private val TAG = "RemoteControlService"
    private val bluetoothServer = BluetoothServer().also {
        it.onCommandListener = { cmd ->
            GlobalScope.launch(Dispatchers.Main) {
                cmd.invoke(spotifyClient)
            }
        }
    }
    private lateinit var spotifyClient: SpotifyLocalClient
    private val state by lazy {
        bluetoothServer.devices.zipWith(spotifyClient.state) { x, y -> Pair(x, "Spotify:$y") }
    }

    override fun onCreate() {
        super.onCreate()
        spotifyClient = SpotifyLocalClient(this, "da170041a6f94b938eadd5d8820778cf")
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
        spotifyClient.playerState.observe(this) {
            bluetoothServer.latestState = it
            bluetoothServer.sendToClient(it)
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
            .setContentIntent(PendingIntent.getActivity(this, 1, Intent(this, AppActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
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