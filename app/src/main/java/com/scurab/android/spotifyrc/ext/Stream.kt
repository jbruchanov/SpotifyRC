package com.scurab.android.spotifyrc.ext

import android.graphics.BitmapFactory
import com.scurab.android.spotifyrc.model.Packet
import input.BoundedInputStream
import java.io.InputStream
import java.io.OutputStream


private fun ByteArray.readInt(offset: Int = 0): Int {
    val i1 = this[offset].toUInt()
    val i2 = this[offset + 1].toUInt()
    val i3 = this[offset + 2].toUInt()
    val i4 = this[offset + 3].toUInt()
    return (i1 shl 24) or (i2 shl 16) or (i3 shl 8) or i4
}

fun Byte.toUInt(): Int = toInt().let { if (it < 0) 256 + it else it }

private fun Int.writeToStream(outputStream: OutputStream) {
    outputStream.write(this ushr 24 and 0xFF)
    outputStream.write(this ushr 16 and 0xFF)
    outputStream.write(this ushr 8 and 0xFF)
    outputStream.write(this and 0xFF)
}

fun InputStream.readPacket(buffer: ByteArray): Packet {
    var read = 0
    do {
        val r = read(buffer, read, 5 - read)
        read += r
    } while (read < 5)
    val dataLen = buffer.readInt()
    val dataType = buffer[4].toUInt()
    val boundedInputStream = BoundedInputStream(this, dataLen.toLong())
    return when (dataType) {
        Packet.TYPE_JSON,
        Packet.TYPE_JSON_STATE -> Packet.Json(boundedInputStream.bufferedReader().readText(), dataType)
        Packet.TYPE_BITMAP -> Packet.Bitmap(BitmapFactory.decodeStream(boundedInputStream), dataType)
        Packet.TYPE_BINARY -> Packet.Binary(boundedInputStream.readBytes(), dataType)
        else -> throw IllegalStateException("Unsupported packet type:${dataType}")
    }
}

fun OutputStream.sendPacket(type: Int, data: ByteArray) {
    data.size.writeToStream(this)
    write(type)
    write(data)
}