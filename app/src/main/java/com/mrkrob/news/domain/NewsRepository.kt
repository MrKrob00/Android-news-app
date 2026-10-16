package com.mrkrob.news.domain

import androidx.paging.PagingData
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsFeed
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    fun articles(feed: NewsFeed): Flow<PagingData<Article>>
}
