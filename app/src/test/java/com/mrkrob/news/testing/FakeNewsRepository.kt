package com.mrkrob.news.testing

import androidx.paging.PagingData
import com.mrkrob.news.domain.NewsRepository
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsFeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeNewsRepository : NewsRepository {

    val requestedFeeds = mutableListOf<NewsFeed>()

    override fun articles(feed: NewsFeed): Flow<PagingData<Article>> {
        requestedFeeds += feed
        return flowOf(PagingData.empty())
    }
}
