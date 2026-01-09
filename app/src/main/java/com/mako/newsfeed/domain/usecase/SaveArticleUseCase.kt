package com.mako.newsfeed.domain.usecase

import com.mako.newsfeed.core.VoidUseCaseWithArgs
import com.mako.newsfeed.domain.entity.ArticleDomainEntity
import com.mako.newsfeed.domain.repository.NewsRepository
import javax.inject.Inject

class SaveArticleUseCase @Inject constructor(private val repository: NewsRepository) :
    VoidUseCaseWithArgs<SaveArticleUseCaseArgs> {

    override suspend fun invoke(args: SaveArticleUseCaseArgs) {
        val article = ArticleDomainEntity(
            uuid = args.uuid,
            sourceName = args.sourceName,
            author = args.author,
            title = args.title,
            description = args.description,
            url = args.url,
            urlToImage = args.urlToImage,
            publishedAt = args.publishedAt,
            content = args.content,
        )

        repository.saveArticleToServer(article)
    }
}

data class SaveArticleUseCaseArgs(
    val uuid: String,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String
) : VoidUseCaseWithArgs.Args()
