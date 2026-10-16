package com.mrkrob.news.testing

import com.mrkrob.news.data.remote.NewsApi
import com.mrkrob.news.data.remote.model.ArticleDto
import com.mrkrob.news.data.remote.model.ArticlesResponse
import com.mrkrob.news.data.remote.model.SourceDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class FakeNewsApi : NewsApi {

    data class Request(
        val endpoint: String,
        val category: String? = null,
        val query: String? = null,
        val page: Int,
    )

    val requests = mutableListOf<Request>()

    var respond: (Request) -> ArticlesResponse = { ArticlesResponse() }

    override suspend fun topHeadlines(
        country: String,
        category: String?,
        page: Int,
        pageSize: Int,
    ): ArticlesResponse = handle(Request(TOP_HEADLINES, category = category, page = page))

    override suspend fun everything(
        query: String,
        sortBy: String,
        page: Int,
        pageSize: Int,
    ): ArticlesResponse = handle(Request(EVERYTHING, query = query, page = page))

    private fun handle(request: Request): ArticlesResponse {
        requests += request
        return respond(request)
    }

    companion object {
        const val TOP_HEADLINES = "top-headlines"
        const val EVERYTHING = "everything"
    }
}

fun articleDto(number: Int) = ArticleDto(
    source = SourceDto(name = "Source"),
    title = "Title $number",
    url = "https://example.com/$number",
    publishedAt = "2026-10-15T09:30:00Z",
)

fun articleDtos(numbers: IntRange) = numbers.map(::articleDto)

fun httpError(code: Int, body: String) = HttpException(
    Response.error<ArticlesResponse>(code, body.toResponseBody("application/json".toMediaType())),
)
