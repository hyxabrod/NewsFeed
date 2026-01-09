package com.mako.newsfeed.domain.entity

import com.mako.newsfeed.core.DomainEntity
import com.mako.newsfeed.presentation.model.ArticlePresentationEntity

data class ArticleDomainEntity(
    val uuid: String,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String,
) : DomainEntity {

    override fun toPresentation(): ArticlePresentationEntity {
        return ArticlePresentationEntity(
            uuid = uuid,
            sourceName = sourceName,
            author = author,
            title = title,
            description = description,
            url = url,
            urlToImage = urlToImage,
            publishedAt = publishedAt,
            content = content,
        )
    }
}
