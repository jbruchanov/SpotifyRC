package com.scurab.android.spotifyrc.lifecycle

import android.annotation.SuppressLint
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.scurab.android.spotifyrc.arch.ITaskScheduler
import com.scurab.android.spotifyrc.arch.MainThreadTaskScheduler
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.Any
import kotlin.Boolean
import kotlin.ConcurrentModificationException
import kotlin.IllegalStateException
import kotlin.Int
import kotlin.Suppress
import kotlin.Unit
import kotlin.apply
import kotlin.check
import kotlin.require
import kotlin.synchronized

/**
 * Similar class to [androidx.lifecycle.LiveData].
 * Main differences are:
 * - can hold only 1 observer => updates are handled only 1 times
 * - there is a queue of updates => values or not overwritten if [Lifecycle] is not at least [Lifecycle.Event.ON_START]
 * - there is no specific backpressure on the queue, by default it has limit of 8, if the limit is reached,
 * it's overwriting value
 * - in case of limit 0, it acts just simply as a bus
 */
open class MutableLiveQueue<T> private constructor(
    initialValue: T? = null,
    private val capacity: Int,
    private val scheduler: ITaskScheduler,
    //just nullable vs nonnullable ctor
    @Suppress("UNUSED_PARAMETER") ignore: Any? = null
) : LiveQueue<T> {

    constructor(initialValue: T, capacity: Int, scheduler: ITaskScheduler = MainThreadTaskScheduler.Instance) :
            this(initialValue, capacity, scheduler, null)

    constructor(capacity: Int, scheduler: ITaskScheduler = MainThreadTaskScheduler.Instance) :
            this(null, capacity, scheduler, null)

    constructor(scheduler: ITaskScheduler = MainThreadTaskScheduler.Instance) : this(null, 8, scheduler = scheduler)

    var isActive = false; private set
    override val hasObserver: Boolean get() = observerWrapper != null

    private val lock = Any()
    private val queue = Collections.synchronizedList(LinkedList<T>())
    private var observerWrapper: ObserverWrapper? = null
    private val atomicBoolean = AtomicBoolean(false)

    init {
        if (initialValue != null) {
            require(capacity > 0) { "Initial value with capacity = 0 is not allowed" }
            enqueueOrDispatch(initialValue) { _, _ ->
                throw IllegalStateException(
                    "Dispatch in this time shouldn't ever happened," +
                            "as it's ctor call and there shouldn't be any observer attached"
                )
            }
        }
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
        synchronized(lock) {
            observerWrapper?.clear()
            observerWrapper = ObserverWrapper(lifecycleOwner, observer)
            dispatchEvents()
        }
    }

    override fun removeObserver() {
        if (observerWrapper != null) {
            synchronized(lock) {
                observerWrapper?.clear()
                observerWrapper = null
            }
        }
    }

    private fun onHandleLifecycleEvent(@Suppress("UNUSED_PARAMETER") source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                isActive = true
                dispatchEvents()
            }
            Lifecycle.Event.ON_STOP -> {
                isActive = false
            }
            Lifecycle.Event.ON_DESTROY -> {
                isActive = false
                removeObserver()
            }
        }
    }

    private fun dispatchEvents() {
        val wrapper = synchronized(lock) {
            val wrapper = observerWrapper
            if (queue.isEmpty() || wrapper == null) {
                return
            }
            wrapper
        } ?: return
        //queue is synchronized, we want to keep it outside main lock,
        //it might block main thread
        val (lifecycle, observer) = wrapper
        queue.iterator().apply {
            while (hasNext() && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                observer.invoke(next())
                try {
                    remove()
                } catch (e: ConcurrentModificationException) {
                    throw ConcurrentModificationException(
                        "Update queue during dispatching. " +
                                "Post/Set Value during dispatching data to observer is not allowed!", e
                    )
                }
            }
        }
    }

    /**
     * Set value
     */
    @MainThread
    fun setValue(item: T) {
        check(scheduler.isMainThread) {
            "setValue is allowed to be executed only in main thread, it's:${Thread.currentThread()}"
        }
        enqueueOrDispatch(item) { observer, v ->
            observer.invoke(v)
        }
    }

    /**
     * Post value
     */
    @AnyThread
    fun postValue(item: T) {
        enqueueOrDispatch(item) { observer, v ->
            scheduler.run { observer.invoke(v) }
        }
    }

    /**
     * Set a value on main thread or post
     *
     * @param item
     */
    @SuppressLint("WrongThread")
    @AnyThread
    fun emit(item: T) {
        if (scheduler.isMainThread) setValue(item) else postValue(item)
    }

    private inline fun enqueueOrDispatch(item: T, dispatch: ((T) -> Unit, T) -> Unit) {
        val observer: (T) -> Unit
        synchronized(lock) {
            val wrapper = observerWrapper
            if (!isActive || wrapper == null) {
                if (capacity != 0) {
                    while (queue.size >= capacity) {
                        queue.removeAt(0)
                    }
                    queue.add(item)
                }
                return
            }
            observer = wrapper.observer
        }
        //intentionally even if it's posted, scheduler might execute it right away, which might be exactly same as setValue
        withSingleDispatchingCheck {
            dispatch(observer, item)
        }
    }

    private inline fun withSingleDispatchingCheck(block: () -> Unit) {
        if (!atomicBoolean.compareAndSet(false, true)) {
            throw IllegalStateException(
                "Dispatching value during dispatching is dangerous as it might lead to infinite loops." +
                        "Calling setValue in your observable is disallowed!"
            )
        }
        //dispatching during dispatching might lead to infinite loop
        block()
        atomicBoolean.compareAndSet(true, false)
    }

    private inner class ObserverWrapper(
        val lifecycleOwner: LifecycleOwner,
        val observer: (T) -> Unit
    ) : LifecycleObserver {
        init {
            lifecycleOwner.lifecycle.addObserver(this)
        }

        @Suppress("unused")
        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun onAny(source: LifecycleOwner, event: Lifecycle.Event) {
            onHandleLifecycleEvent(source, event)
        }

        fun clear() {
            lifecycleOwner.lifecycle.removeObserver(this)
        }

        operator fun component1() = lifecycleOwner.lifecycle
        operator fun component2() = observer
    }

    companion object {
        /**
         * Create [MutableLiveQueue] for navigation events.
         * That means having [capacity] = 1
         *
         * @param T
         */
        fun <T> navigationQueue() = MutableLiveQueue<T>(capacity = 1)
    }
}
