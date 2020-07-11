package com.scurab.android.spotify.api.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class InnerOkHttpException(ex: Throwable) : IOException(ex)

/**
 * Help OkHttp interceptor to handle gracefully non[IOException] inside the okhttp pipeline when using coroutines.
 * [Related issue](https://github.com/square/okhttp/issues/5151)
 */
object NoInnerCrashingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (io: IOException) {
            throw io
        } catch (t: Throwable) {
            throw InnerOkHttpException(t)
        }
    }
}