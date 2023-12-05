package com.example.readability.ui.models
import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.user.TokenResponse
import com.example.readability.data.user.UserDao
import com.example.readability.data.user.UserRemoteDataSource
import com.example.readability.data.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class UserRepositoryTest {

    // classes to be mocked
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var userDao: UserDao
    private lateinit var networkStatusRepository: NetworkStatusRepository

    // class under test
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        userRemoteDataSource = mock(UserRemoteDataSource::class.java)
        userDao = mock(UserDao::class.java)
        networkStatusRepository = mock(NetworkStatusRepository::class.java)
        `when`(networkStatusRepository.isConnected).thenReturn(true)
        userRepository = UserRepository(
            userRemoteDataSource = userRemoteDataSource,
            userDao = userDao,
            networkStatusRepository = networkStatusRepository,
        )
    }

    @Test
    fun `signIn success returns success`() = runTest {
        // Arrange
        val email = "fdsa@fdsa.com"
        val password = "fdsa"
        val fakeResponse = TokenResponse("access_token", "refresh_token", "bearer")

        `when`(
            userRemoteDataSource.signIn(anyString(), anyString()),
        ).thenReturn(Result.success(fakeResponse))

        // Act
        val result = userRepository.signIn(email, password)
        println(result)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signIn failure returns failure`() = runTest {
        // Arrange
        val email = "fdsa@fdsa.com"
        val password = "fdsa"
//        val fakeResponse = Response.error<UserResponse>(401, "Unauthorized".toResponseBody(null))

//        val callMock = mock(Call::class.java) as Call<UserResponse>
//        `when`(callMock.execute()).thenReturn(fakeResponse)
        `when`(
            userRemoteDataSource.signIn(anyString(), anyString()),
        ).thenReturn(Result.failure(Throwable("Any custom error message should be handled")))

        // Act
        val result = userRepository.signIn(email, password)
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Any custom error message should be handled", result.exceptionOrNull()?.message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}
