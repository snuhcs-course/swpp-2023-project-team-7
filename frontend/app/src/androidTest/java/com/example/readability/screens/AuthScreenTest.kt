package com.example.readability.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readability.ui.screens.auth.IntroView
import com.example.readability.ui.theme.ReadabilityTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
}