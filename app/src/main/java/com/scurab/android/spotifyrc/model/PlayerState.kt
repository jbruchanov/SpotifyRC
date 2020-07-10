package com.scurab.android.spotifyrc.model

import com.spotify.protocol.types.PlayerState

class PlayerStateKt(override val data: MutableMap<String, Any?> = mutableMapOf()) : IHasBackingMap {
    var trackArtistName: String? by data
    var trackArtistUri: String? by data
    var trackImageUri: String? by data
    var trackName: String? by data
    var trackUri: String? by data
    var trackAlbumName: String? by data
    var trackAlbumUri: String? by data
    var playbackOptionsShuffling: Boolean by data
    var playbackOptionsRepeatMode: Int by data
    var playbackRestrictionsCanSkipPrev: Boolean by data
    var playbackRestrictionsCanSkipNext: Boolean by data
    var isPaused: Boolean by data
    var playbackPosition: Long by data

    constructor(state: PlayerState) : this() {
        trackArtistName = state.track?.artist?.name
        trackArtistUri = state.track?.artist?.uri
        trackImageUri = state.track?.imageUri?.raw
        trackName = state.track?.name
        trackUri = state.track?.uri
        trackAlbumName = state.track?.album?.name
        trackAlbumUri = state.track?.album?.uri
        playbackOptionsShuffling = state.playbackOptions?.isShuffling ?: false
        playbackOptionsRepeatMode = state.playbackOptions?.repeatMode ?: -1
        playbackRestrictionsCanSkipPrev = state.playbackRestrictions?.canSkipPrev ?: false
        playbackRestrictionsCanSkipNext = state.playbackRestrictions?.canSkipNext ?: false
        isPaused = state.isPaused
        playbackPosition = state.playbackPosition
    }
}