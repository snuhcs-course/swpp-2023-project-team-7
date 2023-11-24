package com.example.readability.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readability.ui.screens.settings.SettingsView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsView_isDisplayed() {
        composeTestRule.setContent {
            SettingsView()
        }
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
