package com.example.readability.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readability.data.viewer.ViewerStyle
import com.example.readability.ui.screens.settings.AccountView
import com.example.readability.ui.screens.settings.ChangePasswordView
import com.example.readability.ui.screens.settings.SettingsView
import com.example.readability.ui.screens.settings.ViewerView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsView_isDisplayed() {
        val uniqueUsername = "user_${System.currentTimeMillis()}"
        composeTestRule.setContent {
            SettingsView(
                username = uniqueUsername,
            )
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText(uniqueUsername).assertIsDisplayed()
    }

    @Test
    fun settingsView_onBackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            SettingsView(
                username = "",
                onBack = { onBackCalled = true },
            )
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(onBackCalled)
    }

    @Test
    fun settingsView_onNavigateAccountSettingClicked() {
        var onNavigateAccountSettingCalled = false
        composeTestRule.setContent {
            SettingsView(
                username = "",
                onNavigateAccountSetting = { onNavigateAccountSettingCalled = true },
            )
        }
        composeTestRule.onNodeWithText("Account Settings").performClick()
        assert(onNavigateAccountSettingCalled)
    }

    @Test
    fun settingsView_onNavigateViewerSettingClicked() {
        var onNavigateViewerCalled = false
        composeTestRule.setContent {
            SettingsView(
                username = "",
                onNavigateViewer = { onNavigateViewerCalled = true },
            )
        }
        composeTestRule.onNodeWithText("Viewer Settings").performClick()
        assert(onNavigateViewerCalled)
    }

    @Test
    fun accountView_isDisplayed() {
        val uniqueUsername = "user_${System.currentTimeMillis()}"
        val uniqueEmail = "user_${System.currentTimeMillis()}@example.com"
        composeTestRule.setContent {
            AccountView(
                username = uniqueUsername,
                email = uniqueEmail,
            )
        }
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        composeTestRule.onNodeWithText(uniqueUsername).assertIsDisplayed()
        composeTestRule.onNodeWithText(uniqueEmail).assertIsDisplayed()
    }

    @Test
    fun accountView_onSignOutClicked() {
        var onSignOutCalled = false
        composeTestRule.setContent {
            AccountView(
                username = "",
                email = "",
                onSignOut = {
                    onSignOutCalled = true
                    Result.success(Unit)
                },
            )
        }
        composeTestRule.onNodeWithText("Sign Out").performClick()
        composeTestRule.waitUntil(1000L) {
            onSignOutCalled
        }
    }

    @Test
    fun accountView_onBackClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            AccountView(
                username = "",
                email = "",
                onBack = { onBackCalled = true },
            )
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(onBackCalled)
    }

    @Test
    fun accountView_onDeleteAccountClicked() {
        var onDeleteAccountCalled = false
        var onNavigateIntroCalled = false
        composeTestRule.setContent {
            AccountView(
                username = "",
                email = "",
                onDeleteAccount = {
                    onDeleteAccountCalled = true
                    Result.success(Unit)
                },
                onNavigateIntro = {
                    onNavigateIntroCalled = true
                }
            )
        }
        composeTestRule.onNodeWithText("Delete Account").performClick()
        composeTestRule.onNodeWithText("Delete My Account").performClick()
        composeTestRule.waitUntil(1000L) {
            onDeleteAccountCalled && onNavigateIntroCalled
        }
    }

    @Test
    fun accountView_onChangePasswordClicked() {
        var onChangePasswordCalled = false
        composeTestRule.setContent {
            AccountView(
                username = "",
                email = "",
                onNavigateChangePassword = { onChangePasswordCalled = true },
            )
        }
        composeTestRule.onNodeWithText("Change Password").performClick()
        composeTestRule.waitUntil(1000L) {
            onChangePasswordCalled
        }
    }

    @Test
    fun changePasswordView_isDisplayed() {
        composeTestRule.setContent {
            ChangePasswordView()
        }

        composeTestRule.onNodeWithText("Change Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Repeat Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
    }

    @Test
    fun changePasswordView_onConfirmClickedWithInvalidFields() {
        var onPasswordSubmitted = false
        composeTestRule.setContent {
            ChangePasswordView(
                onPasswordSubmitted = {
                    onPasswordSubmitted = true
                    Result.success(Unit)
                }
            )
        }
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithText("Confirm").performClick()

        assert(!onPasswordSubmitted)
        composeTestRule.onNodeWithTextAndError("At least 8 characters including a letter and a number")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTextAndError("Passwords do not match").assertIsDisplayed()
    }

    @Test
    fun changePasswordView_onConfirmClickedWithValidFields() {
        var onPasswordSubmitted = false
        var submittedPassword = ""
        var onBackCalled = false
        composeTestRule.setContent {
            ChangePasswordView(
                onPasswordSubmitted = {
                    onPasswordSubmitted = true
                    submittedPassword = it
                    Result.success(Unit)
                },
                onBack = {
                    onBackCalled = true
                },
            )
        }
        composeTestRule.onNodeWithTag("PasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithTag("RepeatPasswordTextField").performTextInput("testtest1")
        composeTestRule.onNodeWithText("Confirm").performClick()

        composeTestRule.waitUntil(1000L) {
            onPasswordSubmitted && onBackCalled
        }
        assert(submittedPassword == "testtest1")
    }

    @Test
    fun changePasswordView_onBackButtonClicked() {
        var onBackCalled = false
        composeTestRule.setContent {
            ChangePasswordView(
                onBack = { onBackCalled = true },
            )
        }
        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()
        assert(onBackCalled)
    }

    @Test
    fun viewerView_isDisplayed() {
        composeTestRule.setContent {
            ViewerView(
                viewerStyle = ViewerStyle()
            )
        }

        composeTestRule.onNodeWithText("Viewer Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Font Family").assertIsDisplayed()
        composeTestRule.onNodeWithText("Text Size").assertIsDisplayed()
        composeTestRule.onNodeWithText("Line Height").assertIsDisplayed()
        composeTestRule.onNodeWithText("Letter Spacing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paragraph Spacing").assertIsDisplayed()
    }

    // TODO: add integration tests for viewerView
    // It is not easy, since the text rendering is done by canvas and textPaint
    // One way: take screenshot, compare with reference rendering (most realistic)
    //    -> How to compare two images?
    //    -> How to get reference rendering?
    // Another way: pass mocked textPaint to the draw code?
}
