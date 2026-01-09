package com.mako.newsfeed.di

import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.repository.NewsRepository
import com.mako.newsfeed.domain.usecase.GetHeadlinesUseCase
import com.mako.newsfeed.domain.usecase.SaveArticleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetHeadlinesUseCase(
            repository: NewsRepository,
            networkMonitor: NetworkMonitor
    ): GetHeadlinesUseCase {
        return GetHeadlinesUseCase(repository, networkMonitor)
    }

    @Provides
    fun provideSaveArticleUseCase(repository: NewsRepository): SaveArticleUseCase {
        return SaveArticleUseCase(repository)
    }
}
