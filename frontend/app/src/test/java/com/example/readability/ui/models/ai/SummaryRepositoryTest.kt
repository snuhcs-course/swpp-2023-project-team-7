package com.example.readability.ui.models.ai

import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.ai.SummaryRemoteDataSource
import com.example.readability.data.ai.SummaryRepository
import com.example.readability.data.user.UserNotSignedInException
import com.example.readability.data.user.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SummaryRepositoryTest {

    private lateinit var summaryRepository: SummaryRepository
    private lateinit var summaryRemoteDataSource: SummaryRemoteDataSource
    private lateinit var userRepository: UserRepository
    private lateinit var networkStatusRepository: NetworkStatusRepository

    @Before
    fun setup() {
        summaryRemoteDataSource = mockk()
        userRepository = mockk()
        networkStatusRepository = mockk()
        summaryRepository = SummaryRepository(
            summaryRemoteDataSource,
            userRepository,
            networkStatusRepository
        )
    }

    @Test
    fun `getSummary when not connected to network should return failure`() = runTest {
        // Arrange
        coEvery { networkStatusRepository.isConnected } returns false

        // Act
        val result = summaryRepository.getSummary(1, 0.5)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() is Exception)
    }

    @Test
    fun `getSummary when user not signed in should return failure`() = runTest {
        // Arrange
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery { userRepository.getAccessToken() } returns null

        // Act
        val result = summaryRepository.getSummary(1, 0.5)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() is UserNotSignedInException)
    }

    @Test
    fun `getSummary success should update summary`() = runTest {
        // Arrange
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery { userRepository.getAccessToken() } returns "testAccessToken"
        coEvery { summaryRemoteDataSource.getSummary(any(), any(), any()) } returns flowOf("Summary 1", "Summary 2", "Summary 3")

        // Act
        val result = summaryRepository.getSummary(1, 0.5)

        // Assert
        assertEquals(Result.success(Unit), result)
        assertEquals("Summary 1Summary 2Summary 3", summaryRepository.summary.first())
    }

    @Test
    fun `clearSummary should reset summary`() = runTest {
        // Arrange
        summaryRepository.clearSummary()

        // Assert
        assertEquals("", summaryRepository.summary.first())
    }
}
