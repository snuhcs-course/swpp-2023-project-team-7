package com.example.readability.ui.models.user

import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.user.TokenResponse
import com.example.readability.data.user.User
import com.example.readability.data.user.UserDao
import com.example.readability.data.user.UserInfoResponse
import com.example.readability.data.user.UserRemoteDataSource
import com.example.readability.data.user.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

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
        userRemoteDataSource = mockk()
        userDao = mockk(relaxed = true)
        networkStatusRepository = mockk()
        every { networkStatusRepository.isConnected } returns true
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

        coEvery { userRemoteDataSource.signIn(any(), any()) } returns Result.success(fakeResponse)

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
        coEvery {
            userRemoteDataSource.signIn(
                any(),
                any(),
            )
        } returns Result.failure(Throwable("Any custom error message should be handled"))

        // Act
        val result = userRepository.signIn(email, password)
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Any custom error message should be handled", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signUp success returns success`() = runTest {
        // Arrange
        val email = "test@example.com"
        val username = "testuser"
        val password = "password"

        coEvery { userRemoteDataSource.signUp(any(), any(), any()) } returns Result.success(Unit)
        coEvery { userRemoteDataSource.signIn(any(), any()) } returns Result.success(
            TokenResponse(
                "fake_access_token",
                "fake_refresh_token",
                "bearer",
            ),
        )

        // Act
        val result = userRepository.signUp(email, username, password)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signUp failure returns failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val username = "testuser"
        val password = "password"

        coEvery {
            userRemoteDataSource.signUp(
                any(),
                any(),
                any(),
            )
        } returns Result.failure(Throwable("Any custom error message should be handled"))

        // Act
        val result = userRepository.signUp(email, username, password)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Any custom error message should be handled", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getRefreshToken returns valid token`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )

        coEvery { userDao.get() } returns flowOf(fakeUser)

        // Act
        val result = userRepository.getRefreshToken()

        // Assert
        assertEquals("fake_refresh_token", result)
    }

    @Test
    fun `getAccessToken updates token when token is expired`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "expired_token",
            accessTokenLife = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email2",
            userName = "fake_name2",
            createdAt = "fake_created_at2",
            verified = 0,
        )
        val fakeRefreshToken = "fake_refresh_token"

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { userDao.updateAccessToken(any(), any()) } returns Unit
        coEvery {
            userRemoteDataSource.refreshAccessToken(fakeRefreshToken)
        } returns Result.success("fake_new_access_token")

        // Act
        val result = userRepository.getAccessToken()

        // Assert
        verify { userDao.updateAccessToken("fake_new_access_token", any()) }
    }

    @Test
    fun `getUserInfo success returns user info`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )
        val fakeUserInfoResponse = UserInfoResponse(
            username = "testuser",
            email = "test@example.com",
            created_at = "2023-01-01",
            verified = 1,
        )

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery {
            userRemoteDataSource.getUserInfo(fakeUser.accessToken!!)
        } returns Result.success(fakeUserInfoResponse)

        // Act
        val result = userRepository.getUserInfo()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(fakeUserInfoResponse, result.getOrNull())
    }

    @Test
    fun `getUserInfo failure returns failure`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery {
            userRemoteDataSource.getUserInfo(fakeUser.accessToken!!)
        } returns Result.failure(Throwable("Any custom error message"))

        // Act
        val result = userRepository.getUserInfo()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Any custom error message", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword success returns success`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )
        val newPassword = "new_password"

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery {
            userRemoteDataSource.changePassword(
                fakeUser.accessToken!!,
                newPassword,
            )
        } returns Result.success(Unit)

        // Act
        val result = userRepository.changePassword(newPassword)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `changePassword failure returns failure`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )
        val newPassword = "new_password"

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery { userRemoteDataSource.changePassword(fakeUser.accessToken!!, newPassword) } returns Result.failure(
            Throwable("Any custom error message"),
        )

        // Act
        val result = userRepository.changePassword(newPassword)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Any custom error message", result.exceptionOrNull()?.message)
    }

    @Test
    fun `signOut clears user data`() = runTest {
        // Arrange
        coEvery { userDao.deleteAll() } returns Unit

        // Act
        userRepository.signOut()

        // Assert
        coVerify(exactly = 1) { userDao.deleteAll() }
    }

    @Test
    fun `deleteAccount success returns success`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery { userRemoteDataSource.deleteAccount(fakeUser.accessToken!!) } returns Result.success(Unit)

        // Act
        val result = userRepository.deleteAccount()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteAccount failure returns failure`() = runTest {
        // Arrange
        val fakeUser = User(
            refreshToken = "fake_refresh_token",
            refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            accessToken = "fake_access_token",
            accessTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
            userEmail = "fake_email",
            userName = "fake_name",
            createdAt = "fake_created_at",
            verified = 0,
        )

        coEvery { userDao.get() } returns flowOf(fakeUser)
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery {
            userRemoteDataSource.deleteAccount(fakeUser.accessToken!!)
        } returns Result.failure(Throwable("Any custom error message"))

        // Act
        val result = userRepository.deleteAccount()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Any custom error message", result.exceptionOrNull()?.message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}
