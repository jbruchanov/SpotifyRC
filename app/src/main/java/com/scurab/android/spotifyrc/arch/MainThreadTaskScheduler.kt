package com.scurab.android.spotifyrc.arch

import android.os.Handler
import android.os.Looper

/**
 * Android implementation of [ITaskScheduler] using MainThread
 *
 * User [MainThreadTaskScheduler.Instance]
 *
 */
class MainThreadTaskScheduler private constructor() : ITaskScheduler {
    private val handler = Handler(Looper.getMainLooper())

    override val isMainThread: Boolean = Looper.myLooper() == Looper.getMainLooper()

    override fun run(runnable: () -> Unit) {
        handler.post(runnable)
    }

    companion object {
        val Instance = MainThreadTaskScheduler()
    }
}