package com.example.readability.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusRepository @Inject constructor(
    private val networkStatusDataSource: NetworkStatusDataSource,
) {
    val isConnected: Boolean
        get() = networkStatusDataSource.isConnected
    val connectedState = networkStatusDataSource.connectedState
}
