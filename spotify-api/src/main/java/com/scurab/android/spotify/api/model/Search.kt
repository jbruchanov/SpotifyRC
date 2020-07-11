package com.scurab.android.spotify.api.model

import com.google.gson.annotations.SerializedName

class Search(
    @SerializedName("artists") val artists: Artists,
    @SerializedName("tracks") val tracks: Tracks
)