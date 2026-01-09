package com.mako.newsfeed.presentation.viewmodel

import android.util.Log
import app.cash.turbine.test
import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.repository.UpgradeRequiredException
import com.mako.newsfeed.domain.usecase.GetHeadlinesUseCase
import com.mako.newsfeed.domain.usecase.GetHeadlinesUseCaseArgs
import com.mako.newsfeed.presentation.model.NewsArticlePresentationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsListViewModelTest {

    private lateinit var viewModel: NewsListViewModel
    private lateinit var getHeadlinesUseCase: GetHeadlinesUseCase
    private lateinit var networkMonitor: NetworkMonitor
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var networkStateFlow: MutableStateFlow<Boolean>

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)
        getHeadlinesUseCase = mockk()
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
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        assertEquals(true, viewModel.state.value.isTopNews)
        assertEquals(false, viewModel.state.value.isLoading)
        assertEquals(false, viewModel.state.value.isPullToRefreshVisible)
    }

    @Test
    fun `init should call loadHeadlines with page 1`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)
        advanceUntilIdle()

        val argsSlot = slot<GetHeadlinesUseCaseArgs>()
        coVerify(exactly = 1) { getHeadlinesUseCase(capture(argsSlot)) }
        assertEquals(1, argsSlot.captured.page)
        assertEquals(true, argsSlot.captured.isTopNews)
    }

    @Test
    fun `init should call observeNetworkStatus`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        assertEquals(false, viewModel.state.value.isOffline)
    }

    @Test
    fun `observeNetworkStatus should update isOffline when network is offline`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        networkStateFlow.emit(false)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isOffline)
    }

    @Test
    fun `observeNetworkStatus should update isOffline when network is online`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles
        networkStateFlow.value = false

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        networkStateFlow.emit(true)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isOffline)
    }

    @Test
    fun `processIntent LoadNextPage should not load when isLoading is true`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } returns articles
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(2, true)) } coAnswers {
            // Simulate long-running operation
            kotlinx.coroutines.delay(1000)
            articles
        }

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)
        advanceUntilIdle()

        viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
        testScheduler.advanceTimeBy(10) // Advance just a bit to enter the loading state

        viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            getHeadlinesUseCase(match { (page, isTopNews) -> isTopNews && page == 2 })
        }
    }

    @Test
    fun `processIntent LoadNextPage should not load when isPullToRefreshVisible is true`() =
        runTest {
            val articles = listOf(
                NewsArticlePresentationEntity(
                    "1", "Source", "Title", "url", "2024-01-01"
                )
            )
            coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } coAnswers {
                kotlinx.coroutines.delay(1000)
                articles
            }
            coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(2, true)) } returns articles

            viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

            viewModel.processIntent(NewsFeedListIntent.Refresh)
            testScheduler.advanceTimeBy(10) // Advance to enter refresh state

            viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
            advanceUntilIdle()

            coVerify(exactly = 0) { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(2, true)) }
        }

    @Test
    fun `processIntent LoadNextPage should emit toast when canLoadMore is false`() = runTest {
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } returns emptyList()

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is NewsFeedListEffect.ShowToast)
            assertEquals(
                "There is no more articles", (effect as NewsFeedListEffect.ShowToast).message
            )
        }
    }

    @Test
    fun `processIntent LoadNextPage should call getHeadlinesUseCase with correct page when canLoadMore is true`() =
        runTest {
            val articles = listOf(
                NewsArticlePresentationEntity(
                    "1", "Source", "Title", "url", "2024-01-01"
                )
            )
            coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } returns articles
            coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(2, true)) } returns articles

            viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)
            advanceUntilIdle()

            viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                getHeadlinesUseCase(match { (page, isTopNews) -> isTopNews && page == 2 })
            }
        }

    @Test
    fun `processIntent LoadNextPage should append articles to existing list`() = runTest {
        val firstPageArticles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title1", "url1", "2024-01-01"))
        val secondPageArticles =
            listOf(NewsArticlePresentationEntity("2", "Source", "Title2", "url2", "2024-01-02"))
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } returns firstPageArticles
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(2, true)) } returns secondPageArticles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.articles.size)
        assertEquals("1", viewModel.state.value.articles[0].uuid)
        assertEquals("2", viewModel.state.value.articles[1].uuid)
    }

    @Test
    fun `processIntent Refresh should not refresh when isLoading is true`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } returns articles
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(2, true)) } coAnswers {
            kotlinx.coroutines.delay(1000)
            articles
        }

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
        testScheduler.advanceTimeBy(10) // Advance to enter loading state

        viewModel.processIntent(NewsFeedListIntent.Refresh)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            getHeadlinesUseCase(match { (page, isTopNews) -> isTopNews && page == 1 })
        }
    }

    @Test
    fun `processIntent Refresh should set isPullToRefreshVisible to true`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        viewModel.processIntent(NewsFeedListIntent.Refresh)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isPullToRefreshVisible)
    }

    @Test
    fun `processIntent Refresh should call getHeadlinesUseCase with page 1`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)
        advanceUntilIdle()

        viewModel.processIntent(NewsFeedListIntent.Refresh)
        advanceUntilIdle()

        val argsList = mutableListOf<GetHeadlinesUseCaseArgs>()
        coVerify(exactly = 2) { getHeadlinesUseCase(capture(argsList)) }
        assertEquals(2, argsList.size)
        assertEquals(1, argsList[0].page)
        assertEquals(true, argsList[0].isTopNews)
        assertEquals(1, argsList[1].page)
        assertEquals(true, argsList[1].isTopNews)
    }

    @Test
    fun `processIntent ToggleTopNews should toggle isTopNews value`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        val initialValue = viewModel.state.value.isTopNews
        viewModel.processIntent(NewsFeedListIntent.ToggleTopNews)
        advanceUntilIdle()

        assertEquals(!initialValue, viewModel.state.value.isTopNews)
    }

    @Test
    fun `processIntent ToggleTopNews should call getHeadlinesUseCase with page 1 and isTopNews true`() =
        runTest {
            val articles = listOf(
                NewsArticlePresentationEntity(
                    "1", "Source", "Title", "url", "2024-01-01"
                )
            )
            coEvery { getHeadlinesUseCase(any()) } returns articles

            viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)
            advanceUntilIdle()

            viewModel.processIntent(NewsFeedListIntent.ToggleTopNews)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                getHeadlinesUseCase(match { (page, isTopNews) -> !isTopNews && page == 1 })
            }
        }

    @Test
    fun `processIntent ToggleTopNews should emit ScrollToTop effect`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        viewModel.effect.test {
            viewModel.processIntent(NewsFeedListIntent.ToggleTopNews)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is NewsFeedListEffect.ScrollToTop)
        }
    }

    @Test
    fun `loadHeadlines with page 1 should replace articles`() = runTest {
        val firstArticles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title1", "url1", "2024-01-01"))

        val secondArticles =
            listOf(NewsArticlePresentationEntity("2", "Source", "Title2", "url2", "2024-01-02"))
        coEvery { getHeadlinesUseCase(GetHeadlinesUseCaseArgs(1, true)) } returnsMany listOf(
            firstArticles,
            secondArticles
        )

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        viewModel.processIntent(NewsFeedListIntent.Refresh)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.articles.size)
        assertEquals("2", viewModel.state.value.articles[0].uuid)
    }

    @Test
    fun `loadHeadlines should set isPullToRefreshVisible to false after success`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        assertFalse(viewModel.state.value.isPullToRefreshVisible)
    }

    @Test
    fun `loadHeadlines should set isLoading to false after success`() = runTest {
        val articles =
            listOf(NewsArticlePresentationEntity("1", "Source", "Title", "url", "2024-01-01"))
        coEvery { getHeadlinesUseCase(any()) } returns articles

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadHeadlines should emit toast on UpgradeRequiredException`() = runTest {
        coEvery { getHeadlinesUseCase(any()) } throws UpgradeRequiredException()

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        viewModel.effect.test {
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is NewsFeedListEffect.ShowToast)
            assertEquals("Free limit reached", (effect as NewsFeedListEffect.ShowToast).message)
        }
    }

    @Test
    fun `loadHeadlines should handle generic exception`() = runTest {
        coEvery { getHeadlinesUseCase(any()) } throws RuntimeException("Test error")

        viewModel = NewsListViewModel(getHeadlinesUseCase, networkMonitor)

        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isPullToRefreshVisible)
    }
}
