package com.example.readability.ui.models
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class AuthDBTest {

    private val userModel = UserModel()

    @Test
    fun signUp_SignIn_success() = runTest {
        // Arrange
        val email = "dbtesting@test.com"
        val username = "testuser"
        val password = "testpassword"

        // Act
        userModel.signUp(email, username, password)
        val signInResult = userModel.signIn(email, password)
        val signInFail = userModel.signIn(email, "wrongpassword")
        val emailsignUpFail = userModel.signUp(email, "newuser", password)
        val usersignUpFail = userModel.signUp("newemail@test.com", username, password)

        // Assert

        // SignIn Success case
        assertTrue(signInResult.isSuccess, "SignIn should succeed")
        assertEquals("Success", signInResult.getOrNull(), "SignIn result should be 'Success'")

        // SignIn in wrong password
        assertTrue(signInFail.isFailure)

        // SignUp in duplicated email
        Assert.assertTrue(emailsignUpFail.isSuccess)
        assertEquals("SignUp failed", emailsignUpFail.getOrNull(), "SignUp failed since email already exists")

        // SignUp in duplicated username
        Assert.assertTrue(usersignUpFail.isSuccess)
        assertEquals("SignUp failed", usersignUpFail.getOrNull(), "SignUp failed since username already exists")
    }
}
