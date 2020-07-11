package com.scurab.android.spotify.api

import com.scurab.android.spotify.api.model.Album
import com.scurab.android.spotify.api.model.Search

class CachingApi(private val spotifyApi: SpotifyApi) : SpotifyApi {
    private val albums = mutableMapOf<String, Album>()
    private val searches = mutableMapOf<String, Search>()

    override suspend fun getAlbum(id: String): Album {
        return albums[id] ?: spotifyApi.getAlbum(id).also { albums[id] = it }
    }

    override suspend fun search(query: String, type: String, market: String?): Search {
        val key = "$query|$type|$market"
        return searches[key] ?: spotifyApi.search(query, type, market).also { searches[key] = it }
    }
}