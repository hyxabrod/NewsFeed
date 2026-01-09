package com.mako.newsfeed.di

import com.mako.newsfeed.data.repository.NewsRepositoryImpl
import com.mako.newsfeed.domain.repository.NewsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNewsRepository(newsRepositoryImpl: NewsRepositoryImpl): NewsRepository
}
