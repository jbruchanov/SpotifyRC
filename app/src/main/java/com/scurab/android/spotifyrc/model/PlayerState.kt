package com.scurab.android.spotifyrc.model

import com.spotify.protocol.types.PlayerState

@Suppress("ConvertSecondaryConstructorToPrimary")
class PlayerStateKt {
    var trackArtistName: String? = null
    var trackArtistUri: String? = null
    var trackImageUri: String? = null
    var trackName: String? = null
    var trackUri: String? = null
    var trackAlbumName: String? = null
    var trackAlbumId: String? = null
    var trackAlbumUrl: String? = null
    var playbackOptionsShuffling: Boolean = false
    var playbackOptionsRepeatMode: Int = 0
    var playbackRestrictionsCanSkipPrev: Boolean = false
    var playbackRestrictionsCanSkipNext: Boolean = false
    var isPaused: Boolean? = null
    var playbackPosition: Long = 0
    var albumTracks: List<STrack>? = null

    constructor(state: PlayerState) {
        trackArtistName = state.track?.artist?.name
        trackArtistUri = state.track?.artist?.uri
        trackImageUri = state.track?.imageUri?.raw
        trackName = state.track?.name
        trackUri = state.track?.uri
        trackAlbumName = state.track?.album?.name
        trackAlbumId = state.track?.album?.uri?.substring("spotify:album:".length)
        playbackOptionsShuffling = state.playbackOptions?.isShuffling ?: false
        playbackOptionsRepeatMode = state.playbackOptions?.repeatMode ?: -1
        playbackRestrictionsCanSkipPrev = state.playbackRestrictions?.canSkipPrev ?: false
        playbackRestrictionsCanSkipNext = state.playbackRestrictions?.canSkipNext ?: false
        isPaused = state.isPaused
        playbackPosition = state.playbackPosition
    }
}