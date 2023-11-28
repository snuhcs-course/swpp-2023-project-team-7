package com.example.readability.data

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // a MutableStateFlow which represents the current network status
    // It is automatically updated when the network becomes available.
    // However, it is not automatically updated when the network becomes unavailable.
    val connectedState = MutableStateFlow(false)
    val isConnected: Boolean
        get() {
            val result = connectivityManager.activeNetworkInfo?.isConnected ?: false
            connectedState.value = result
            return result
        }

    init {
        connectedState.value = isConnected
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                connectedState.value = true
            }

            // This is not actually called?
            override fun onUnavailable() {
                super.onUnavailable()
                connectedState.value = false
            }
        })
    }
}
