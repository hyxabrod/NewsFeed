package com.mako.newsfeed.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mako.newsfeed.data.local.dao.ArticlesDao
import com.mako.newsfeed.data.local.entity.ArticleDataEntity

@Database(entities = [ArticleDataEntity::class], version = 1, exportSchema = false)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDao
}
