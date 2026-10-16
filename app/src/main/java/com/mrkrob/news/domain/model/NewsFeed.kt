package com.mrkrob.news.domain.model

sealed interface NewsFeed {

    data object TopHeadlines : NewsFeed

    data class Search(val query: String) : NewsFeed

    data class Category(val category: NewsCategory) : NewsFeed
}
