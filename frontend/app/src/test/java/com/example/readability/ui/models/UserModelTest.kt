package com.example.readability.ui.models
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Response

@ExperimentalCoroutinesApi
class UserModelTest {
    private lateinit var loginAPI: LoginAPI // Replace with your actual API interface
    private lateinit var userModel: UserModel // Replace with your actual class that contains signIn

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        loginAPI = mock(LoginAPI::class.java)
        userModel = UserModel() // Assuming Authentication takes an API as a constructor parameter
        userModel.loginAPI = loginAPI
    }

    @Test
    fun `signIn success returns success`() = runTest {
        // Arrange
        val email = "fdsa@fdsa.com"
        val password = "fdsa"
        val fakeResponse = Response.success(UserResponse("access_token_value", "refresh_token_value", "Bearer"))

        val callMock = mock(Call::class.java) as Call<UserResponse>
        `when`(callMock.execute()).thenReturn(fakeResponse)
        `when`(
            loginAPI.signIn(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()),
        ).thenReturn(callMock)

        // Act
        val result = userModel.signIn(email, password)
        println(result)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
    }

    @Test
    fun `signIn failure returns failure`() = runTest {
        // Arrange
        val email = "fdsa@fdsa.com"
        val password = "fdsa"
        val fakeResponse = Response.error<UserResponse>(401, "Unauthorized".toResponseBody(null))

        val callMock = mock(Call::class.java) as Call<UserResponse>
        `when`(callMock.execute()).thenReturn(fakeResponse)
        `when`(
            loginAPI.signIn(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()),
        ).thenReturn(callMock)

        // Act
        val result = userModel.signIn(email, password)
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Login failed", result.exceptionOrNull()?.message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}
