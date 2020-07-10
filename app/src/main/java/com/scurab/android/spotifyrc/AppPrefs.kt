package com.scurab.android.spotifyrc

import android.content.Context
import com.scurab.android.spotifyrc.property.SharedPrefsProperty.Companion.string
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrefs @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE)

    var selectedDeviceName: String? by string(sharedPreferences, null)
    var selectedDeviceMac: String? by string(sharedPreferences, null)
}