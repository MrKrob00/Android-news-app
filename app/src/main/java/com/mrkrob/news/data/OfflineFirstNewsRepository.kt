package com.mrkrob.news.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mrkrob.news.data.local.ArticleEntity
import com.mrkrob.news.data.local.NewsDatabase
import com.mrkrob.news.data.paging.NewsRemoteMediator
import com.mrkrob.news.data.remote.NewsRemoteDataSource
import com.mrkrob.news.domain.NewsRepository
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineFirstNewsRepository @Inject constructor(
    private val database: NewsDatabase,
    private val remoteDataSource: NewsRemoteDataSource,
) : NewsRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun articles(feed: NewsFeed): Flow<PagingData<Article>> {
        val feedKey = feed.cacheKey
        return Pager(
            config = PagingConfig(
                pageSize = NewsRemoteMediator.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            remoteMediator = NewsRemoteMediator(feed, database, remoteDataSource),
            pagingSourceFactory = { database.articleDao().pagingSource(feedKey) },
        ).flow.map { pagingData -> pagingData.map(ArticleEntity::toDomain) }
    }
}
