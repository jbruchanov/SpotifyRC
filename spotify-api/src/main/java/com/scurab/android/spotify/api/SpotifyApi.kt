package com.scurab.android.spotify.api

import com.scurab.android.spotify.api.model.Album
import com.scurab.android.spotify.api.model.Search
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApi {

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Album

    /*
    curl -X "GET" "https://api.spotify.com/v1/search?q=Lucie&type=track%2Cartist&market=cz&limit=1&offset=5" -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: Bearer BQDBpNRl8aUmdYMxiXPA-PVcNfgu_2QISFBa54qPgKsXYd20AqZOEW8qsu-sYnVzzFFv4fkvAyCDKG5shZsEiuv5YDM5tQIpp3WmID4J4NU0YoUPsIqPRasq7CeaGC0snQafnVOHRxtqgvETMfAa4UY9NpBQxhvokw"
     */
    @GET("search")
    suspend fun search(@Query("q") query: String, @Query("type") type: String, @Query("market") market: String?): Search
}