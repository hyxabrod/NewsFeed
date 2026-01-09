package com.mako.newsfeed.domain.usecase

import com.mako.newsfeed.domain.entity.ArticleDomainEntity
import com.mako.newsfeed.domain.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetArticleByIdUseCaseTest {

    private lateinit var useCase: GetArticleByIdUseCase
    private lateinit var repository: NewsRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetArticleByIdUseCase(repository)
    }

    @Test
    fun `invoke should call getNewsArticleById with correct uuid`() = runTest {
        val testUuid = "test-uuid"
        val uuidSlot = slot<String>()
        val domainEntity = ArticleDomainEntity(
            uuid = testUuid,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { repository.getNewsArticleById(any()) } returns domainEntity

        useCase(GetArticleByIdUseCaseArgs(testUuid))

        coVerify(exactly = 1) { repository.getNewsArticleById(capture(uuidSlot)) }
        assertEquals(testUuid, uuidSlot.captured)
    }

    @Test
    fun `invoke should return presentation entity`() = runTest {
        val testUuid = "test-uuid"
        val domainEntity = ArticleDomainEntity(
            uuid = testUuid,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { repository.getNewsArticleById(testUuid) } returns domainEntity

        val result = useCase(GetArticleByIdUseCaseArgs(testUuid))

        assertEquals(testUuid, result.uuid)
        assertEquals("Source", result.sourceName)
        assertEquals("Author", result.author)
        assertEquals("Title", result.title)
        assertEquals("Description", result.description)
        assertEquals("url", result.url)
        assertEquals("urlToImage", result.urlToImage)
        assertEquals("2024-01-01", result.publishedAt)
        assertEquals("Content", result.content)
    }

    @Test
    fun `invoke should map domain entity to presentation entity correctly`() = runTest {
        val testUuid = "test-uuid-2"
        val domainEntity = ArticleDomainEntity(
            uuid = testUuid,
            sourceName = "Source2",
            author = "Author2",
            title = "Title2",
            description = "Description2",
            url = "url2",
            urlToImage = "urlToImage2",
            publishedAt = "2024-01-02",
            content = "Content2"
        )
        coEvery { repository.getNewsArticleById(testUuid) } returns domainEntity

        val result = useCase(GetArticleByIdUseCaseArgs(testUuid))

        assertEquals(domainEntity.uuid, result.uuid)
        assertEquals(domainEntity.sourceName, result.sourceName)
        assertEquals(domainEntity.author, result.author)
        assertEquals(domainEntity.title, result.title)
        assertEquals(domainEntity.description, result.description)
        assertEquals(domainEntity.url, result.url)
        assertEquals(domainEntity.urlToImage, result.urlToImage)
        assertEquals(domainEntity.publishedAt, result.publishedAt)
        assertEquals(domainEntity.content, result.content)
    }

    @Test
    fun `invoke should handle empty string uuid`() = runTest {
        val testUuid = ""
        val domainEntity = ArticleDomainEntity(
            uuid = testUuid,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { repository.getNewsArticleById(testUuid) } returns domainEntity

        val result = useCase(GetArticleByIdUseCaseArgs(testUuid))

        assertEquals(testUuid, result.uuid)
    }

}
