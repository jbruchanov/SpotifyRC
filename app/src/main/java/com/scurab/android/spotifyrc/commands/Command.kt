package com.scurab.android.spotifyrc.commands

import com.scurab.android.spotifyrc.model.IHasBackingMap
import com.scurab.android.spotifyrc.spotify.SpotifyLocalClient

sealed class Command(override val data: MutableMap<String, Any?> = mutableMapOf()) : IHasBackingMap {

    init {
        data["name"] = this::class.java.name
    }

    class Resume : Command() {
        override suspend fun invoke(spotifak: SpotifyLocalClient) {
            spotifak.resume()
        }
    }

    class Pause : Command() {
        override suspend fun invoke(spotifak: SpotifyLocalClient) {
            spotifak.pause()
        }
    }

    class Next : Command() {
        override suspend fun invoke(spotifak: SpotifyLocalClient) {
            spotifak.playNext()
        }
    }

    class Previous : Command() {
        override suspend fun invoke(spotifak: SpotifyLocalClient) {
            spotifak.playPrevious()
        }
    }

    class Ping : Command() {
        override suspend fun invoke(spotifak: SpotifyLocalClient) {

        }
    }

    abstract suspend fun invoke(spotifak: SpotifyLocalClient)
}