package com.mrkrob.news.data.remote

import com.mrkrob.news.data.remote.model.ArticleDto
import com.mrkrob.news.data.remote.model.ArticlesResponse
import com.mrkrob.news.data.remote.model.ErrorResponse
import com.mrkrob.news.domain.model.NewsException
import com.mrkrob.news.domain.model.NewsFeed
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class NewsRemoteDataSource @Inject constructor(
    private val api: NewsApi,
    private val json: Json,
) {

    suspend fun loadPage(feed: NewsFeed, page: Int, pageSize: Int): NewsPage {
        val response = try {
            request(feed, page, pageSize)
        } catch (e: HttpException) {
            val error = e.errorResponse()
            if (error?.code == ERROR_MAXIMUM_RESULTS_REACHED) {
                return NewsPage(articles = emptyList(), isLastPage = true)
            }
            throw error.toNewsException(fallbackMessage = e.message())
        } catch (e: IOException) {
            throw NewsException.NoConnection(e)
        } catch (e: SerializationException) {
            throw NewsException.Api(e.message)
        }
        return NewsPage(
            articles = response.articles,
            isLastPage = response.articles.isEmpty() || page * pageSize >= response.totalResults,
        )
    }

    private suspend fun request(feed: NewsFeed, page: Int, pageSize: Int): ArticlesResponse =
        when (feed) {
            NewsFeed.TopHeadlines -> api.topHeadlines(COUNTRY, category = null, page, pageSize)
            is NewsFeed.Category -> api.topHeadlines(COUNTRY, feed.category.apiValue, page, pageSize)
            is NewsFeed.Search -> api.everything(feed.query, SORT_BY_PUBLISHED_AT, page, pageSize)
        }

    private fun HttpException.errorResponse(): ErrorResponse? {
        val body = response()?.errorBody()?.string() ?: return null
        return runCatching { json.decodeFromString<ErrorResponse>(body) }.getOrNull()
    }

    private fun ErrorResponse?.toNewsException(fallbackMessage: String): NewsException {
        val message = this?.message ?: fallbackMessage
        return when (this?.code) {
            "apiKeyMissing", "apiKeyInvalid", "apiKeyDisabled", "apiKeyExhausted" ->
                NewsException.InvalidApiKey(message)
            "rateLimited" -> NewsException.RateLimited(message)
            else -> NewsException.Api(message)
        }
    }

    private companion object {
        const val COUNTRY = "us"
        const val SORT_BY_PUBLISHED_AT = "publishedAt"
        const val ERROR_MAXIMUM_RESULTS_REACHED = "maximumResultsReached"
    }
}

data class NewsPage(
    val articles: List<ArticleDto>,
    val isLastPage: Boolean,
)
