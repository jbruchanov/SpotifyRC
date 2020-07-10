package com.scurab.android.spotifyrc.property

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for nicer shared prefs backed class
 *
 * @param T
 * @property sharedPrefs
 * @property key - explicit key name, if null [KProperty.name] is used
 * @property getter
 * @property setter
 */
class SharedPrefsProperty<T> internal constructor(
    private val sharedPrefs: SharedPreferences,
    private val key: String? = null,
    private val getter: (SharedPreferences.(String) -> T),
    private val setter: (SharedPreferences.Editor.(key: String, value: T) -> Unit)
) : ReadWriteProperty<Any, T>, ReadOnlyProperty<Any, T> {

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T =
        getter(sharedPrefs, property.name)

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        sharedPrefs.edit { setter(this, property.name, value) }

    companion object {

        fun string(sharedPrefs: SharedPreferences, defValue: String, key: String? = null): SharedPrefsProperty<String> {
            @Suppress("UNCHECKED_CAST")
            //code can be exactly same and as this defValue is String, it won't ever be null
            //so we can easily use the nullable version hidden by not-nullable
            return string(sharedPrefs, defValue as String?, key) as SharedPrefsProperty<String>
        }

        @JvmName("nullableStringSharedPref")
        fun string(sharedPrefs: SharedPreferences, defValue: String? = null, key: String? = null): SharedPrefsProperty<String?> {
            return SharedPrefsProperty(sharedPrefs,
                getter = { k -> getString(key ?: k, defValue) ?: defValue },
                setter = { k, v -> putString(key ?: k, v) })
        }

        fun int(sharedPrefs: SharedPreferences, defValue: Int, key: String? = null): SharedPrefsProperty<Int> {
            return SharedPrefsProperty(sharedPrefs,
                getter = { k -> getInt(key ?: k, defValue) },
                setter = { k, v -> putInt(key ?: k, v) })
        }

        fun long(sharedPrefs: SharedPreferences, defValue: Long, key: String? = null): SharedPrefsProperty<Long> {
            return SharedPrefsProperty(sharedPrefs,
                getter = { k -> getLong(key ?: k, defValue) },
                setter = { k, v -> putLong(key ?: k, v) })
        }
    }
}