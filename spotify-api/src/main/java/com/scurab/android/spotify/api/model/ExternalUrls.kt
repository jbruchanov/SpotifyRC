package com.scurab.android.spotify.api.model

import com.google.gson.annotations.SerializedName

data class ExternalUrls(

    @SerializedName("spotify") val spotify: String
)