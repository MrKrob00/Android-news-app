package com.mrkrob.news.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "articles",
    indices = [Index(value = ["feed", "url"], unique = true)],
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val feed: String,
    val position: Int,
    val url: String,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "source_name") val sourceName: String,
    @ColumnInfo(name = "published_at") val publishedAt: Long?,
)
