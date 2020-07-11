package com.scurab.android.spotifyrc.model

sealed class Packet(val type: Int) {
    class Json(val string: String, type: Int) : Packet(type) {
        override fun toString(): String = "Json:'$string'"
    }

    class Bitmap(val data: android.graphics.Bitmap, type: Int) : Packet(type)
    class Binary(val data: ByteArray, type: Int) : Packet(type)

    companion object {
        const val TYPE_JSON = 1
        const val TYPE_BITMAP = 2
        const val TYPE_BINARY = 3
        const val TYPE_JSON_STATE = 100
    }
}