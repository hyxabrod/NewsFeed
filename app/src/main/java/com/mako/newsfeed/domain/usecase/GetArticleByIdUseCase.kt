package com.mako.newsfeed.domain.usecase

import com.mako.newsfeed.core.UseCaseWithArgs
import com.mako.newsfeed.domain.repository.NewsRepository
import com.mako.newsfeed.presentation.model.ArticlePresentationEntity
import javax.inject.Inject

class GetArticleByIdUseCase
@Inject constructor(
    private val repository: NewsRepository
) : UseCaseWithArgs<GetArticleByIdUseCaseArgs> {

    override suspend fun invoke(args: GetArticleByIdUseCaseArgs): ArticlePresentationEntity {
        val articleEntity = repository.getNewsArticleById(args.uuid)

        return articleEntity.toPresentation()
    }
}

data class GetArticleByIdUseCaseArgs(val uuid: String) : UseCaseWithArgs.Args()
