package com.scurab.android.spotifyrc.model

import com.scurab.android.spotify.api.model.Album

class STrack(override val data: MutableMap<String, Any?> = mutableMapOf()) : IHasBackingMap {
    var id: String by data
    var number: Int by data
    var uri: String by data
    var name: String by data

    constructor(id: String, trackNumber: Int, uri: String, name: String) : this() {
        this.id = id
        this.number = trackNumber
        this.uri = uri
        this.name = name
    }
}


fun Album.getSimpleTracks(): List<STrack> {
    return tracks?.items
        ?.map { STrack(it.id, it.track_number, it.uri, it.name) }
        ?.sortedBy { it.number }
}