package com.mrkrob.news.data.remote

import com.mrkrob.news.data.remote.model.ArticlesResponse
import com.mrkrob.news.domain.model.NewsCategory
import com.mrkrob.news.domain.model.NewsException
import com.mrkrob.news.domain.model.NewsFeed
import com.mrkrob.news.testing.FakeNewsApi
import com.mrkrob.news.testing.FakeNewsApi.Companion.EVERYTHING
import com.mrkrob.news.testing.FakeNewsApi.Companion.TOP_HEADLINES
import com.mrkrob.news.testing.articleDtos
import com.mrkrob.news.testing.httpError
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class NewsRemoteDataSourceTest {

    private val api = FakeNewsApi()
    private val dataSource = NewsRemoteDataSource(api, Json { ignoreUnknownKeys = true })

    @Test
    fun `requests the endpoint of each feed`() = runTest {
        dataSource.loadPage(NewsFeed.TopHeadlines, page = 1, pageSize = 20)
        dataSource.loadPage(NewsFeed.Category(NewsCategory.SPORTS), page = 2, pageSize = 20)
        dataSource.loadPage(NewsFeed.Search("kotlin"), page = 3, pageSize = 20)

        assertEquals(
            listOf(
                FakeNewsApi.Request(TOP_HEADLINES, page = 1),
                FakeNewsApi.Request(TOP_HEADLINES, category = "sports", page = 2),
                FakeNewsApi.Request(EVERYTHING, query = "kotlin", page = 3),
            ),
            api.requests,
        )
    }

    @Test
    fun `detects the last page from the total number of results`() = runTest {
        api.respond = { ArticlesResponse(totalResults = 25, articles = articleDtos(1..20)) }

        assertFalse(dataSource.loadPage(NewsFeed.TopHeadlines, page = 1, pageSize = 20).isLastPage)
        assertTrue(dataSource.loadPage(NewsFeed.TopHeadlines, page = 2, pageSize = 20).isLastPage)
    }

    @Test
    fun `an empty page is the last one`() = runTest {
        api.respond = { ArticlesResponse(totalResults = 1000, articles = emptyList()) }

        assertTrue(dataSource.loadPage(NewsFeed.TopHeadlines, page = 1, pageSize = 20).isLastPage)
    }

    @Test
    fun `the results limit of the free plan ends the pagination`() = runTest {
        api.respond = { throw httpError(426, errorBody("maximumResultsReached")) }

        val page = dataSource.loadPage(NewsFeed.Search("kotlin"), page = 6, pageSize = 20)

        assertTrue(page.isLastPage)
        assertTrue(page.articles.isEmpty())
    }

    @Test
    fun `maps key errors`() = runTest {
        for (code in listOf("apiKeyMissing", "apiKeyInvalid", "apiKeyDisabled", "apiKeyExhausted")) {
            api.respond = { throw httpError(401, errorBody(code)) }

            assertTrue(code, loadError() is NewsException.InvalidApiKey)
        }
    }

    @Test
    fun `maps the rate limit error`() = runTest {
        api.respond = { throw httpError(429, errorBody("rateLimited")) }

        assertTrue(loadError() is NewsException.RateLimited)
    }

    @Test
    fun `keeps the message of other api errors`() = runTest {
        api.respond = { throw httpError(500, errorBody("unexpectedError", message = "Server is down")) }

        val error = loadError()

        assertTrue(error is NewsException.Api)
        assertEquals("Server is down", error?.message)
    }

    @Test
    fun `an error without a body is an api error`() = runTest {
        api.respond = { throw httpError(502, "Bad gateway") }

        assertTrue(loadError() is NewsException.Api)
    }

    @Test
    fun `maps network failures`() = runTest {
        api.respond = { throw IOException("Unable to resolve host") }

        assertTrue(loadError() is NewsException.NoConnection)
    }

    private suspend fun loadError(): Throwable? =
        runCatching { dataSource.loadPage(NewsFeed.TopHeadlines, page = 1, pageSize = 20) }.exceptionOrNull()

    private fun errorBody(code: String, message: String = "Message") =
        """{"status":"error","code":"$code","message":"$message"}"""
}
