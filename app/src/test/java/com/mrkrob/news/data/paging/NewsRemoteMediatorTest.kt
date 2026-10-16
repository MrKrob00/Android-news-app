package com.mrkrob.news.data.paging

import android.app.Application
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.InitializeAction
import androidx.paging.RemoteMediator.MediatorResult
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mrkrob.news.data.cacheKey
import com.mrkrob.news.data.local.ArticleEntity
import com.mrkrob.news.data.local.NewsDatabase
import com.mrkrob.news.data.remote.NewsRemoteDataSource
import com.mrkrob.news.data.remote.model.ArticlesResponse
import com.mrkrob.news.domain.model.NewsCategory
import com.mrkrob.news.domain.model.NewsException
import com.mrkrob.news.domain.model.NewsFeed
import com.mrkrob.news.testing.FakeNewsApi
import com.mrkrob.news.testing.articleDtos
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class NewsRemoteMediatorTest {

    private val api = FakeNewsApi()
    private val dataSource = NewsRemoteDataSource(api, Json { ignoreUnknownKeys = true })
    private lateinit var database: NewsDatabase
    private var now = 0L

    private val pagingState = PagingState<Int, ArticleEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = NewsRemoteMediator.PAGE_SIZE),
        leadingPlaceholderCount = 0,
    )

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `refresh caches the first page and the key of the next one`() = runTest {
        api.respond = { ArticlesResponse(totalResults = 30, articles = articleDtos(1..20)) }

        val result = mediator(NewsFeed.TopHeadlines).load(LoadType.REFRESH, pagingState)

        assertEquals(false, (result as MediatorResult.Success).endOfPaginationReached)
        assertEquals(2, database.remoteKeyDao().get(NewsFeed.TopHeadlines.cacheKey)?.nextPage)
        assertEquals((1..20).map { "Title $it" }, cachedTitles(NewsFeed.TopHeadlines))
    }

    @Test
    fun `append continues from the cached key until the last page`() = runTest {
        api.respond = { request ->
            val articles = if (request.page == 1) articleDtos(1..20) else articleDtos(21..30)
            ArticlesResponse(totalResults = 30, articles = articles)
        }
        val mediator = mediator(NewsFeed.TopHeadlines)

        mediator.load(LoadType.REFRESH, pagingState)
        val result = mediator.load(LoadType.APPEND, pagingState)

        assertEquals(true, (result as MediatorResult.Success).endOfPaginationReached)
        assertEquals(listOf(1, 2), api.requests.map { it.page })
        assertNull(database.remoteKeyDao().get(NewsFeed.TopHeadlines.cacheKey)?.nextPage)
        assertEquals((1..30).map { "Title $it" }, cachedTitles(NewsFeed.TopHeadlines))
    }

    @Test
    fun `an article repeated on the next page is cached once`() = runTest {
        api.respond = { request ->
            val articles = if (request.page == 1) articleDtos(1..20) else articleDtos(20..39)
            ArticlesResponse(totalResults = 40, articles = articles)
        }
        val mediator = mediator(NewsFeed.TopHeadlines)

        mediator.load(LoadType.REFRESH, pagingState)
        mediator.load(LoadType.APPEND, pagingState)

        assertEquals((1..39).map { "Title $it" }, cachedTitles(NewsFeed.TopHeadlines))
    }

    @Test
    fun `a failed refresh keeps the cached articles`() = runTest {
        api.respond = { ArticlesResponse(totalResults = 20, articles = articleDtos(1..20)) }
        mediator(NewsFeed.TopHeadlines).load(LoadType.REFRESH, pagingState)
        api.respond = { throw IOException("No network") }

        val result = mediator(NewsFeed.TopHeadlines).load(LoadType.REFRESH, pagingState)

        assertTrue((result as MediatorResult.Error).throwable is NewsException.NoConnection)
        assertEquals(20, cachedTitles(NewsFeed.TopHeadlines).size)
    }

    @Test
    fun `refresh replaces the cached articles`() = runTest {
        api.respond = { ArticlesResponse(totalResults = 20, articles = articleDtos(1..20)) }
        mediator(NewsFeed.TopHeadlines).load(LoadType.REFRESH, pagingState)
        api.respond = { ArticlesResponse(totalResults = 5, articles = articleDtos(101..105)) }

        mediator(NewsFeed.TopHeadlines).load(LoadType.REFRESH, pagingState)

        assertEquals((101..105).map { "Title $it" }, cachedTitles(NewsFeed.TopHeadlines))
    }

    @Test
    fun `the cache is reused while it is fresh`() = runTest {
        val mediator = mediator(NewsFeed.TopHeadlines)
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())

        api.respond = { ArticlesResponse(totalResults = 20, articles = articleDtos(1..20)) }
        mediator.load(LoadType.REFRESH, pagingState)
        now += TimeUnit.MINUTES.toMillis(10)
        assertEquals(InitializeAction.SKIP_INITIAL_REFRESH, mediator.initialize())

        now += TimeUnit.MINUTES.toMillis(30)
        assertEquals(InitializeAction.LAUNCH_INITIAL_REFRESH, mediator.initialize())
    }

    @Test
    fun `only the latest search is cached`() = runTest {
        api.respond = { ArticlesResponse(totalResults = 20, articles = articleDtos(1..20)) }
        val oldSearch = NewsFeed.Search("kotlin")
        val category = NewsFeed.Category(NewsCategory.TECHNOLOGY)
        mediator(oldSearch).load(LoadType.REFRESH, pagingState)
        mediator(category).load(LoadType.REFRESH, pagingState)

        mediator(NewsFeed.Search("android")).load(LoadType.REFRESH, pagingState)

        assertTrue(cachedTitles(oldSearch).isEmpty())
        assertNull(database.remoteKeyDao().get(oldSearch.cacheKey))
        assertEquals(20, cachedTitles(category).size)
        assertEquals(20, cachedTitles(NewsFeed.Search("android")).size)
    }

    private fun mediator(feed: NewsFeed) = NewsRemoteMediator(feed, database, dataSource, now = { now })

    private suspend fun cachedTitles(feed: NewsFeed): List<String> {
        val result = database.articleDao().pagingSource(feed.cacheKey).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
        )
        return (result as PagingSource.LoadResult.Page).data.map { it.title }
    }
}
