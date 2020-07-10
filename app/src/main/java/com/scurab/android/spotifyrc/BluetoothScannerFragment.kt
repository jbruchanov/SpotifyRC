package com.scurab.android.spotifyrc

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.scurab.android.spotifyrc.adapter.BluetoothDevicesAdapter
import com.scurab.android.spotifyrc.databinding.FragmentBluetoothScannerBinding
import com.scurab.android.spotifyrc.databinding.ViewBluetoothDeviceBinding
import com.scurab.android.spotifyrc.util.viewBinding
import com.scurab.android.spotifyrc.viewmodel.BluetoothScannerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BluetoothScannerFragment : Fragment(R.layout.fragment_bluetooth_scanner) {

    private val views by viewBinding { FragmentBluetoothScannerBinding.bind(requireView()) }
    private val selectedDeviceViews by viewBinding { ViewBluetoothDeviceBinding.bind(views.root.findViewById(R.id.view_bluetooth_device)) }
    private val viewModel: BluetoothScannerViewModel by viewModels()
    private val adapter by viewBinding {
        BluetoothDevicesAdapter {
            viewModel.onDeviceSelected(it)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onResume() {
        super.onResume()
        views.icon.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink).also {
            it.start()
        }
    }

    override fun onStop() {
        viewModel.onStop()
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews()
        bindViewModel()
    }

    private fun bindViewModel() = viewModel.apply {
        uiState.observe(viewLifecycleOwner) { state ->
            if (state.isScanning) {
                views.icon.visibility = View.VISIBLE
                views.icon.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink).also {
                    it.start()
                }
            } else {
                views.icon.animation = null
                views.icon.visibility = View.INVISIBLE
            }
            selectedDeviceViews.deviceName.text = state.deviceName
            selectedDeviceViews.deviceAddress.text = state.deviceMac
        }
        devices.observe(viewLifecycleOwner) {
            adapter.setItems(it)
        }
    }

    private fun bindViews() = views.apply {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.restartDiscovery()
            swipeRefreshLayout.postDelayed(500) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}

