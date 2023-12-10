package com.snu.readability
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.snu.readability.data.user.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.Duration

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
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
        assertTrue("SignIn should succeed", signInSuccess.isSuccess)

        // SignIn in wrong password
        assertTrue("SignIn should fail since password is wrong", signInFail.isFailure)

        // SignUp in duplicated email
        Assert.assertTrue(emailSignUpFail.isFailure)
        assertEquals(
            "SignUp failed since email already exists",
            "Email already exists",
            emailSignUpFail.exceptionOrNull()?.message,
        )

        // SignUp in duplicated username
        Assert.assertTrue(userSignUpFail.isFailure)
        assertEquals(
            "SignUp failed since username already exists",
            "Username already exists",
            userSignUpFail.exceptionOrNull()?.message,
        )
    }
}
