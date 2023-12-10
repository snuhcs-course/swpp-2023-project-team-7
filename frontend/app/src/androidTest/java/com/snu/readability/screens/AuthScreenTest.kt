package com.snu.readability.screens

import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.snu.readability.MainActivity
import com.snu.readability.ui.screens.auth.AuthScreen
import com.snu.readability.ui.screens.auth.EmailView
import com.snu.readability.ui.screens.auth.IntroView
import com.snu.readability.ui.screens.auth.SignInView
import com.snu.readability.ui.screens.auth.SignUpView
import com.snu.readability.ui.theme.ReadabilityTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

fun hasTextWithError(text: String): SemanticsMatcher {
    return hasText(text).and(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.Error),
    )
}

fun ComposeContentTestRule.onNodeWithTextAndError(text: String): SemanticsNodeInteraction {
    return onNode(
        hasTextWithError(text),
    )
}

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun introView_ContinueWithEmailClicked() {
        var onContinueWithEmailCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                IntroView(
                    onContinueWithEmailClicked = { onContinueWithEmailCalled = true },
                )
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Continue with email"), 2500L)
        composeTestRule.onNodeWithText("Continue with email").performClick()
        assert(onContinueWithEmailCalled)
    }

    @Test
    fun emailView_NextClicked_WithEmptyEmail() {
        var onNavigateSignInCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                EmailView(
                    email = "",
                    onNavigateSignIn = {
                        onNavigateSignInCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Sign in").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        assert(!onNavigateSignInCalled)
    }

    @Test
    fun emailView_OnEmailChanged() {
        var onEmailChangedCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                EmailView(
                    email = "",
                    onEmailChanged = {
                        onEmailChangedCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test@example.com")
        assert(onEmailChangedCalled)
    }

    @Test
    fun emailView_NextClicked_WithInvalidEmail() {
        var onNavigateSignInCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                EmailView(
                    email = "testexample.com",
                    onNavigateSignIn = {
                        onNavigateSignInCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Sign in").performClick()

        composeTestRule.onNodeWithTextAndError("Please enter a valid email address").assertExists()
            .assertIsDisplayed()
        assert(!onNavigateSignInCalled)
    }

    @Test
    fun emailView_NextClicked_WithValidEmail() {
        var onNavigateSignInCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                EmailView(
                    email = "test@example.com",
                    onNavigateSignIn = {
                        onNavigateSignInCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Sign in").performClick()

        assert(onNavigateSignInCalled)
    }

    @Test
    fun emailView_SignUpClicked() {
        var onNavigateSignUpCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                EmailView(
                    email = "",
                    onNavigateSignUp = {
                        onNavigateSignUpCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Sign up").performClick()

        assert(onNavigateSignUpCalled)
    }

    @Test
    fun emailView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                EmailView(
                    email = "",
                    onBack = {
                        onBackCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @Test
    fun signInView_SignInClicked_WithEmptyPassword() {
        var onPasswordSubmittedCalled = false
        composeTestRule.activity.setContent {
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
        composeTestRule.activity.setContent {
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
        composeTestRule.activity.setContent {
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
    fun signInView_BackButtonClicked() {
        var onBackCalled = false
        composeTestRule.activity.setContent {
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
        composeTestRule.activity.setContent {
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
        composeTestRule.activity.setContent {
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
        composeTestRule.activity.setContent {
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
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                SignUpView(onBack = {
                    onBackCalled = true
                })
            }
        }

        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()

        assert(onBackCalled)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun authScreen_SignIn() {
        var onNavigateBookListCalled = false
        composeTestRule.activity.setContent {
            ReadabilityTheme {
                AuthScreen(
                    onNavigateBookList = {
                        onNavigateBookListCalled = true
                    },
                )
            }
        }

        // 1. Continue with Email
        composeTestRule.waitUntilAtLeastOneExists(hasText("Continue with email"), 2500L)
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
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test11@example.com")
        // 6. click Sign in
        composeTestRule.onNodeWithTag("SignInButton").performClick()
        // 7. write password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("test")
        // 8. click Sign in
        composeTestRule.onNodeWithTag("SignInButton").performClick()
        // 9. see error
        composeTestRule.waitUntilAtLeastOneExists(
            hasTextWithError("Incorrect email or password"),
            2500L,
        )
        // 10. rewrite password
        composeTestRule.onNodeWithTag("PasswordTextField").performTextClearance()
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest1")
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
    fun authScreen_SignUp() {
        var onNavigateBookListCalled = false
        lateinit var navController: TestNavHostController
        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            ReadabilityTheme {
                AuthScreen(navController = navController, onNavigateBookList = {
                    onNavigateBookListCalled = true
                })
            }
        }

        // 1. Continue with Email
        composeTestRule.waitUntilAtLeastOneExists(hasText("Continue with email"), 2500L)
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
        composeTestRule.onNodeWithTag("EmailTextField").performTextInput("test15@example.com")
        // 12. rewrite username
        composeTestRule.onNodeWithTag("UsernameTextField").performTextClearance()
        composeTestRule.onNodeWithTag("UsernameTextField").performTextInput("test15")
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
