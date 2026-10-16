package com.mrkrob.news.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mrkrob.news.data.SEARCH_CACHE_KEY_PREFIX
import com.mrkrob.news.data.cacheKey
import com.mrkrob.news.data.local.ArticleEntity
import com.mrkrob.news.data.local.NewsDatabase
import com.mrkrob.news.data.local.RemoteKeyEntity
import com.mrkrob.news.data.remote.NewsPage
import com.mrkrob.news.data.remote.NewsRemoteDataSource
import com.mrkrob.news.data.toEntityOrNull
import com.mrkrob.news.domain.model.NewsException
import com.mrkrob.news.domain.model.NewsFeed
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class NewsRemoteMediator(
    private val feed: NewsFeed,
    private val database: NewsDatabase,
    private val remoteDataSource: NewsRemoteDataSource,
    private val now: () -> Long = System::currentTimeMillis,
) : RemoteMediator<Int, ArticleEntity>() {

    private val feedKey = feed.cacheKey
    private val articleDao = database.articleDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        val refreshedAt = remoteKeyDao.get(feedKey)?.refreshedAt
        val isFresh = refreshedAt != null && now() - refreshedAt < CACHE_TIMEOUT_MILLIS
        return if (isFresh) InitializeAction.SKIP_INITIAL_REFRESH else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>,
    ): MediatorResult {
        val remoteKey = remoteKeyDao.get(feedKey)
        val page = when (loadType) {
            LoadType.REFRESH -> FIRST_PAGE
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> remoteKey?.nextPage
                ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
        }

        val newsPage = try {
            remoteDataSource.loadPage(feed, page, PAGE_SIZE)
        } catch (e: NewsException) {
            return MediatorResult.Error(e)
        }

        val isRefresh = loadType == LoadType.REFRESH
        database.withTransaction {
            if (isRefresh) clearFeed()
            remoteKeyDao.upsert(
                RemoteKeyEntity(
                    feed = feedKey,
                    nextPage = if (newsPage.isLastPage) null else page + 1,
                    refreshedAt = if (isRefresh) now() else remoteKey?.refreshedAt ?: now(),
                ),
            )
            articleDao.insertAll(newsPage.toEntities(page))
        }
        return MediatorResult.Success(endOfPaginationReached = newsPage.isLastPage)
    }

    private suspend fun clearFeed() {
        articleDao.deleteFeed(feedKey)
        if (feed is NewsFeed.Search) {
            articleDao.deleteFeedsWithPrefix(SEARCH_CACHE_KEY_PREFIX, keep = feedKey)
            remoteKeyDao.deleteFeedsWithPrefix(SEARCH_CACHE_KEY_PREFIX, keep = feedKey)
        }
    }

    private fun NewsPage.toEntities(page: Int): List<ArticleEntity> {
        val firstPosition = (page - 1) * PAGE_SIZE
        return articles.mapIndexedNotNull { index, article ->
            article.toEntityOrNull(feedKey, position = firstPosition + index)
        }
    }

    companion object {
        const val PAGE_SIZE = 20
        private const val FIRST_PAGE = 1
        private val CACHE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(30)
    }
}
