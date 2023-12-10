package com.example.readability.ui.models.network

import android.content.Context
import android.net.ConnectivityManager
import com.example.readability.data.NetworkStatusDataSource
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkStatusDataSourceTest {

    private lateinit var networkStatusDataSource: NetworkStatusDataSource
    private lateinit var mockContext: Context
    private lateinit var mockConnectivityManager: ConnectivityManager

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockContext = mockk(relaxed = true)
        mockConnectivityManager = mockk(relaxed = true)
        every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockConnectivityManager
        networkStatusDataSource = NetworkStatusDataSource(mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isConnected returns correct value based on ConnectivityManager`() {
        // Arrange
        every { mockConnectivityManager.activeNetworkInfo?.isConnected } returns true

        // Act
        val result = networkStatusDataSource.isConnected

        // Assert
        assertEquals(true, result)
        assertEquals(true, networkStatusDataSource.connectedState.value)
    }

    @Test
    fun `onAvailable callback updates connectedState to true`() = runTest {
        // Arrange
        coEvery { mockConnectivityManager.activeNetworkInfo?.isConnected } returns false

        // Act
        networkStatusDataSource.isConnected // call to trigger initialization
        coEvery { mockConnectivityManager.activeNetworkInfo?.isConnected } returns true
        networkStatusDataSource.onAvailable()

        // Assert
        assertEquals(true, networkStatusDataSource.connectedState.value)
    }

    @Test
    fun `onUnavailable callback updates connectedState to false`() = runTest {
        // Arrange
        coEvery { mockConnectivityManager.activeNetworkInfo?.isConnected } returns true

        // Act
        networkStatusDataSource.isConnected // call to trigger initialization
        coEvery { mockConnectivityManager.activeNetworkInfo?.isConnected } returns false
        networkStatusDataSource.onUnavailable()

        // Assert
        assertEquals(false, networkStatusDataSource.connectedState.value)
    }
}
