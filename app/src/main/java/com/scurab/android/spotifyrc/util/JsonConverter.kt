package com.scurab.android.spotifyrc.util

import com.google.gson.Gson
import com.scurab.android.spotifyrc.commands.Command
import com.scurab.android.spotifyrc.model.IHasBackingMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonConverter @Inject constructor() {
    private val gson = Gson()

    fun toJson(item: IHasBackingMap): String {
        return gson.toJson(item.data)
    }

    fun toJson(item: Any): String {
        return gson.toJson(item)
    }

    fun <T : IHasBackingMap> fromJson(json: String): T {
        val map = gson.fromJson(json, Map::class.java)
        val className = map["name"] as String
        val instance = Class.forName(className).newInstance() as T
        instance.data.putAll(map as Map<out String, Any>)
        return instance
    }

    fun <T> fromJson(json: String, klass: Class<T>): T {
        return gson.fromJson(json, klass)
    }
}