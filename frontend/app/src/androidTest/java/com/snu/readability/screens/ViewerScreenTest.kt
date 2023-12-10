package com.snu.readability.screens

import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.snu.readability.data.ai.Quiz
import com.snu.readability.data.ai.QuizLoadState
import com.snu.readability.data.book.Book
import com.snu.readability.data.viewer.PageSplitData
import com.snu.readability.data.viewer.ViewerStyle
import com.snu.readability.data.viewer.getPageIndex
import com.snu.readability.ui.screens.viewer.QuizReportView
import com.snu.readability.ui.screens.viewer.QuizView
import com.snu.readability.ui.screens.viewer.SummaryView
import com.snu.readability.ui.screens.viewer.ViewerView
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewerScreenTest {
    companion object {

        lateinit var openBoatBookData: Book
        lateinit var pageSplitData: PageSplitData

        @JvmStatic
        @BeforeClass
        fun initOpenBoatBookData() {
            val content = javaClass.classLoader!!.getResource("the_open_boat.txt")!!.readText()
            openBoatBookData = Book(
                bookId = 1,
                title = "The Open Boat",
                author = "Stephen Crane",
                content = "",
                contentData = content,
                progress = 0.5,
                coverImage = "",
                summaryProgress = 1.0,
            )
            val pageSplits = mutableListOf<Int>()
            for (i in 0..content.length step 100) {
                pageSplits.add(i)
            }
            if (pageSplits.last() != content.length) {
                pageSplits.add(content.length)
            }
            pageSplitData = PageSplitData(
                pageSplits = pageSplits,
                width = 300,
                height = 300,
                viewerStyle = ViewerStyle(),
            )
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun viewerView_LeftClick() {
        var width = 0f
        var height = 0f
        var bookData =
            openBoatBookData.copy(progress = (1.5 / pageSplitData.pageSplits.size))
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = bookData,
                pageSplitData = pageSplitData,
                onProgressChange = {
                    bookData = bookData.copy(progress = it)
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click left side of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.1f, height * 0.5f))
            up()
        }
        // check if progress is decreased by one page
        assert(pageSplitData.getPageIndex(bookData.progress) == 0)
    }

    @Test
    fun viewerView_RightClick() {
        var width = 0f
        var height = 0f
        var bookData =
            openBoatBookData.copy(progress = 0.0)
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = bookData,
                pageSplitData = pageSplitData,
                onProgressChange = {
                    bookData = bookData.copy(progress = it)
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click right side of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.9f, height * 0.5f))
            up()
        }
        // check if progress is increased by one page
        assert(pageSplitData.getPageIndex(bookData.progress) == 1)
    }

    @Test
    fun viewerView_CenterClick() {
        var width = 0f
        var height = 0f
        var bookData =
            openBoatBookData.copy(progress = (1.5 / pageSplitData.pageSplits.size))
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = bookData,
                pageSplitData = pageSplitData,
                onProgressChange = {
                    bookData = bookData.copy(progress = it)
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click center of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.5f, height * 0.5f))
            up()
        }
        // check if overlay displayed
        composeTestRule.onNodeWithText("The Open Boat").assertIsDisplayed()
    }

    @Test
    fun viewerView_LeftSwipe() {
        var bookData = openBoatBookData.copy(progress = 0.0)
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = bookData,
                pageSplitData = pageSplitData,
                onProgressChange = {
                    bookData = bookData.copy(progress = it)
                },
            )
        }
        // swipe left
        composeTestRule.onRoot().performTouchInput {
            swipeLeft()
        }
        // check if progress is increased by one page
        assert(pageSplitData.getPageIndex(bookData.progress) == 1)
    }

    @Test
    fun viewerView_RightSwipe() {
        var bookData =
            openBoatBookData.copy(progress = (1.5 / pageSplitData.pageSplits.size))
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = bookData,
                pageSplitData = pageSplitData,
                onProgressChange = {
                    bookData = bookData.copy(progress = it)
                },
            )
        }
        // swipe right
        composeTestRule.onRoot().performTouchInput {
            swipeRight()
        }
        // check if progress is decreased by one page
        assert(pageSplitData.getPageIndex(bookData.progress) == 0)
    }

    @Test
    fun viewerView_BackButtonClicked() {
        var width = 0f
        var height = 0f
        var onBack = false
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = openBoatBookData,
                pageSplitData = pageSplitData,
                onBack = {
                    onBack = true
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click center of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.5f, height * 0.5f))
            up()
        }
        // click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        // check if onBack is true
        assert(onBack)
    }

    @Test
    fun viewerView_SettingButtonClicked() {
        var width = 0f
        var height = 0f
        var onNavigateSettings = false
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = openBoatBookData,
                pageSplitData = pageSplitData,
                onNavigateSettings = {
                    onNavigateSettings = true
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click center of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.5f, height * 0.5f))
            up()
        }
        // click setting button
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        // check if onNavigateSettings is true
        assert(onNavigateSettings)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun viewerView_GenerateSummaryClicked() {
        var width = 0f
        var height = 0f
        var onNavigateSummary = false
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = openBoatBookData.copy(progress = 0.5),
                pageSplitData = pageSplitData,
                onNavigateSummary = {
                    onNavigateSummary = true
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click center of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.5f, height * 0.5f))
            up()
        }
        composeTestRule.waitUntilAtLeastOneExists(hasText("Generate Summary"), 1000L)
        // click generate summary
        composeTestRule.onNodeWithText("Generate Summary").performClick()
        // check if onNavigateSummary is true
        assert(onNavigateSummary)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun viewerView_GenerateQuizClicked() {
        var width = 0f
        var height = 0f
        var onNavigateQuiz = false
        composeTestRule.setContent {
            ViewerView(
                isNetworkConnected = true,
                bookData = openBoatBookData,
                pageSplitData = pageSplitData,
                onNavigateQuiz = {
                    onNavigateQuiz = true
                },
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        // click center of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.5f, height * 0.5f))
            up()
        }
        composeTestRule.waitUntilAtLeastOneExists(hasText("Generate Quiz"), 1000L)
        // click generate quiz
        composeTestRule.onNodeWithText("Generate Quiz").performClick()
        // check if onNavigateQuiz is true
        assert(onNavigateQuiz)
    }

    @Test
    fun quizView_Displayed() {
        composeTestRule.setContent {
            QuizView(
                quizList = listOf(
                    Quiz(question = "question1", answer = "answer1"),
                ),
                quizSize = 1,
                quizLoadState = QuizLoadState.LOADED,
            )
        }

        // check if quiz is displayed and answer is not displayed
        composeTestRule.onNodeWithText("question1").assertIsDisplayed()
        composeTestRule.onNodeWithText("answer1").assertIsDisplayed()
    }

    @Test
    fun quizView_BackButtonClicked() {
        var onBack = false
        composeTestRule.setContent {
            QuizView(
                quizList = listOf(
                    Quiz(question = "question1", answer = "answer1"),
                ),
                quizSize = 1,
                quizLoadState = QuizLoadState.LOADED,
                onBack = { onBack = true },
            )
        }

        // click back button
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        // check if onBack is true
        assert(onBack)
    }

    @Test
    fun quizView_ReportButtonClicked() {
        var onNavigateReport = false
        composeTestRule.setContent {
            QuizView(
                quizList = listOf(
                    Quiz(question = "question1", answer = "answer1"),
                ),
                quizSize = 1,
                quizLoadState = QuizLoadState.LOADED,
                onNavigateReport = { onNavigateReport = true },
            )
        }

        // click report button
        composeTestRule.onNodeWithContentDescription("Report").performClick()
        // check if onNavigateReport is true
        assert(onNavigateReport)
    }

    @Test
    fun quizReportView_BackButtonClicked() {
        var onBack = false
        composeTestRule.setContent {
            QuizReportView(question = "question1", answer = "answer1", onBack = { onBack = true })
        }

        // click back button
        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()
        // check if onBack is true
        assert(onBack)
    }

    @Test
    fun quizReportView_ReportButtonClicked() {
        var reason = ""
        composeTestRule.setContent {
            QuizReportView(question = "question1", answer = "answer1", onReport = {
                reason = it
                Result.success(Unit)
            })
        }

        // click reason
        composeTestRule.onNodeWithText("It isn't true.").performClick()
        // click report button
        composeTestRule.onNodeWithText("Submit Feedback").performClick()
        // check if onBack is true
        assert(reason == "It isn't true.")
    }

    fun summaryView_OnLoadFailed() {
        var onBack = false
        composeTestRule.setContent {
            SummaryView(
                summary = "",
                viewerStyle = ViewerStyle(),
                typeface = Typeface.DEFAULT,
                referenceLineHeight = 16f,
                onLoadSummary = {
                    Result.failure(Exception())
                },
                onBack = {
                    onBack = true
                },
            )
        }

        assert(onBack)
    }

    @Test
    fun summaryView_Displayed() {
        composeTestRule.setContent {
            SummaryView(
                summary = "this is a summary",
                viewerStyle = ViewerStyle(),
                typeface = Typeface.DEFAULT,
                referenceLineHeight = 16f,
                onLoadSummary = { Result.success(Unit) },
            )
        }

        // check if summary is displayed
        composeTestRule.onNodeWithText("this is a summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Previous Story").assertIsDisplayed()
    }

    @Test
    fun summaryView_BackButtonClicked() {
        var onBack = false
        composeTestRule.setContent {
            SummaryView(
                summary = "this is a summary",
                onBack = { onBack = true },
                viewerStyle = ViewerStyle(),
                typeface = Typeface.DEFAULT,
                referenceLineHeight = 16f,
                onLoadSummary = { Result.success(Unit) },
            )
        }

        // click back button
        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()
        // check if onBack is true
        assert(onBack)
    }
}
