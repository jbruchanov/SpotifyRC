package com.scurab.android.spotifyrc.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

fun <T> Fragment.viewBinding(initializer: () -> T) = lifecycleAwareLazy({ viewLifecycleOwner.lifecycle }, initializer)

/**
 * Handy shortcut for easier property delegation using [LifecycleAwareLazy]
 *
 * @param T
 * @param lifecycleProvider
 * @param initializer
 */
fun <T> lifecycleAwareLazy(lifecycleProvider: () -> Lifecycle, initializer: () -> T) = LifecycleAwareLazy(
    lifecycleProvider,
    initializer
)

/**
 * Lazy implementation with respect of lifecycle
 *
 * @param lifecycleProvider lambda is necessary, because [Lifecycle] doesn't have to be defined at
 * class instantiation time (e.g. fragment's view lifecycle provider)
 * @param initializer
 */
class LifecycleAwareLazy<out T>(
    private val lifecycleProvider: () -> Lifecycle,
    private val initializer: () -> T
) : Lazy<T> {
    private var _value: Any? = UNINITIALIZED_VALUE

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        @Suppress("unused")
        fun onDestroy() {
            reset()
        }
    }

    override val value: T
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                val provider = lifecycleProvider()
                if (provider.currentState != Lifecycle.State.DESTROYED) {
                    provider.addObserver(lifecycleObserver)
                    _value = initializer()
                } else {
                    //we are destroyed, so don't cache, potential memleak if we were caching
                    return initializer()
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String =
        if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    fun reset() {
        _value = UNINITIALIZED_VALUE
        lifecycleProvider().removeObserver(lifecycleObserver)
    }

    companion object {
        private val UNINITIALIZED_VALUE = Any()
    }
}