package com.snu.readability.ui.models.network

import com.snu.readability.data.NetworkStatusDataSource
import com.snu.readability.data.NetworkStatusRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NetworkStatusRepositoryTest {

    private lateinit var networkStatusRepository: NetworkStatusRepository
    private lateinit var mockNetworkStatusDataSource: NetworkStatusDataSource

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockNetworkStatusDataSource = mockk(relaxed = true)
        networkStatusRepository = NetworkStatusRepository(mockNetworkStatusDataSource)
    }

    @Test
    fun `isConnected returns correct value based on NetworkStatusDataSource`() {
        // Arrange
        coEvery { mockNetworkStatusDataSource.isConnected } returns true

        // Act
        val result = networkStatusRepository.isConnected

        // Assert
        assertEquals(true, result)
    }
}
