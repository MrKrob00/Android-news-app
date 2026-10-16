package com.mrkrob.news.data

import com.mrkrob.news.data.local.ArticleEntity
import com.mrkrob.news.data.remote.model.ArticleDto
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsFeed
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

internal const val SEARCH_CACHE_KEY_PREFIX = "search:"

internal val NewsFeed.cacheKey: String
    get() = when (this) {
        NewsFeed.TopHeadlines -> "top"
        is NewsFeed.Category -> "category:${category.apiValue}"
        is NewsFeed.Search -> SEARCH_CACHE_KEY_PREFIX + query.lowercase()
    }

internal fun ArticleDto.toEntityOrNull(feed: String, position: Int): ArticleEntity? {
    val url = url?.takeUnless { it.isBlank() || it == REMOVED_URL } ?: return null
    val sourceName = source?.name?.trim().orEmpty()
    val title = title?.trim()
        ?.takeUnless { it.isEmpty() || it == REMOVED }
        ?.let { if (sourceName.isEmpty()) it else it.removeSuffix(" - $sourceName") }
        ?: return null
    return ArticleEntity(
        feed = feed,
        position = position,
        url = url,
        title = title,
        description = description?.trim()?.takeUnless { it.isEmpty() || it == REMOVED },
        imageUrl = urlToImage?.trim()?.takeUnless { it.isEmpty() }?.replaceFirst("http://", "https://"),
        sourceName = sourceName,
        publishedAt = publishedAt?.let(::parseInstantOrNull)?.toEpochMilli(),
    )
}

internal fun ArticleEntity.toDomain() = Article(
    id = id,
    title = title,
    description = description,
    url = url,
    imageUrl = imageUrl,
    sourceName = sourceName,
    publishedAt = publishedAt?.let(Instant::ofEpochMilli),
)

private fun parseInstantOrNull(value: String): Instant? =
    try {
        OffsetDateTime.parse(value).toInstant()
    } catch (e: DateTimeParseException) {
        null
    }

private const val REMOVED = "[Removed]"
private const val REMOVED_URL = "https://removed.com"
