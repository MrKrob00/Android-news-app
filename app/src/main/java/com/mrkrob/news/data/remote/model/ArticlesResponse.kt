package com.mrkrob.news.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponse(
    val totalResults: Int = 0,
    val articles: List<ArticleDto> = emptyList(),
)

@Serializable
data class ArticleDto(
    val source: SourceDto? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
)

@Serializable
data class SourceDto(
    val name: String? = null,
)

@Serializable
data class ErrorResponse(
    val code: String? = null,
    val message: String? = null,
)
