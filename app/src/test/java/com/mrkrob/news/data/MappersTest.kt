package com.mrkrob.news.data

import com.mrkrob.news.data.remote.model.ArticleDto
import com.mrkrob.news.data.remote.model.SourceDto
import com.mrkrob.news.domain.model.NewsCategory
import com.mrkrob.news.domain.model.NewsFeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class MappersTest {

    private val article = ArticleDto(
        source = SourceDto(name = "The Verge"),
        title = "Something happened - The Verge",
        description = "  Details  ",
        url = "https://www.theverge.com/article",
        urlToImage = "http://cdn.theverge.com/image.jpg",
        publishedAt = "2026-10-15T09:30:00Z",
    )

    @Test
    fun `maps an article`() {
        val entity = article.toEntityOrNull(feed = "top", position = 3)!!

        assertEquals("top", entity.feed)
        assertEquals(3, entity.position)
        assertEquals("Something happened", entity.title)
        assertEquals("Details", entity.description)
        assertEquals("https://cdn.theverge.com/image.jpg", entity.imageUrl)
        assertEquals("The Verge", entity.sourceName)
        assertEquals(Instant.parse("2026-10-15T09:30:00Z").toEpochMilli(), entity.publishedAt)
    }

    @Test
    fun `skips removed articles`() {
        val removed = ArticleDto(
            source = SourceDto(name = "[Removed]"),
            title = "[Removed]",
            description = "[Removed]",
            url = "https://removed.com",
        )

        assertNull(removed.toEntityOrNull(feed = "top", position = 0))
    }

    @Test
    fun `skips articles without a title or url`() {
        assertNull(article.copy(title = " ").toEntityOrNull(feed = "top", position = 0))
        assertNull(article.copy(url = null).toEntityOrNull(feed = "top", position = 0))
    }

    @Test
    fun `keeps an article with an unparsable date`() {
        val entity = article.copy(publishedAt = "yesterday").toEntityOrNull(feed = "top", position = 0)!!

        assertNull(entity.publishedAt)
    }

    @Test
    fun `cache keys are distinct per feed and case-insensitive for search`() {
        assertEquals(NewsFeed.Search("Kotlin").cacheKey, NewsFeed.Search("kotlin").cacheKey)
        assertNotEquals(NewsFeed.TopHeadlines.cacheKey, NewsFeed.Category(NewsCategory.GENERAL).cacheKey)
        assertNotEquals(
            NewsFeed.Category(NewsCategory.SPORTS).cacheKey,
            NewsFeed.Category(NewsCategory.SCIENCE).cacheKey,
        )
    }
}
