package com.example.readability.screens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
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
import com.example.readability.ui.PageSplitter
import com.example.readability.ui.models.BookData
import com.example.readability.ui.models.BookModel
import com.example.readability.ui.models.Quiz
import com.example.readability.ui.models.QuizLoadState
import com.example.readability.ui.screens.viewer.QuizReportView
import com.example.readability.ui.screens.viewer.QuizView
import com.example.readability.ui.screens.viewer.SummaryView
import com.example.readability.ui.screens.viewer.ViewerView
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewerScreenTest {
    companion object {

        lateinit var openBoatBookData: BookData
        val pageSplitter = PageSplitter()

        @JvmStatic
        @BeforeClass
        fun initOpenBoatBookData() {
            val content = javaClass.classLoader!!.getResource("the_open_boat.txt")!!.readText()
            openBoatBookData = BookData(
                id = "1",
                title = "The Open Boat",
                author = "Stephen Crane",
                content = content,
                coverImage = "",
                progress = 0.0,
                pageSplitData = null
            )
            // wait until page cache is built
            runBlocking {
                pageSplitter.splitPage(
                    300, 300, openBoatBookData
                ) {
                    openBoatBookData = openBoatBookData.copy(pageSplitData = it)
                }
            }
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    fun getPageIndex(bookData: BookData): Int {
        return maxOf(
            minOf(
                (bookData.pageSplitData!!.pageSplits.size * bookData.progress).toInt(),
                bookData.pageSplitData!!.pageSplits.size - 1
            ), 0
        )
    }

    @Test
    fun viewerView_LeftClick() {
        var width = 0f
        var height = 0f
        openBoatBookData =
            openBoatBookData.copy(progress = (1.5 / openBoatBookData.pageSplitData!!.pageSplits.size))
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onProgressChange = {
                    openBoatBookData = openBoatBookData.copy(progress = it)
                },
                pageSplitter = BookModel.getInstance().pageSplitter
            )
            with(LocalDensity.current) {
                width = LocalConfiguration.current.screenWidthDp.dp.toPx()
                height = LocalConfiguration.current.screenHeightDp.dp.toPx()
            }
        }
        println("page: ${getPageIndex(openBoatBookData)}")
        println("click: ${width * 0.1f}, ${height * 0.5f}")
        // click left side of screen
        composeTestRule.onRoot().performTouchInput {
            down(Offset(width * 0.1f, height * 0.5f))
            up()
        }
        // check if progress is decreased by one page
        println("page: ${getPageIndex(openBoatBookData)}")
        assert(getPageIndex(openBoatBookData) == 0)
    }

    @Test
    fun viewerView_RightClick() {
        var width = 0f
        var height = 0f
        openBoatBookData = openBoatBookData.copy(progress = 0.0)
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onProgressChange = {
                    openBoatBookData = openBoatBookData.copy(progress = it)
                },
                pageSplitter = BookModel.getInstance().pageSplitter
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
        assert(getPageIndex(openBoatBookData) == 1)
    }

    @Test
    fun viewerView_CenterClick() {
        var width = 0f
        var height = 0f
        openBoatBookData = openBoatBookData.copy(progress = 0.0)
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                pageSplitter = BookModel.getInstance().pageSplitter
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
        openBoatBookData = openBoatBookData.copy(progress = 0.0)
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onProgressChange = {
                    openBoatBookData = openBoatBookData.copy(progress = it)
                },
                pageSplitter = BookModel.getInstance().pageSplitter,
            )
        }
        // swipe left
        composeTestRule.onRoot().performTouchInput {
            swipeLeft()
        }
        // check if progress is increased by one page
        assert(getPageIndex(openBoatBookData) == 1)
    }

    @Test
    fun viewerView_RightSwipe() {
        openBoatBookData =
            openBoatBookData.copy(progress = (1.5 / openBoatBookData.pageSplitData!!.pageSplits.size))
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onProgressChange = {
                    openBoatBookData = openBoatBookData.copy(progress = it)
                },
                pageSplitter = BookModel.getInstance().pageSplitter,
            )
        }
        // swipe right
        composeTestRule.onRoot().performTouchInput {
            swipeRight()
        }
        // check if progress is decreased by one page
        assert(getPageIndex(openBoatBookData) == 0)
    }

    @Test
    fun viewerView_BackButtonClicked() {
        var width = 0f
        var height = 0f
        var onBack = false
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onBack = {
                    onBack = true
                },
                pageSplitter = BookModel.getInstance().pageSplitter,
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
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onNavigateSettings = {
                    onNavigateSettings = true
                },
                pageSplitter = BookModel.getInstance().pageSplitter,
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

    @Test
    fun viewerView_AIButtonClicked() {
        var width = 0f
        var height = 0f
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                pageSplitter = BookModel.getInstance().pageSplitter,
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
        // click AI button
        composeTestRule.onNodeWithContentDescription("AI Button").performClick()
        // check if buttons are displayed
        composeTestRule.onNodeWithText("Generate Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generate Quiz").assertIsDisplayed()
    }

    @Test
    fun viewerView_GenerateSummaryClicked() {
        var width = 0f
        var height = 0f
        var onNavigateSummary = false
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onNavigateSummary = {
                    onNavigateSummary = true
                },
                pageSplitter = BookModel.getInstance().pageSplitter,
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
        // click AI button
        composeTestRule.onNodeWithContentDescription("AI Button").performClick()
        // click generate summary
        composeTestRule.onNodeWithText("Generate Summary").performClick()
        // check if onNavigateSummary is true
        assert(onNavigateSummary)
    }

    @Test
    fun viewerView_GenerateQuizClicked() {
        var width = 0f
        var height = 0f
        var onNavigateQuiz = false
        composeTestRule.setContent {
            ViewerView(
                bookData = openBoatBookData,
                pageSize = openBoatBookData.pageSplitData!!.pageSplits.size,
                onNavigateQuiz = {
                    onNavigateQuiz = true
                },
                pageSplitter = BookModel.getInstance().pageSplitter,
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
        // click AI button
        composeTestRule.onNodeWithContentDescription("AI Button").performClick()
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
                    Quiz(id = "1", question = "question1", answer = "answer1"),
                ),
                quizSize = 1,
                quizLoadState = QuizLoadState.LOADED,
            )
        }

        // check if quiz is displayed and answer is not displayed
        composeTestRule.onNodeWithText("question1").assertIsDisplayed()
        composeTestRule.onNodeWithText("answer1").assertIsNotDisplayed()
        // check is answer is displayed when Show Answer is clicked
        composeTestRule.onNodeWithText("See Answer").performClick()
        composeTestRule.onNodeWithText("answer1").assertIsDisplayed()
    }

    @Test
    fun quizView_BackButtonClicked() {
        var onBack = false
        composeTestRule.setContent {
            QuizView(
                quizList = listOf(
                    Quiz(id = "1", question = "question1", answer = "answer1"),
                ),
                quizSize = 1,
                quizLoadState = QuizLoadState.LOADED,
                onBack = { onBack = true },
            )
        }

        // click back button
        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()
        // check if onBack is true
        assert(onBack)
    }

    @Test
    fun quizView_ReportButtonClicked() {
        var onNavigateReport = false
        composeTestRule.setContent {
            QuizView(
                quizList = listOf(
                    Quiz(id = "1", question = "question1", answer = "answer1"),
                ),
                quizSize = 1,
                quizLoadState = QuizLoadState.LOADED,
                onNavigateReport = { onNavigateReport = true },
            )
        }

        // click report button
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("Report This Quiz").performClick()
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

    @Test
    fun summaryView_Displayed() {
        composeTestRule.setContent {
            SummaryView(summary = "this is a summary")
        }

        // check if summary is displayed
        composeTestRule.onNodeWithText("this is a summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Previous Story").assertIsDisplayed()
    }

    @Test
    fun summaryView_BackButtonClicked() {
        var onBack = false
        composeTestRule.setContent {
            SummaryView(summary = "this is a summary", onBack = { onBack = true })
        }

        // click back button
        composeTestRule.onNodeWithContentDescription("Arrow Back").performClick()
        // check if onBack is true
        assert(onBack)
    }
}