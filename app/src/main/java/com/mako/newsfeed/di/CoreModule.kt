package com.mako.newsfeed.di

import android.content.Context
import com.mako.newsfeed.core.LiveNetworkMonitor
import com.mako.newsfeed.core.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


@Module
@InstallIn(SingletonComponent::class)
object KtorModule {
    private const val BASE_URL = "https://newsapi.org/v2/"

    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                        Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        }
                )
            }
            install(Logging) {
                this.level = LogLevel.ALL
                this.logger = Logger.Companion.SIMPLE
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                url(BASE_URL)
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor {
        return LiveNetworkMonitor(context)
    }
}

suspend fun HttpClient.mockedPost(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
): Boolean {
    return true
}
