package com.mako.newsfeed.presentation.model

import com.mako.newsfeed.core.PresentationEntity

data class NewsArticlePresentationEntity(
        val uuid: String,
        val sourceName: String,
        val title: String,
        val urlToImage: String,
        val publishedAt: String,
) : PresentationEntity
