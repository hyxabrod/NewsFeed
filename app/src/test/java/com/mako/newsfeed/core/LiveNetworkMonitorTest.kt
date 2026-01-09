package com.mako.newsfeed.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LiveNetworkMonitorTest {

    private lateinit var networkMonitor: LiveNetworkMonitor
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities

    @Before
    fun setup() {
        context = mockk()
        connectivityManager = mockk(relaxed = true)
        network = mockk()
        networkCapabilities = mockk()

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    }

    @Test
    fun `isConnected should return true when network is available with internet capability`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        networkMonitor = LiveNetworkMonitor(context)

        val result = networkMonitor.isConnected()

        assertTrue(result)
    }

    @Test
    fun `isConnected should return false when activeNetwork is null`() {
        every { connectivityManager.activeNetwork } returns null

        networkMonitor = LiveNetworkMonitor(context)

        val result = networkMonitor.isConnected()

        assertFalse(result)
    }

    @Test
    fun `isConnected should return false when networkCapabilities is null`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        networkMonitor = LiveNetworkMonitor(context)

        val result = networkMonitor.isConnected()

        assertFalse(result)
    }

    @Test
    fun `isConnected should call activeNetwork on connectivityManager`() {
        every { connectivityManager.activeNetwork } returns null

        networkMonitor = LiveNetworkMonitor(context)
        networkMonitor.isConnected()

        verify(exactly = 1) { connectivityManager.activeNetwork }
    }

    @Test
    fun `isConnected should call getNetworkCapabilities when activeNetwork is not null`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(any()) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        networkMonitor = LiveNetworkMonitor(context)
        networkMonitor.isConnected()

        val networkSlot = slot<Network>()
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(capture(networkSlot)) }
        assertEquals(network, networkSlot.captured)
    }

    @Test
    fun `isConnected should call hasCapability with NET_CAPABILITY_INTERNET when capabilities exist`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(any()) } returns true

        networkMonitor = LiveNetworkMonitor(context)
        networkMonitor.isConnected()

        val capabilitySlot = slot<Int>()
        verify(exactly = 1) { networkCapabilities.hasCapability(capture(capabilitySlot)) }
        assertEquals(NetworkCapabilities.NET_CAPABILITY_INTERNET, capabilitySlot.captured)
    }

    @Test
    fun `constructor should call getSystemService with CONNECTIVITY_SERVICE`() {
        every { context.getSystemService(any()) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns null

        networkMonitor = LiveNetworkMonitor(context)

        val serviceNameSlot = slot<String>()
        verify(exactly = 1) { context.getSystemService(capture(serviceNameSlot)) }
        assertEquals(Context.CONNECTIVITY_SERVICE, serviceNameSlot.captured)
    }

    @Test
    fun `isConnected should not call getNetworkCapabilities when activeNetwork is null`() {
        every { connectivityManager.activeNetwork } returns null

        networkMonitor = LiveNetworkMonitor(context)
        networkMonitor.isConnected()

        verify(exactly = 0) { connectivityManager.getNetworkCapabilities(any()) }
    }

    @Test
    fun `isConnected should not call hasCapability when networkCapabilities is null`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        networkMonitor = LiveNetworkMonitor(context)
        networkMonitor.isConnected()

        verify(exactly = 0) { networkCapabilities.hasCapability(any()) }
    }

    @Test
    fun `multiple calls to isConnected should return consistent results`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        networkMonitor = LiveNetworkMonitor(context)

        val result1 = networkMonitor.isConnected()
        val result2 = networkMonitor.isConnected()

        assertTrue(result1)
        assertTrue(result2)
        assertEquals(result1, result2)
    }

    @Test
    fun `isConnected should return false when activeNetwork exists but has no internet capability`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        networkMonitor = LiveNetworkMonitor(context)

        val result = networkMonitor.isConnected()

        assertFalse(result)
    }
}
