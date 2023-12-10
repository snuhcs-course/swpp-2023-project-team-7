package com.snu.readability.ui.models.user

import com.snu.readability.data.user.RefreshTokenResponse
import com.snu.readability.data.user.SignUpResponse
import com.snu.readability.data.user.TokenResponse
import com.snu.readability.data.user.UserAPI
import com.snu.readability.data.user.UserInfoResponse
import com.snu.readability.data.user.UserRemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class UserRemoteDataSourceTest {

    private lateinit var userAPI: UserAPI
    private lateinit var userRemoteDataSource: UserRemoteDataSource

    @Before
    fun setup() {
        userAPI = mockk(relaxed = true)
        userRemoteDataSource = UserRemoteDataSource(userAPI)
    }

    @Test
    fun `signIn success should return token response`() = runTest {
        // Arrange
        coEvery {
            userAPI.signIn(
                grantType = any(),
                scope = any(),
                clientId = any(),
                clientSecret = any(),
                username = any(),
                password = any(),
            ).execute()
        } returns Response.success(TokenResponse("access_token", "refresh_token", "token_type"))

        // Act
        val result = userRemoteDataSource.signIn("email", "password")

        // Assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertNotNull(result.getOrNull())
        Assert.assertEquals("access_token", result.getOrNull()?.access_token)
    }

    @Test
    fun `signIn failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            userAPI.signIn(
                grantType = any(),
                scope = any(),
                clientId = any(),
                clientSecret = any(),
                username = any(),
                password = any(),
            ).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = userRemoteDataSource.signIn("email", "password")

        // Assert
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `signUp success should return Unit`() = runTest {
        // Arrange
        coEvery { userAPI.signUp(any()).execute() } returns Response.success(SignUpResponse(true))

        // Act
        val result = userRemoteDataSource.signUp("email", "username", "password")

        // Assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull() is Unit)
    }

    @Test
    fun `signUp failure should return a failure result`() = runTest {
        // Arrange
        coEvery { userAPI.signUp(any()).execute() } returns Response.error(
            400,
            "".toResponseBody("application/json".toMediaTypeOrNull()),
        )

        // Act
        val result = userRemoteDataSource.signUp("email", "username", "password")

        // Assert
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `refreshAccessToken success should return access token`() = runTest {
        // Arrange
        coEvery { userAPI.refreshAccessToken(any()).execute() } returns Response.success(
            RefreshTokenResponse(
                "new_access_token",
                "token_type",
            ),
        )

        // Act
        val result = userRemoteDataSource.refreshAccessToken("refresh_token")

        // Assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals("new_access_token", result.getOrNull())
    }

    @Test
    fun `refreshAccessToken failure should return a failure result`() = runTest {
        // Arrange
        coEvery { userAPI.refreshAccessToken(any()).execute() } returns Response.error(
            400,
            "".toResponseBody("application/json".toMediaTypeOrNull()),
        )

        // Act
        val result = userRemoteDataSource.refreshAccessToken("refresh_token")

        // Assert
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `getUserInfo success should return user info response`() = runTest {
        // Arrange
        coEvery { userAPI.getUserInfo(any()).execute() } returns Response.success(
            UserInfoResponse(
                "username",
                "email",
                "created_at",
                1,
            ),
        )

        // Act
        val result = userRemoteDataSource.getUserInfo("access_token")

        // Assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertNotNull(result.getOrNull())
        Assert.assertEquals("username", result.getOrNull()?.username)
    }

    @Test
    fun `getUserInfo failure should return a failure result`() = runTest {
        // Arrange
        coEvery { userAPI.getUserInfo(any()).execute() } returns Response.error(
            400,
            "".toResponseBody("application/json".toMediaTypeOrNull()),
        )

        // Act
        val result = userRemoteDataSource.getUserInfo("access_token")

        // Assert
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `changePassword success should return Unit`() = runTest {
        // Arrange
        coEvery { userAPI.changePassword(any(), any()).execute() } returns Response.success(Unit)

        // Act
        val result = userRemoteDataSource.changePassword("access_token", "new_password")

        // Assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull() is Unit)
    }

    @Test
    fun `changePassword failure should return a failure result`() = runTest {
        // Arrange
        coEvery { userAPI.changePassword(any(), any()).execute() } returns Response.error(
            400,
            "".toResponseBody("application/json".toMediaTypeOrNull()),
        )

        // Act
        val result = userRemoteDataSource.changePassword("access_token", "new_password")

        // Assert
        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `deleteAccount success should return Unit`() = runTest {
        // Arrange
        coEvery { userAPI.deleteAccount(any()).execute() } returns Response.success(Unit)

        // Act
        val result = userRemoteDataSource.deleteAccount("access_token")

        // Assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertTrue(result.getOrNull() is Unit)
    }

    @Test
    fun `deleteAccount failure should return a failure result`() = runTest {
        // Arrange
        coEvery { userAPI.deleteAccount(any()).execute() } returns Response.error(
            400,
            "".toResponseBody("application/json".toMediaTypeOrNull()),
        )

        // Act
        val result = userRemoteDataSource.deleteAccount("access_token")

        // Assert
        Assert.assertTrue(result.isFailure)
    }
}
