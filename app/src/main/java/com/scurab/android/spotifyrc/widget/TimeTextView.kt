package com.scurab.android.spotifyrc.widget

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class TimeTextView : AppCompatTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var lastTick = 0L

    var isTicking: Boolean = false; private set
    var timeFormat: DateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    var time: Long = 0
        set(value) {
            field = max(0L, value)
            refreshTime()
        }

    fun setTicking(enabled: Boolean) {
        isTicking = enabled
        lastTick = SystemClock.elapsedRealtime()
        refreshTime()
    }

    private val tick = Runnable {
        val now = SystemClock.elapsedRealtime()
        val diff = now - lastTick
        time += diff
        lastTick = now
        refreshTime()
    }

    private fun refreshTime() {
        text = timeFormat.format(time)
        removeCallbacks(tick)
        if (isTicking) {
            postDelayed(tick, 500)
        }
    }
}