package com.mako.newsfeed.domain.entity

import android.util.Log
import com.mako.newsfeed.core.DomainEntity
import com.mako.newsfeed.presentation.model.NewsArticlePresentationEntity

data class NewsDomainEntity(
    val uuid: String,
    val sourceName: String,
    val title: String,
    val urlToImage: String,
    val publishedAt: String,
) : DomainEntity {

    override fun toPresentation(): NewsArticlePresentationEntity {
        Log.d("aa_aa", "urlToImage = $urlToImage")
        return NewsArticlePresentationEntity(
            uuid = uuid,
            sourceName = sourceName,
            title = title,
            urlToImage = urlToImage,
            publishedAt = publishedAt,
        )
    }
}
