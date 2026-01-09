package com.mako.newsfeed.di

import android.app.Application
import androidx.room.Room
import com.mako.newsfeed.data.local.NewsDatabase
import com.mako.newsfeed.data.local.dao.ArticlesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNewsDatabase(app: Application): NewsDatabase {
        return Room.databaseBuilder(app, NewsDatabase::class.java, "news_db")
            .build()
    }

    @Provides
    @Singleton
    fun provideArticlesDao(db: NewsDatabase): ArticlesDao {
        return db.articlesDao()
    }
}
