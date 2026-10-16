package com.mrkrob.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ArticleEntity::class, RemoteKeyEntity::class],
    version = 1,
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    abstract fun remoteKeyDao(): RemoteKeyDao
}
