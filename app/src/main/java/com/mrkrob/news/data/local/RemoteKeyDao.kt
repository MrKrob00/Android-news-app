package com.mrkrob.news.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE feed = :feed")
    suspend fun get(feed: String): RemoteKeyEntity?

    @Upsert
    suspend fun upsert(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE feed LIKE :prefix || '%' AND feed != :keep")
    suspend fun deleteFeedsWithPrefix(prefix: String, keep: String)
}
