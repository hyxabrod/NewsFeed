package com.mako.newsfeed.presentation.model

import com.mako.newsfeed.core.PresentationEntity

data class ArticlePresentationEntity(
    val uuid: String,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String,
) : PresentationEntity
