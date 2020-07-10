package com.scurab.android.spotifyrc.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData


/**
 * zipWith 2nd [LiveData] streams into 1
 *
 * @param T
 * @param A
 * @param B
 * @param other
 * @param onChange
 * @return
 */
fun <T, A, B> LiveData<A>.zipWith(other: LiveData<B>, onChange: (A, B) -> T): LiveData<T> {
    //bitmask
    var emitted = 0
    val result = MediatorLiveData<T>()

    val merge = {
        val source1Value = this.value
        val source2Value = other.value

        if (emitted == 3) {
            //`as A` in this generic means however it's defined by receiver, it doesn't mean `as? A` or `as A?`
            result.value = onChange.invoke(source1Value as A, source2Value as B)
        }
    }

    result.addSource(this) { emitted = emitted or 1; merge.invoke() }
    result.addSource(other) { emitted = emitted or 2; merge.invoke() }

    return result
}