package com.mrkrob.news.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (apiKey.isBlank()) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("X-Api-Key", apiKey).build())
    }
}
