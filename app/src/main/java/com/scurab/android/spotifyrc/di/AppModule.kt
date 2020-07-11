package com.scurab.android.spotifyrc.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.scurab.android.spotify.api.CachingApi
import com.scurab.android.spotify.api.SpotifyApi
import com.scurab.android.spotify.api.okhttp.AuthInterceptor
import com.scurab.android.spotify.api.okhttp.IHasAuthToken
import com.scurab.android.spotify.api.okhttp.NoInnerCrashingInterceptor
import com.scurab.android.spotifyrc.AppPrefs
import com.scurab.android.spotifyrc.spotify.SpotifyLocalClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Provides
    fun provideBluetoothAdapter() = BluetoothAdapter.getDefaultAdapter()

    @Provides
    fun provideSpotifyClient(@ApplicationContext context: Context) =
        SpotifyLocalClient(context, "da170041a6f94b938eadd5d8820778cf")

    @Provides
    @Singleton
    fun provideOkHttpClient(appPrefs: AppPrefs): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(5000, TimeUnit.MILLISECONDS)
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .writeTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(NoInnerCrashingInterceptor)
            .addInterceptor(AuthInterceptor(object : IHasAuthToken {
                override val accessToken: String get() = appPrefs.localAccessToken ?: ""//throw NullPointerException("No access token!")
            }))
            .addInterceptor(HttpLoggingInterceptor().also {
                it.level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("real")
    fun provideSpotifyApi(okHttpClient: OkHttpClient): SpotifyApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl("https://api.spotify.com/v1/")
            .build()
            .create(SpotifyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCachingSpotifyApi(@Named("real") spotifyApi: SpotifyApi): SpotifyApi {
        return CachingApi(spotifyApi)
    }
}
