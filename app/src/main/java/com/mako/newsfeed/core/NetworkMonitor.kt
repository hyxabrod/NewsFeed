package com.mako.newsfeed.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isConnected(): Boolean
}

class LiveNetworkMonitor(private val context: Context) : NetworkMonitor {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isOnline: Flow<Boolean> =
        callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        channel.trySend(true)
                    }

                    override fun onLost(network: Network) {
                        channel.trySend(false)
                    }
                }

            val request =
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

            connectivityManager.registerNetworkCallback(request, callback)
            channel.trySend(connectivityManager.isCurrentlyConnected())
            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }
            .conflate()

    override fun isConnected(): Boolean {
        return connectivityManager.isCurrentlyConnected()
    }

    private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
        val activeNetwork = activeNetwork ?: return false
        val networkCapabilities = getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
