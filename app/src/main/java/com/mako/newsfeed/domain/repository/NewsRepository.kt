package com.mako.newsfeed.domain.repository

import com.mako.newsfeed.domain.entity.ArticleDomainEntity
import com.mako.newsfeed.domain.entity.NewsDomainEntity

interface NewsRepository {
    suspend fun getHeadlines(page: Int, isTopNews: Boolean): List<NewsDomainEntity>

    suspend fun saveHeadlines(articles: List<ArticleDomainEntity>)
    suspend fun clearHeadlines()
    suspend fun getHeadlinesFromDb(): List<NewsDomainEntity>
    suspend fun getNewsArticleById(uuid: String): ArticleDomainEntity
    suspend fun saveArticleToServer(article: ArticleDomainEntity): Boolean

    companion object {
        const val PAGE_SIZE: Int = 21
        const val TOP_HEADERS_PATH = "top-headlines"
        const val EVERYTHING_PATH = "everything"
    }
}

class UpgradeRequiredException : Exception()
