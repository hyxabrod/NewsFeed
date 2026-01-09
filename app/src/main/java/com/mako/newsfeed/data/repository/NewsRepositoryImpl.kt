package com.mako.newsfeed.data.repository

import com.mako.newsfeed.BuildConfig
import com.mako.newsfeed.data.local.dao.ArticlesDao
import com.mako.newsfeed.data.local.entity.ArticleDataEntity
import com.mako.newsfeed.data.remote.model.NewsResponse
import com.mako.newsfeed.data.remote.proto.ArticleProto
import com.mako.newsfeed.data.remote.proto.ArticleSaveResponse
import com.mako.newsfeed.data.remote.proto.ArticleSaveServiceGrpcKt.ArticleSaveServiceCoroutineStub
import com.mako.newsfeed.domain.entity.ArticleDomainEntity
import com.mako.newsfeed.domain.entity.NewsDomainEntity
import com.mako.newsfeed.domain.repository.NewsRepository
import com.mako.newsfeed.domain.repository.UpgradeRequiredException
import io.grpc.ManagedChannelBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.map

class NewsRepositoryImpl
@Inject
constructor(private val client: HttpClient, private val dao: ArticlesDao) : NewsRepository {
    override suspend fun getHeadlines(
        page: Int,
        isTopNews: Boolean
    ): List<NewsDomainEntity> {
        val response = fetchHeadlines(page, isTopNews)
        val articles = response.articles.map { dto ->
            ArticleDomainEntity(
                uuid = UUID.nameUUIDFromBytes(dto.url.toByteArray()).toString(),
                sourceName = dto.source?.name ?: "",
                title = dto.title,
                urlToImage = dto.urlToImage ?: "",
                publishedAt = dto.publishedAt,
                author = dto.author ?: "",
                content = dto.content ?: "",
                description = dto.description ?: "",
                url = dto.url
            )
        }
        saveHeadlines(articles)

        return articles.map { news ->
            NewsDomainEntity(
                uuid = news.uuid,
                sourceName = news.sourceName,
                title = news.title,
                urlToImage = news.urlToImage,
                publishedAt = news.publishedAt
            )
        }
    }

    override suspend fun saveArticleToServer(article: ArticleDomainEntity): Boolean {
        kotlinx.coroutines.delay(1000) // Simulate network delay
        val proto =
            ArticleProto.newBuilder()
                .setTitle(article.title)
                .setUrl(article.url)
                .setContent(article.content)
                .build()

        return sendArticleProtobuf(proto)
    }

    private suspend fun fetchHeadlines(page: Int, isTopNews: Boolean): NewsResponse {
        val path =
            if (isTopNews) NewsRepository.TOP_HEADERS_PATH else NewsRepository.EVERYTHING_PATH
        val data = client
            .get(path) {
                if (isTopNews) parameter("country", "us")
                if (!isTopNews) parameter("q", "USA")//
                parameter("apiKey", BuildConfig.API_KEY)
                parameter("pageSize", NewsRepository.PAGE_SIZE)
                parameter("page", page)
            }
        if (data.status == HttpStatusCode.UpgradeRequired) {
            throw UpgradeRequiredException()
        }
        return data.body()
    }

    override suspend fun saveHeadlines(articles: List<ArticleDomainEntity>) {
        val entities =
            articles.map { domain ->
                ArticleDataEntity(
                    uuid = domain.uuid,
                    sourceName = domain.sourceName,
                    author = domain.author,
                    title = domain.title,
                    description = domain.description,
                    url = domain.url,
                    urlToImage = domain.urlToImage,
                    publishedAt = domain.publishedAt,
                    content = domain.content,
                )
            }
        dao.insertAll(entities)
    }

    override suspend fun clearHeadlines() {
        dao.deleteAll()
    }

    override suspend fun getHeadlinesFromDb(): List<NewsDomainEntity> {
        return dao.getAllSync().map { news ->
            NewsDomainEntity(
                uuid = news.uuid,
                sourceName = news.sourceName,
                title = news.title,
                urlToImage = news.urlToImage,
                publishedAt = news.publishedAt
            )
        }
    }

    override suspend fun getNewsArticleById(uuid: String): ArticleDomainEntity {
        return dao.getByIdSync(uuid).toDomain()
    }

    private suspend fun sendArticleProtobuf(proto: ArticleProto): Boolean {
        val channel = ManagedChannelBuilder.forAddress("aBaseUrl", 8080).usePlaintext().build()
        val stub = ArticleSaveServiceCoroutineStub(channel)
        val response = stub.fakeSendArticle(request = proto)

        return response.success

        // .. or
        //        return client.mockedPost("/save-article") {
        //            contentType(ContentType.Application.ProtoBuf)
        //            setBody(proto.toByteArray())
        //        }
    }
}

suspend fun ArticleSaveServiceCoroutineStub.fakeSendArticle(
    request: ArticleProto
): ArticleSaveResponse {
    return ArticleSaveResponse.newBuilder().setSuccess(true).build()
}

