package com.scurab.android.spotifyrc

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.scurab.android.spotifyrc.databinding.FragmentHomeBinding
import com.scurab.android.spotifyrc.ext.replaceFragment
import com.scurab.android.spotifyrc.service.RemoteControlService
import com.scurab.android.spotifyrc.util.viewBinding
import com.scurab.android.spotifyrc.viewmodel.HomeNavigationToken
import com.scurab.android.spotifyrc.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val views by viewBinding { FragmentHomeBinding.bind(requireView()) }
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.server.setOnClickListener {
            viewModel.onServerClicked()
        }

        views.client.setOnClickListener {
            viewModel.onClientClicked()
        }

        views.bluetoothScanner.setOnClickListener {
            viewModel.onScannerClicked()
        }
        bindViewModel()
    }

    private fun bindViewModel() {
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
            }
        }
    }
}