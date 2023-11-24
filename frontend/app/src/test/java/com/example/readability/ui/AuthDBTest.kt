package com.example.readability.ui
import com.example.readability.data.user.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlin.time.Duration

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AuthDBTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userRepository: UserRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun signUp_SignIn_success() = runTest(timeout = Duration.parse("30s")) {
        // Arrange
        val email = "dbtesting@test.com"
        val username = "testuser"
        val password = "testpassword"

        // Act
        withContext(Dispatchers.IO) { userRepository.signUp(email, username, password) }
        val signInSuccess = withContext(Dispatchers.IO) { userRepository.signIn(email, password) }
        val signInFail = withContext(Dispatchers.IO) { userRepository.signIn(email, "wrongpassword") }
        val emailSignUpFail = withContext(Dispatchers.IO) { userRepository.signUp(email, "newuser", password) }
        val userSignUpFail =
            withContext(Dispatchers.IO) { userRepository.signUp("asdadasdasdsadasdsd@test.com", username, password) }

        // Assert

        // SignIn Success case
        assertTrue(signInSuccess.isSuccess, "SignIn should succeed")

        // SignIn in wrong password
        assertTrue(signInFail.isFailure, "SignIn should fail since password is wrong")

        // SignUp in duplicated email
        Assert.assertTrue(emailSignUpFail.isFailure)
        assertEquals(
            "Email already exists",
            emailSignUpFail.exceptionOrNull()?.message,
            "SignUp failed since email already exists",
        )

        // SignUp in duplicated username
        Assert.assertTrue(userSignUpFail.isFailure)
        assertEquals(
            "Username already exists",
            userSignUpFail.exceptionOrNull()?.message,
            "SignUp failed since username already exists",
        )
    }
}
