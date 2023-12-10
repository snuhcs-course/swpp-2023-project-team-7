package com.snu.readability.data

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _connectedState = MutableStateFlow(false)
    val connectedState = _connectedState.asStateFlow()
    val isConnected: Boolean
        get() {
            val result = connectivityManager.activeNetworkInfo?.isConnected ?: false
            _connectedState.value = result
            return result
        }

    init {
        _connectedState.value = isConnected
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                super.onAvailable(network)
                this@NetworkStatusDataSource.onAvailable()
            }

            // This is not actually called?
            override fun onUnavailable() {
                super.onUnavailable()
                this@NetworkStatusDataSource.onUnavailable()
            }
        })
    }

    fun onAvailable() {
        _connectedState.value = true
    }

    fun onUnavailable() {
        _connectedState.value = false
    }
}
