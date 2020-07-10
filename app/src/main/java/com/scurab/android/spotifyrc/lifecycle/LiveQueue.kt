package com.scurab.android.spotifyrc.lifecycle

import androidx.lifecycle.LifecycleOwner

/**
 * Iface similar to [androidx.lifecycle.LiveData.LiveData]
 *
 * @param T
 */
interface LiveQueue<T> {
    /**
     * Is any observer attached
     */
    val hasObserver: Boolean

    /**
     * Observe
     *
     * @param lifecycleOwner
     * @param observer
     */
    fun observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit)

    /**
     * Remove attached observer if exists
     *
     */
    fun removeObserver()
}