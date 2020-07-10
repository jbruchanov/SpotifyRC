package com.scurab.android.spotifyrc.arch

/**
 * Simple interface for delegating code execution on particular thread
 *
 */
interface ITaskScheduler {

    /**
     * Simple detection if current execution context is on MainThread
     */
    //mainly for testing, to avoid using robolectric just for this
    val isMainThread: Boolean

    /**
     * Execute runnable using particular thread/executor
     *
     * @param runnable
     */
    fun run(runnable: () -> Unit)
}