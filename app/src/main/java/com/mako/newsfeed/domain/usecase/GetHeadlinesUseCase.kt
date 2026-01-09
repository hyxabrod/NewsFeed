package com.mako.newsfeed.domain.usecase

import com.mako.newsfeed.core.ListUseCaseWithArgs
import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.repository.NewsRepository
import com.mako.newsfeed.presentation.model.NewsArticlePresentationEntity
import javax.inject.Inject

class GetHeadlinesUseCase
@Inject constructor(
    private val repository: NewsRepository,
    private val networkMonitor: NetworkMonitor
) : ListUseCaseWithArgs<GetHeadlinesUseCaseArgs> {

    override suspend fun invoke(args: GetHeadlinesUseCaseArgs): List<NewsArticlePresentationEntity> {
        val page = args.page
        if (networkMonitor.isConnected()) {
            if (page == 1) {
                repository.clearHeadlines()
            }
            val remoteArticles = repository.getHeadlines(page, args.isTopNews)
            return remoteArticles.map { it.toPresentation() }
        } else {
            return repository.getHeadlinesFromDb().map { it.toPresentation() }
        }
    }
}

data class GetHeadlinesUseCaseArgs(val page: Int, val isTopNews: Boolean) : ListUseCaseWithArgs.Args()
