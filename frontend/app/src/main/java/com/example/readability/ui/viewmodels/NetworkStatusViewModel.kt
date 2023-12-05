package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.data.NetworkStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkStatusViewModel @Inject constructor(
    private val networkStatusRepository: NetworkStatusRepository,
) : ViewModel() {
    val isConnected: Boolean
        get() = networkStatusRepository.isConnected
    val connectedState = networkStatusRepository.connectedState
}
