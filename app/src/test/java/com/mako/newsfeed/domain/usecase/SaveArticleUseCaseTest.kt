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

class SaveArticleUseCaseTest {

    private lateinit var useCase: SaveArticleUseCase
    private lateinit var repository: NewsRepository

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveArticleUseCase(repository)
    }

    @Test
    fun `invoke should call saveArticleToServer`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { repository.saveArticleToServer(any()) } returns true

        useCase(args)

        coVerify(exactly = 1) { repository.saveArticleToServer(any()) }
    }

    @Test
    fun `invoke should pass correct uuid to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid-123",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("test-uuid-123", slot.captured.uuid)
    }

    @Test
    fun `invoke should pass correct sourceName to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Test Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("Test Source", slot.captured.sourceName)
    }

    @Test
    fun `invoke should pass correct author to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Test Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("Test Author", slot.captured.author)
    }

    @Test
    fun `invoke should pass correct title to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Test Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("Test Title", slot.captured.title)
    }

    @Test
    fun `invoke should pass correct description to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Test Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("Test Description", slot.captured.description)
    }

    @Test
    fun `invoke should pass correct url to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "https://test.url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("https://test.url", slot.captured.url)
    }

    @Test
    fun `invoke should pass correct urlToImage to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "https://test.image.url",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("https://test.image.url", slot.captured.urlToImage)
    }

    @Test
    fun `invoke should pass correct publishedAt to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-12-31",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("2024-12-31", slot.captured.publishedAt)
    }

    @Test
    fun `invoke should pass correct content to repository`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Test Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("Test Content", slot.captured.content)
    }

    @Test
    fun `invoke should create domain entity with all fields from args`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "test-uuid",
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals(args.uuid, slot.captured.uuid)
        assertEquals(args.sourceName, slot.captured.sourceName)
        assertEquals(args.author, slot.captured.author)
        assertEquals(args.title, slot.captured.title)
        assertEquals(args.description, slot.captured.description)
        assertEquals(args.url, slot.captured.url)
        assertEquals(args.urlToImage, slot.captured.urlToImage)
        assertEquals(args.publishedAt, slot.captured.publishedAt)
        assertEquals(args.content, slot.captured.content)
    }

    @Test
    fun `invoke should handle empty strings in args`() = runTest {
        val args = SaveArticleUseCaseArgs(
            uuid = "",
            sourceName = "",
            author = "",
            title = "",
            description = "",
            url = "",
            urlToImage = "",
            publishedAt = "",
            content = ""
        )
        val slot = slot<ArticleDomainEntity>()
        coEvery { repository.saveArticleToServer(capture(slot)) } returns true

        useCase(args)

        assertEquals("", slot.captured.uuid)
        assertEquals("", slot.captured.sourceName)
        assertEquals("", slot.captured.author)
        assertEquals("", slot.captured.title)
        assertEquals("", slot.captured.description)
        assertEquals("", slot.captured.url)
        assertEquals("", slot.captured.urlToImage)
        assertEquals("", slot.captured.publishedAt)
        assertEquals("", slot.captured.content)
    }

}
