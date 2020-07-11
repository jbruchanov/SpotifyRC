package com.scurab.android.spotifyrc

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.scurab.android.spotifyrc.service.RemoteControlService
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannels = ArrayList<NotificationChannel>()
            notificationChannels.add(
                NotificationChannel(
                    RemoteControlService.NOTIFICATION_CHANNEL,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            notificationManager.createNotificationChannels(notificationChannels)
        }
    }


    companion object {
        const val REDIRECT_URL = "spotifyrc://redirect"
    }
}