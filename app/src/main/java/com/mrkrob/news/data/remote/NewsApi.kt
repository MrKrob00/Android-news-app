package com.mrkrob.news.data.remote

import com.mrkrob.news.data.remote.model.ArticlesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Query("country") country: String,
        @Query("category") category: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponse

    @GET("v2/everything")
    suspend fun everything(
        @Query("q") query: String,
        @Query("sortBy") sortBy: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): ArticlesResponse
}
