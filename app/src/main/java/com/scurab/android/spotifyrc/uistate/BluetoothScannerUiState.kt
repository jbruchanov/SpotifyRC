package com.scurab.android.spotifyrc.uistate

data class BluetoothScannerUiState(
    val isScanning: Boolean,
    val deviceName: String?,
    val deviceMac: String?
)