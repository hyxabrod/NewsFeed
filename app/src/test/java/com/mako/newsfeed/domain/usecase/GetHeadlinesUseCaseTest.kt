package com.mako.newsfeed.domain.usecase

import android.util.Log
import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.entity.NewsDomainEntity
import com.mako.newsfeed.domain.repository.NewsRepository
import com.mako.newsfeed.presentation.model.NewsArticlePresentationEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetHeadlinesUseCaseTest {

    private lateinit var useCase: GetHeadlinesUseCase
    private lateinit var repository: NewsRepository
    private lateinit var networkMonitor: NetworkMonitor

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        repository = mockk()
        networkMonitor = mockk()
        useCase = GetHeadlinesUseCase(repository, networkMonitor)
    }

    @Test
    fun `invoke should call clearHeadlines when page is 1 and network is connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.clearHeadlines() } returns Unit
        coEvery { repository.getHeadlines(1, false) } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(1, false))

        coVerify(exactly = 1) { repository.clearHeadlines() }
    }

    @Test
    fun `invoke should not call clearHeadlines when page is greater than 1 and network is connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.getHeadlines(2, false) } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(2, false))

        coVerify(exactly = 0) { repository.clearHeadlines() }
    }

    @Test
    fun `invoke should call getHeadlines with correct page when network is connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        val pageSlot = slot<Int>()
        val isTopNewsSlot = slot<Boolean>()
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.clearHeadlines() } returns Unit
        coEvery { repository.getHeadlines(any(), any()) } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(1, false))

        coVerify(exactly = 1) { repository.getHeadlines(capture(pageSlot), capture(isTopNewsSlot)) }
        assertEquals(1, pageSlot.captured)
        assertEquals(false, isTopNewsSlot.captured)
    }

    @Test
    fun `invoke should call getHeadlines with correct isTopNews when network is connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        val pageSlot = slot<Int>()
        val isTopNewsSlot = slot<Boolean>()
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.clearHeadlines() } returns Unit
        coEvery { repository.getHeadlines(any(), any()) } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(1, true))

        coVerify(exactly = 1) { repository.getHeadlines(capture(pageSlot), capture(isTopNewsSlot)) }
        assertEquals(1, pageSlot.captured)
        assertEquals(true, isTopNewsSlot.captured)
    }

    @Test
    fun `invoke should return list of presentation entities when network is connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01"),
            NewsDomainEntity("2", "Source2", "Title2", "url2", "2024-01-02")
        )
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.clearHeadlines() } returns Unit
        coEvery { repository.getHeadlines(1, false) } returns domainEntities

        val result = useCase(GetHeadlinesUseCaseArgs(1, false))

        assertEquals(2, result.size)
        assertEquals("1", result[0].uuid)
        assertEquals("Source", result[0].sourceName)
        assertEquals("Title", result[0].title)
        assertEquals("url", result[0].urlToImage)
        assertEquals("2024-01-01", result[0].publishedAt)
    }

    @Test
    fun `invoke should return mapped presentation entities when network is connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.clearHeadlines() } returns Unit
        coEvery { repository.getHeadlines(1, false) } returns domainEntities

        val result = useCase(GetHeadlinesUseCaseArgs(1, false))

        assertEquals(domainEntities[0].uuid, result[0].uuid)
        assertEquals(domainEntities[0].sourceName, result[0].sourceName)
        assertEquals(domainEntities[0].title, result[0].title)
        assertEquals(domainEntities[0].urlToImage, result[0].urlToImage)
        assertEquals(domainEntities[0].publishedAt, result[0].publishedAt)
    }

    @Test
    fun `invoke should call getHeadlinesFromDb when network is not connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns false
        coEvery { repository.getHeadlinesFromDb() } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(1, false))

        coVerify(exactly = 1) { repository.getHeadlinesFromDb() }
    }

    @Test
    fun `invoke should not call clearHeadlines when network is not connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns false
        coEvery { repository.getHeadlinesFromDb() } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(1, false))

        coVerify(exactly = 0) { repository.clearHeadlines() }
    }

    @Test
    fun `invoke should not call getHeadlines when network is not connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns false
        coEvery { repository.getHeadlinesFromDb() } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(1, false))

        coVerify(exactly = 0) { repository.getHeadlines(any(), any()) }
    }

    @Test
    fun `invoke should return list of presentation entities from db when network is not connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01"),
            NewsDomainEntity("2", "Source2", "Title2", "url2", "2024-01-02")
        )
        every { networkMonitor.isConnected() } returns false
        coEvery { repository.getHeadlinesFromDb() } returns domainEntities

        val result = useCase(GetHeadlinesUseCaseArgs(1, false))

        assertEquals(2, result.size)
        assertEquals("1", result[0].uuid)
        assertEquals("Source", result[0].sourceName)
        assertEquals("Title", result[0].title)
    }

    @Test
    fun `invoke should return mapped presentation entities from db when network is not connected`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        every { networkMonitor.isConnected() } returns false
        coEvery { repository.getHeadlinesFromDb() } returns domainEntities

        val result = useCase(GetHeadlinesUseCaseArgs(1, false))

        assertEquals(domainEntities[0].uuid, result[0].uuid)
        assertEquals(domainEntities[0].sourceName, result[0].sourceName)
        assertEquals(domainEntities[0].title, result[0].title)
        assertEquals(domainEntities[0].urlToImage, result[0].urlToImage)
        assertEquals(domainEntities[0].publishedAt, result[0].publishedAt)
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.clearHeadlines() } returns Unit
        coEvery { repository.getHeadlines(1, false) } returns emptyList()

        val result = useCase(GetHeadlinesUseCaseArgs(1, false))

        assertEquals(0, result.size)
    }

    @Test
    fun `invoke should pass correct page value to repository`() = runTest {
        val domainEntities = listOf(
            NewsDomainEntity("1", "Source", "Title", "url", "2024-01-01")
        )
        val pageSlot = slot<Int>()
        val isTopNewsSlot = slot<Boolean>()
        every { networkMonitor.isConnected() } returns true
        coEvery { repository.getHeadlines(any(), any()) } returns domainEntities

        useCase(GetHeadlinesUseCaseArgs(5, false))

        coVerify(exactly = 1) { repository.getHeadlines(capture(pageSlot), capture(isTopNewsSlot)) }
        assertEquals(5, pageSlot.captured)
        assertEquals(false, isTopNewsSlot.captured)
    }

}
