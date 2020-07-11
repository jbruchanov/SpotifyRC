package com.scurab.android.spotifyrc

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import com.scurab.android.spotifyrc.databinding.FragmentHomeBinding
import com.scurab.android.spotifyrc.ext.replaceFragment
import com.scurab.android.spotifyrc.service.RemoteControlService
import com.scurab.android.spotifyrc.util.viewBinding
import com.scurab.android.spotifyrc.viewmodel.HomeNavigationToken
import com.scurab.android.spotifyrc.viewmodel.HomeViewModel
import com.spotify.android.appremote.internal.SpotifyLocator
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val views by viewBinding { FragmentHomeBinding.bind(requireView()) }
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        bindViewModel()
    }

    private fun bindViews() = views.apply {
        server.setOnClickListener { viewModel.onServerClicked() }
        client.setOnClickListener { viewModel.onClientClicked() }
        bluetoothScanner.setOnClickListener { viewModel.onScannerClicked() }
        spotify.setOnClickListener { viewModel.onSpotifyClicked() }
    }

    private fun bindViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) {
            views.spotify.drawable?.setTint(
                ContextCompat.getColor(requireContext(), if (it.isLoggedIn) R.color.colorPrimary else R.color.gray)
            )
        }
        viewModel.navigation.observe(viewLifecycleOwner) {
            when (it) {
                HomeNavigationToken.StartServer -> {
                    requireActivity().startService(Intent(requireContext(), RemoteControlService::class.java).apply {
                        action = RemoteControlService.ACTION_START
                    })
                    Toast.makeText(requireContext(), R.string.server_started, Toast.LENGTH_LONG).apply {
                        setGravity(Gravity.CENTER, 0, 0)
                    }.show()
                    requireActivity().moveTaskToBack(true)
                }
                HomeNavigationToken.OpenClient -> replaceFragment(ClientFragment())
                HomeNavigationToken.OpenBluetoothScanner -> replaceFragment(BluetoothScannerFragment())
                HomeNavigationToken.ErrorNoSpotifyApp -> {
                    Snackbar.make(requireView(), R.string.install_spotify_app, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.install) {
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")).also {
                                requireActivity().startActivity(it)
                            }
                        }.show()
                }
                HomeNavigationToken.ErrorNoServerSelected -> {
                    Snackbar.make(requireView(), R.string.select_service_first, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.open_bluetooth_scanner) {
                            viewModel.onScannerClicked()
                        }.show()
                }
                HomeNavigationToken.ErrorBluetoothOff -> {
                    Snackbar.make(requireView(), R.string.turn_on_bluetooth, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.turn_on) {
                            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
                        }.show()
                }
                HomeNavigationToken.ErrorConnectToSelectedDeviceFirst -> {
                    Snackbar.make(requireView(), R.string.pair_with_device_first, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.connect) {
                            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        }.show()
                }
                HomeNavigationToken.ErrorNeedLocationPermission -> {
                    Snackbar.make(requireView(), R.string.bt_scanner_needs_location_perms, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ask) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requireActivity().requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
                            }
                        }.show()
                }
                HomeNavigationToken.SpotifyLogin -> {
                    val request = AuthorizationRequest.Builder(
                        resources.getString(R.string.spotify_client_id),
                        AuthorizationResponse.Type.TOKEN,
                        App.REDIRECT_URL
                    ).setScopes(arrayOf("user-read-email")).build()
                    val intent = AuthorizationClient.createLoginActivityIntent(requireActivity(), request)
                    startActivityForResult(intent, SPOTIFY_REQ_CODE)
                }
                HomeNavigationToken.SpotifyLoggedOut -> {
                    Snackbar.make(requireView(), R.string.you_have_been_logged_out, Snackbar.LENGTH_SHORT).show()
                }
                HomeNavigationToken.SpotifyOk -> {
                    Snackbar.make(requireView(), R.string.you_have_been_logged_in, Snackbar.LENGTH_SHORT).show()
                }
                HomeNavigationToken.SpotifyError -> {
                    Snackbar.make(requireView(), R.string.unexpected_spotify_response, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.ok, null)
                        .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPOTIFY_REQ_CODE && resultCode == Activity.RESULT_OK) {
            AuthorizationClient.getResponse(resultCode, data)?.let {
                viewModel.onSpotifyResult(it)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val SPOTIFY_REQ_CODE = 123
    }
}