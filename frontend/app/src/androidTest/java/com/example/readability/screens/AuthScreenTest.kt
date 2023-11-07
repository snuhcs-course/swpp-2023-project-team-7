package com.example.readability.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readability.ui.screens.auth.AuthScreen
import com.example.readability.ui.screens.auth.EmailView
import com.example.readability.ui.screens.auth.ForgotPasswordView
import com.example.readability.ui.screens.auth.IntroView
import com.example.readability.ui.screens.auth.ResetPasswordView
import com.example.readability.ui.screens.auth.SignInView
import com.example.readability.ui.screens.auth.SignUpView
import com.example.readability.ui.screens.auth.VerifyEmailView
import com.example.readability.ui.theme.ReadabilityTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

fun hasTextWithError(text: String): SemanticsMatcher {
    return hasText(text).and(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)
    )
}

fun ComposeContentTestRule.onNodeWithTextAndError(text: String): SemanticsNodeInteraction {
    return onNode(
        hasTextWithError(text)
    )
}


@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun introView_ContinueWithEmailClicked() {
        var onContinueWithEmailCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                IntroView(
                    onContinueWithEmailClicked = { onContinueWithEmailCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Continue with email").performClick()
        assert(onContinueWithEmailCalled)
    }

    @Test
    fun emailView_NextClicked_WithEmptyEmail() {
        var onNavigateSignInCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                EmailView(onNavigateSignIn = {
                    onNavigateSignInCalled = true
                })
            }
        }

        composeTestRule.onNodeWithText("Sign in").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        assert(!onNavigateSignInCalled)
    }

    @Test
    fun emailView_NextClicked_WithInvalidEmail() {
        var onNavigateSignInCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                EmailView(onNavigateSignIn = {
                    onNavigateSignInCalled = true
                })
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test")
        composeTestRule.onNodeWithText("Sign in").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        assert(!onNavigateSignInCalled)
    }

    @Test
    fun emailView_NextClicked_WithValidEmail() {
        var onNavigateSignInCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                EmailView(onNavigateSignIn = {
                    onNavigateSignInCalled = true
                })
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Sign in").performClick()

        assert(onNavigateSignInCalled)
    }

    @Test
    fun emailView_SignUpClicked() {
        var onNavigateSignUpCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                EmailView(onNavigateSignUp = {
                    onNavigateSignUpCalled = true
                })
            }
        }

        composeTestRule.onNodeWithText("Sign up").performClick()

        assert(onNavigateSignUpCalled)
    }

    @Test
    fun emailView_ForgotPasswordClicked() {
        var onNavigateForgotPasswordCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                EmailView(onNavigateForgotPassword = {
                    onNavigateForgotPasswordCalled = true
                })
            }
        }

        composeTestRule.onNodeWithText("Forgot password?").performClick()

        assert(onNavigateForgotPasswordCalled)
    }

    @Test
    fun emailView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                EmailView(onBack = {
                    onBackCalled = true
                })
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @Test
    fun signInView_SignInClicked_WithEmptyPassword() {
        var onPasswordSubmittedCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                SignInView(email = "test@example.com", onPasswordSubmitted = {
                    onPasswordSubmittedCalled = true
                    Result.success(Unit)
                })
            }
        }

        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a password").assertExists()
            .assertIsDisplayed()
        assert(!onPasswordSubmittedCalled)
    }

    @Test
    fun signInView_SignInClicked_WithInvalidPassword() {
        composeTestRule.setContent {
            ReadabilityTheme {
                SignInView(email = "test@example.com", onPasswordSubmitted = {
                    Result.failure(Exception("Invalid password"))
                })
            }
        }

        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.onNodeWithText("Invalid password").assertExists().assertIsDisplayed()
    }

    @Test
    fun signInView_SignInClicked_WithValidPassword() {
        var onNavigateBookListCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                SignInView(email = "test@example.com", onPasswordSubmitted = {
                    Result.success(Unit)
                }, onNavigateBookList = {
                    onNavigateBookListCalled = true
                })
            }
        }

        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest")
        composeTestRule.onNodeWithTag("SignInButton").performClick()

        composeTestRule.waitUntil(2500L) {
            onNavigateBookListCalled
        }
    }

    @Test
    fun signInView_ForgotPasswordClicked() {
        var onNavigateForgotPasswordCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                SignInView(email = "test@example.com", onNavigateForgotPassword = {
                    onNavigateForgotPasswordCalled = true
                })
            }
        }

        composeTestRule.onNodeWithText("Forgot password?").performClick()

        assert(onNavigateForgotPasswordCalled)
    }

    @Test
    fun signInView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                SignInView(email = "test@example.com", onBack = {
                    onBackCalled = true
                })
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @Test
    fun signUpView_SignUpClicked_WithEmptyArguments() {
        composeTestRule.setContent {
            ReadabilityTheme {
                SignUpView()
            }
        }

        composeTestRule.onNodeWithTag("SignUpButton").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("Please enter a valid username").assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertExists().assertIsDisplayed()
    }

    @Test
    fun signUpView_SignUpClicked_WithInvalidArguments() {
        composeTestRule.setContent {
            ReadabilityTheme {
                SignUpView()
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("UsernameTextField").performTextInput("") // empty username
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("SignUpButton").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("Please enter a valid username").assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertExists().assertIsDisplayed()
    }

    @Test
    fun signUpView_SignUpClicked_WithValidArguments() {
        var onNavigateVerifyCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                SignUpView(onNavigateVerify = {
                    onNavigateVerifyCalled = true
                })
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("UsernameTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("SignUpButton").performClick()

        composeTestRule.waitUntil(2500L) {
            onNavigateVerifyCalled
        }
    }

    @Test
    fun signUpView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                SignUpView(onBack = {
                    onBackCalled = true
                })
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @Test
    fun verifyEmailView_NextClicked_WithEmptyVerificationCode() {
        var onVerificationCodeSubmittedCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                VerifyEmailView(
                    email = "test@example.com",
                    fromSignUp = false,
                    onVerificationCodeSubmitted = {
                        onVerificationCodeSubmittedCalled = true
                        Result.success(Unit)
                    })
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()
        assert(!onVerificationCodeSubmittedCalled)
    }

    @Test
    fun verifyEmailView_NextClicked_FromSignUp() {
        var onVerificationCodeSubmittedCalled = false
        var onNavigateBookListCalled = false
        var onNavigateResetPasswordCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                VerifyEmailView(
                    email = "test@example.com",
                    fromSignUp = true,
                    onVerificationCodeSubmitted = {
                        onVerificationCodeSubmittedCalled = true
                        Result.success(Unit)
                    },
                    onNavigateBookList = {
                        onNavigateBookListCalled = true
                    },
                    onNavigateResetPassword = {
                        onNavigateResetPasswordCalled = true
                    })
            }
        }

        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextInput("123456")
        composeTestRule.onNodeWithText("Next").performClick()
        assert(onVerificationCodeSubmittedCalled)
        composeTestRule.waitUntil (2500L) {
            onNavigateBookListCalled || onNavigateResetPasswordCalled
        }
        assert(onNavigateBookListCalled)
        assert(!onNavigateResetPasswordCalled)
    }

    @Test
    fun verifyEmailView_NextClicked_FromForgotPassword() {
        var onVerificationCodeSubmittedCalled = false
        var onNavigateBookListCalled = false
        var onNavigateResetPasswordCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                VerifyEmailView(
                    email = "test@example.com",
                    fromSignUp = false,
                    onVerificationCodeSubmitted = {
                        onVerificationCodeSubmittedCalled = true
                        Result.success(Unit)
                    },
                    onNavigateBookList = {
                        onNavigateBookListCalled = true
                    },
                    onNavigateResetPassword = {
                        onNavigateResetPasswordCalled = true
                    })
            }
        }

        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextInput("123456")
        composeTestRule.onNodeWithText("Next").performClick()
        assert(onVerificationCodeSubmittedCalled)
        composeTestRule.waitUntil (2500L) {
            onNavigateBookListCalled || onNavigateResetPasswordCalled
        }
        assert(!onNavigateBookListCalled)
        assert(onNavigateResetPasswordCalled)
    }

    @Test
    fun verifyEmailView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                VerifyEmailView(email = "test@example.com", fromSignUp = false, onBack = {
                    onBackCalled = true
                })
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @Test
    fun forgotPasswordView_NextClicked_WithEmptyEmail() {
        var onEmailSubmittedCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                ForgotPasswordView(
                    onEmailSubmitted = {
                        onEmailSubmittedCalled = true
                        Result.success(Unit)
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Next").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        assert(!onEmailSubmittedCalled)
    }

    @Test
    fun forgotPasswordView_NextClicked_WithInvalidEmail() {
        var onEmailSubmittedCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                ForgotPasswordView(
                    onEmailSubmitted = {
                        onEmailSubmittedCalled = true
                        Result.success(Unit)
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test")
        composeTestRule.onNodeWithText("Next").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        assert(!onEmailSubmittedCalled)
    }

    @Test
    fun forgotPasswordView_NextClicked_WithValidEmail() {
        var onEmailSubmittedCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                ForgotPasswordView(
                    onEmailSubmitted = {
                        onEmailSubmittedCalled = true
                        Result.success(Unit)
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Next").performClick()
        assert(onEmailSubmittedCalled)
    }

    @Test
    fun forgotPasswordView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                ForgotPasswordView(
                    onBack = {
                        onBackCalled = true
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @Test
    fun resetPasswordView_ResetPasswordClicked_WithEmptyInputs() {
        composeTestRule.setContent {
            ReadabilityTheme {
                ResetPasswordView()
            }
        }

        composeTestRule.onNodeWithTag("ResetPasswordButton").performClick()

        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun resetPasswordView_ResetPasswordClicked_WithInvalidInputs() {
        composeTestRule.setContent {
            ReadabilityTheme {
                ResetPasswordView()
            }
        }

        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("ResetPasswordButton").performClick()

        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("Passwords do not match").assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun resetPasswordView_ResetPasswordClicked_WithValidInputs() {
        var onResetPasswordSubmittedCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                ResetPasswordView(
                    onPasswordSubmitted = {
                        onResetPasswordSubmittedCalled = true
                        Result.success(Unit)
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("ResetPasswordButton").performClick()

        assert(onResetPasswordSubmittedCalled)
    }

    @Test
    fun resetPasswordView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                ResetPasswordView(
                    onBack = {
                        onBackCalled = true
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun authScreen_SignIn() {
        var onNavigateBookListCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                AuthScreen(
                    onNavigateBookList = {
                        onNavigateBookListCalled = true
                    }
                )
            }
        }

        // 1. Continue with Email
        composeTestRule.onNodeWithText("Continue with email").performClick()
        // 2. write email
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("testexample.com")
        // 3. click Sign in
        composeTestRule.onNodeWithTag("SignInButton").performClick()
        // 4. assert email error
        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        // 5. rewrite email
        composeTestRule.onNodeWithTag("EmailTextField").performTextClearance()
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        // 6. click Sign in
        composeTestRule.onNodeWithTag("SignInButton").performClick()
        // 7. write password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("test")
        // 8. click Sign in
        composeTestRule.onNodeWithTag("SignInButton").performClick()
        // 9. see error
        composeTestRule.waitUntilAtLeastOneExists(
            hasTextWithError("Password is incorrect"),
            2500L
        )
        composeTestRule.onNodeWithText("Password is incorrect").assertIsDisplayed()
        // 10. rewrite password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextClearance()
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest")
        // 11. click Sign in
        composeTestRule.onNodeWithTag("SignInButton").performClick()
        composeTestRule.waitUntil(2500L) {
            onNavigateBookListCalled
        }
        // 12. assert onNavigateBookListCalled
        assert(onNavigateBookListCalled)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun authScreen_ForgotPassword() {
        lateinit var navController: TestNavHostController
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            ReadabilityTheme {
                AuthScreen(navController = navController)
            }
        }

        // 1. Continue with Email
        composeTestRule.onNodeWithText("Continue with email").performClick()
        // 2. click Forgot password
        composeTestRule.onNodeWithTag("ForgotPasswordButton").performClick()
        // 3. write email
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("testexample.com")
        // 4. click Next
        composeTestRule.onNodeWithTag("NextButton").performClick()
        // 5. assert email error
        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        // 6. rewrite email
        composeTestRule.onNodeWithTag("EmailTextField").performTextClearance()
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        // 7. click Next
        composeTestRule.onNodeWithTag("NextButton").performClick()
        // 8. check if navigate to VerifyEmailView
        composeTestRule.waitUntilAtLeastOneExists(hasText("Verify Email"), 2500L)
        // 9. write empty verification code
        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextInput("")
        // 10. click Next
        composeTestRule.onNodeWithTag("NextButton").performClick()
        // 11. No navigation
        composeTestRule.onNodeWithText("Verify Email").assertIsDisplayed()
        // 12. rewrite verification code
        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextClearance()
        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextInput("123456")
        // 13. click Next
        composeTestRule.onNodeWithTag("NextButton").performClick()

        // 14. check if navigate to ResetPasswordView
        composeTestRule.waitUntilAtLeastOneExists(hasText("Reset Password"), 2500L)
        // 15. write password and repeat password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("test")
        // 16. click Reset password
        composeTestRule.onNodeWithTag("ResetPasswordButton").performClick()
        // 17. assert error
        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("Passwords do not match").assertExists()
            .assertIsDisplayed()
        // 18. rewrite password and repeat password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextClearance()
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextClearance()
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest1")
        // 19. click Reset password
        composeTestRule.onNodeWithTag("ResetPasswordButton").performClick()
        // 20. check if navigate to EmailView
        composeTestRule.waitUntilAtLeastOneExists(hasText("Continue with email"), 2500L)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun authScreen_SignUp() {
        var onNavigateBookListCalled = false
        lateinit var navController: TestNavHostController
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            ReadabilityTheme {
                AuthScreen(navController = navController, onNavigateBookList = {
                    onNavigateBookListCalled = true
                })
            }
        }

        // 1. Continue with Email
        composeTestRule.onNodeWithText("Continue with email").performClick()
        // 2. click Sign up
        composeTestRule.onNodeWithTag("SignUpButton").performClick()
        // 3. write email
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("testexample.com")
        // 4. write empty username
        composeTestRule.onNodeWithTag("UsernameTextField").performTextInput("")
        // 5. write password and repeat password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("test")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest")
        // 6. click Sign up
        composeTestRule.onNodeWithTag("SignUpButton").performClick()
        // 7. assert email error
        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        // 8. assert username error
        composeTestRule.onNodeWithTextAndError("Please enter a valid username").assertExists()
            .assertIsDisplayed()
        // 9. assert password error
        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertExists()
            .assertIsDisplayed()
        // 10. assert repeat password error
        composeTestRule.onNodeWithTextAndError("Passwords do not match").assertExists()
            .assertIsDisplayed()
        // 11. rewrite email
        composeTestRule.onNodeWithTag("EmailTextField").performTextClearance()
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        // 12. rewrite username
        composeTestRule.onNodeWithTag("UsernameTextField").performTextClearance()
        composeTestRule.onNodeWithTag("UsernameTextField").performTextInput("test")
        // 13. rewrite password and repeat password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextClearance()
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextClearance()
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest1")
        // 14. click Sign up
        composeTestRule.onNodeWithTag("SignUpButton").performClick()
        // 15. check if navigate to VerifyEmailView
        composeTestRule.waitUntilAtLeastOneExists(hasText("Verify Email"), 2500L)
        // 16. write empty verification code
        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextInput("")
        // 17. click Next
        composeTestRule.onNodeWithTag("NextButton").performClick()
        // 18. No navigation
        composeTestRule.onNodeWithText("Verify Email").assertIsDisplayed()
        // 19. rewrite verification code
        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextClearance()
        composeTestRule.onNodeWithTag("VerificationCodeTextField").performTextInput("123456")
        // 20. click Next
        composeTestRule.onNodeWithTag("NextButton").performClick()
        // 21. check if navigate to BookList
        composeTestRule.waitUntil(2500L) {
            onNavigateBookListCalled
        }
    }
}