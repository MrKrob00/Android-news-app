package com.mrkrob.news.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val feed: String,
    @ColumnInfo(name = "next_page") val nextPage: Int?,
    @ColumnInfo(name = "refreshed_at") val refreshedAt: Long,
)
