package com.scurab.android.spotifyrc

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.scurab.android.spotifyrc.adapter.AlbumTracksAdapter
import com.scurab.android.spotifyrc.databinding.FragmentClientBinding
import com.scurab.android.spotifyrc.ext.addFragment
import com.scurab.android.spotifyrc.ext.replaceFragment
import com.scurab.android.spotifyrc.spotify.ConnectingState
import com.scurab.android.spotifyrc.util.viewBinding
import com.scurab.android.spotifyrc.viewmodel.ClientNavigationToken
import com.scurab.android.spotifyrc.viewmodel.ClientViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientFragment : Fragment(R.layout.fragment_client) {

    private val viewModel by viewModels<ClientViewModel>()
    private val views by viewBinding { FragmentClientBinding.bind(requireView()) }
    private val viewBoundToConnectivity by viewBinding {
        listOf(views.trackNext, views.trackPlayPause, views.trackPrevious, views.search)
    }
    private val adapter by viewBinding {
        AlbumTracksAdapter {
            viewModel.onTrackClick(it)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        bindViewModel()
    }

    override fun onStart() {
        super.onStart()
        viewModel.connect()
    }

    override fun onStop() {
        viewModel.disconnect()
        super.onStop()
    }

    private fun bindViews() = views.apply {
        trackPlayPause.setOnClickListener { viewModel.playPause() }
        trackPrevious.setOnClickListener { viewModel.playPrevious() }
        trackNext.setOnClickListener { viewModel.playNext() }
        search.setOnClickListener { viewModel.search() }
        album.isSelected = true
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun bindViewModel() {
        viewModel.navigation.observe(viewLifecycleOwner) {
            when(it) {
                ClientNavigationToken.Search -> {
                    addFragment(SearchFragment(), animations = Common.slideUpDownAnimations)
                }
            }
        }

        viewModel.connectingState.observe(viewLifecycleOwner) { state ->
            viewBoundToConnectivity.forEach { v -> v.isEnabled = state == ConnectingState.Connected }
            views.track.text = ""
            views.artist.text = ""
            when (state) {
                ConnectingState.Disconnected -> {
                    views.album.setText(R.string.disconnected)
                    viewModel.reconnect()
                    views.time.setTicking(false)
                }
                ConnectingState.Connecting -> {
                    views.album.setText(R.string.connecting)
                    views.time.setTicking(false)
                }
                ConnectingState.Connected -> {
                    //player state will update it
                    views.album.setText(R.string.connected)
                }
            }
        }

        viewModel.image.observe(viewLifecycleOwner) { imageData ->
            setDrawableWithTransition(imageData.first)
        }

        var lastImageUri: String? = null
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            Log.d("ClientFragment", "Updated state")
            views.apply {
                if (album.text.toString() != state.trackAlbumName) {
                    album.text = state.trackAlbumName
                }
                time.time = state.playbackPosition
                time.setTicking(state.isPaused != true)
                track.text = state.trackName
                artist.text = state.trackArtistName
                trackPrevious.isEnabled = state.playbackRestrictionsCanSkipPrev
                trackNext.isEnabled = state.playbackRestrictionsCanSkipNext
                when (state.isPaused) {
                    true -> trackPlayPause.setImageDrawable(
                        resources.getDrawable(R.drawable.ic_baseline_play_arrow_48, requireActivity().theme)
                    )
                    false -> trackPlayPause.setImageDrawable(
                        resources.getDrawable(R.drawable.ic_baseline_pause_48, requireActivity().theme)
                    )
                    null -> trackPlayPause.setImageDrawable(null)

                }
                trackPlayPause.isEnabled = state.trackUri != null
                if (lastImageUri != null && state.trackImageUri == null) {
                    views.image.setImageResource(R.drawable.ic_baseline_album_96)
                }
                lastImageUri = state.trackImageUri
                adapter.items = state.albumTracks ?: emptyList()
                adapter.playginUri = state.trackUri
            }
        }
    }

    private fun setDrawableWithTransition(bitmap: Bitmap?) {
        if (bitmap != null) {
            TransitionDrawable(arrayOf(views.image.drawable, BitmapDrawable(resources, bitmap))).also {
                views.image.setImageDrawable(it)
                it.startTransition(resources.getInteger(R.integer.activity_anim_duration))
            }
        } else {
            views.image.setImageResource(R.drawable.ic_baseline_album_96)
        }
    }
}