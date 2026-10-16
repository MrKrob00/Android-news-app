package com.mrkrob.news.di

import com.mrkrob.news.data.OfflineFirstNewsRepository
import com.mrkrob.news.domain.NewsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindNewsRepository(repository: OfflineFirstNewsRepository): NewsRepository
}
