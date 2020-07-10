package com.scurab.android.spotifyrc.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.scurab.android.spotifyrc.spotify.SpotifyLocalClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Provides
    fun provideBluetoothAdapter() = BluetoothAdapter.getDefaultAdapter()

    @Provides
    fun provideSpotifyClient(@ApplicationContext context: Context) =
        SpotifyLocalClient(context, "da170041a6f94b938eadd5d8820778cf")
}
