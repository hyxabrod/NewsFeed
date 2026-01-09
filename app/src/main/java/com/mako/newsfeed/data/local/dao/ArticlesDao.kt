package com.mako.newsfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mako.newsfeed.data.local.entity.ArticleDataEntity

@Dao
interface ArticlesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleDataEntity>)

    @Query("SELECT * FROM articles")
    suspend fun getAllSync(): List<ArticleDataEntity>

    @Query("SELECT * FROM articles WHERE uuid = :uuid")
    suspend fun getByIdSync(uuid: String): ArticleDataEntity

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    @Update
    suspend fun update(article: ArticleDataEntity)
}
