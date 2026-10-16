package com.mrkrob.news.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles WHERE feed = :feed ORDER BY position")
    fun pagingSource(feed: String): PagingSource<Int, ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE feed = :feed")
    suspend fun deleteFeed(feed: String)

    @Query("DELETE FROM articles WHERE feed LIKE :prefix || '%' AND feed != :keep")
    suspend fun deleteFeedsWithPrefix(prefix: String, keep: String)
}
