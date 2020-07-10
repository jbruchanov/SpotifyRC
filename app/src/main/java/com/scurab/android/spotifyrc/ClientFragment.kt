package com.scurab.android.spotifyrc

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.scurab.android.spotifyrc.databinding.FragmentClientBinding
import com.scurab.android.spotifyrc.spotify.ConnectingState
import com.scurab.android.spotifyrc.util.viewBinding
import com.scurab.android.spotifyrc.viewmodel.ClientViewModel
import com.spotify.protocol.types.Track
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientFragment : Fragment(R.layout.fragment_client) {

    private val viewModel by viewModels<ClientViewModel>()
    private val views by viewBinding { FragmentClientBinding.bind(requireView()) }
    private val viewBoundToConnectivity by viewBinding {
        listOf(views.trackNext, views.trackPlayPause, views.trackPrevious)
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
        views.album.isSelected = true
    }

    private fun bindViewModel() {
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

        viewModel.image.observe(viewLifecycleOwner) { bitmap ->
            TransitionDrawable(arrayOf(views.image.drawable, BitmapDrawable(resources, bitmap))).also {
                views.image.setImageDrawable(it)
                it.startTransition(resources.getInteger(R.integer.activity_anim_duration))
            }
        }

        viewModel.playerState.observe(viewLifecycleOwner) {
            Log.d("ClientFragment", "Updated state")
            views.apply {
                if (album.text.toString() != it.trackAlbumName) {
                    album.text = it.trackAlbumName
                }
                time.time = it.playbackPosition
                time.setTicking(!it.isPaused)
                track.text = it.trackName
                artist.text = it.trackArtistName
                trackPrevious.isEnabled = it.playbackRestrictionsCanSkipPrev
                trackNext.isEnabled = it.playbackRestrictionsCanSkipNext
                val resId = if (it.isPaused) R.drawable.ic_baseline_play_arrow_48 else R.drawable.ic_baseline_pause_48
                trackPlayPause.setImageDrawable(resources.getDrawable(resId, requireActivity().theme))
            }
        }
    }
}