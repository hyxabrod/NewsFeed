package com.mako.newsfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mako.newsfeed.core.DataEntity
import com.mako.newsfeed.domain.entity.ArticleDomainEntity
import com.mako.newsfeed.domain.entity.NewsDomainEntity

@Entity(tableName = "articles")
data class ArticleDataEntity(
    @PrimaryKey val uuid: String,
    val sourceName: String,
    val author: String,
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String,
    val publishedAt: String,
    val content: String,
) : DataEntity {

    override fun toDomain(): ArticleDomainEntity {
        return ArticleDomainEntity(
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
