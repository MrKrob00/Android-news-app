package com.mrkrob.news.domain.model

import java.time.Instant

data class Article(
    val id: Long,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val sourceName: String,
    val publishedAt: Instant?,
)
