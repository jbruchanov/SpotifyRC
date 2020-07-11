package com.scurab.android.spotify.api.okhttp

import okhttp3.Interceptor
import okhttp3.Response

interface IHasAuthToken {
    val accessToken: String
}

class AuthInterceptor(private val tokenProvider: IHasAuthToken) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${tokenProvider.accessToken}")
            .build()

        return chain.proceed(request)
    }
}