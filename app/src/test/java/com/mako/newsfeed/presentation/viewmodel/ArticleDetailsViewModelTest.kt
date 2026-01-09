package com.mako.newsfeed.presentation.viewmodel

import app.cash.turbine.test
import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.usecase.GetArticleByIdUseCase
import com.mako.newsfeed.domain.usecase.GetArticleByIdUseCaseArgs
import com.mako.newsfeed.domain.usecase.SaveArticleUseCase
import com.mako.newsfeed.domain.usecase.SaveArticleUseCaseArgs
import com.mako.newsfeed.presentation.model.ArticlePresentationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailsViewModelTest {

    private lateinit var viewModel: ArticleDetailsViewModel
    private lateinit var getArticleByIdUseCase: GetArticleByIdUseCase
    private lateinit var saveArticleUseCase: SaveArticleUseCase
    private lateinit var networkMonitor: NetworkMonitor
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var networkStateFlow: MutableStateFlow<Boolean>
    private val testArticleId = "test-article-id"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getArticleByIdUseCase = mockk()
        saveArticleUseCase = mockk()
        networkMonitor = mockk()
        networkStateFlow = MutableStateFlow(true)
        every { networkMonitor.isOnline } returns networkStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should set initial state`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isOffline)
    }

    @Test
    fun `init should call observeNetworkStatus`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        assertFalse(viewModel.state.value.isOffline)
    }

    @Test
    fun `observeNetworkStatus should update isOffline when network is offline`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        networkStateFlow.emit(false)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isOffline)
    }

    @Test
    fun `observeNetworkStatus should update isOffline when network is online`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        networkStateFlow.value = false

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        networkStateFlow.emit(true)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isOffline)
    }

    @Test
    fun `loadArticle should call getArticleByIdUseCase with correct articleId`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        val argsSlot = slot<GetArticleByIdUseCaseArgs>()
        coVerify(exactly = 1) { getArticleByIdUseCase(capture(argsSlot)) }
        assertEquals(testArticleId, argsSlot.captured.uuid)
    }

    @Test
    fun `loadArticle should update state with article on success`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        assertEquals(article, viewModel.state.value.article)
    }

    @Test
    fun `loadArticle should emit ShowSnackbar effect on exception`() = runTest {
        coEvery { getArticleByIdUseCase(any()) } throws RuntimeException("Test error")

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        viewModel.effect.test {
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ArticleDetailsEffect.ShowSnackbar)
            assertEquals(
                "Failed to load article",
                (effect as ArticleDetailsEffect.ShowSnackbar).message
            )
        }
    }

    @Test
    fun `loadArticle should not update article state on exception`() = runTest {
        coEvery { getArticleByIdUseCase(any()) } throws RuntimeException("Test error")

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        assertNull(viewModel.state.value.article)
    }

    @Test
    fun `processIntent SaveArticle should call saveArticle`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } returns Unit

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveArticleUseCase(any()) }
    }

    @Test
    fun `saveArticle should return early when article is null`() = runTest {
        coEvery { getArticleByIdUseCase(any()) } throws RuntimeException("Test error")

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
        advanceUntilIdle()

        coVerify(exactly = 0) { saveArticleUseCase(any()) }
    }

    @Test
    fun `saveArticle should set isSaving to true before saving`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } coAnswers {
            assertTrue(viewModel.state.value.isSaving)
        }

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
        advanceUntilIdle()
    }

    @Test
    fun `saveArticle should call saveArticleUseCase with correct arguments`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } returns Unit

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
        advanceUntilIdle()

        val argsSlot = slot<SaveArticleUseCaseArgs>()
        coVerify(exactly = 1) { saveArticleUseCase(capture(argsSlot)) }
        assertEquals(testArticleId, argsSlot.captured.uuid)
        assertEquals("Source", argsSlot.captured.sourceName)
        assertEquals("Author", argsSlot.captured.author)
        assertEquals("Title", argsSlot.captured.title)
        assertEquals("Description", argsSlot.captured.description)
        assertEquals("url", argsSlot.captured.url)
        assertEquals("urlToImage", argsSlot.captured.urlToImage)
        assertEquals("2024-01-01", argsSlot.captured.publishedAt)
        assertEquals("Content", argsSlot.captured.content)
    }

    @Test
    fun `saveArticle should emit ShowSnackbar effect with Done message on success`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } returns Unit

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ArticleDetailsEffect.ShowSnackbar)
            assertEquals("Done", (effect as ArticleDetailsEffect.ShowSnackbar).message)
        }
    }

    @Test
    fun `saveArticle should emit ShowSnackbar effect with error message on exception`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } throws RuntimeException("Test error")

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ArticleDetailsEffect.ShowSnackbar)
            assertEquals("Failed to save", (effect as ArticleDetailsEffect.ShowSnackbar).message)
        }
    }

    @Test
    fun `saveArticle should set isSaving to false after success`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } returns Unit

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `saveArticle should set isSaving to false after exception`() = runTest {
        val article = ArticlePresentationEntity(
            uuid = testArticleId,
            sourceName = "Source",
            author = "Author",
            title = "Title",
            description = "Description",
            url = "url",
            urlToImage = "urlToImage",
            publishedAt = "2024-01-01",
            content = "Content"
        )
        coEvery { getArticleByIdUseCase(any()) } returns article
        coEvery { saveArticleUseCase(any()) } throws RuntimeException("Test error")

        viewModel = ArticleDetailsViewModel(
            getArticleByIdUseCase,
            saveArticleUseCase,
            networkMonitor,
            testArticleId
        )

        viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSaving)
    }

}
