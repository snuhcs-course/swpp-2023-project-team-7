package com.example.readability.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readability.data.book.BookCardData
import com.example.readability.ui.screens.book.BookListView
import com.example.readability.ui.screens.book.BookScreen
import com.example.readability.ui.theme.ReadabilityTheme
import com.example.readability.ui.viewmodels.AddBookViewModel
import com.example.readability.ui.viewmodels.BookListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookListView_addBookButtonClicked() {
        var onAddBookCalled = false
        composeTestRule.setContent {
            ReadabilityTheme {
                BookListView(
                    bookCardDataList = emptyList(),
                    onNavigateAddBook = { onAddBookCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("Floating action button").performClick()
        assert(onAddBookCalled)
    }

    @Test
    fun bookListView_displayBookCardItems() {
        var loadImageCalled = 0
        var loadContentCalled = 0
        val bookCardDataList = listOf(
            BookCardData(
                id = 1,
                title = "Book 1",
                author = "Author 1",
                content = "",
                progress = 0.1,
                coverImage = "asd",
                summaryProgress = 0.1,
            ),
            BookCardData(
                id = 2,
                title = "Book 2",
                author = "Author 2",
                content = "",
                progress = 0.2,
                coverImage = "asd",
                summaryProgress = 0.1,
            ),
        )
        composeTestRule.setContent {
            ReadabilityTheme {
                BookListView(
                    bookCardDataList = bookCardDataList,
                    onNavigateAddBook = { },
                    onLoadImage = {
                        loadImageCalled++
                        Result.success(Unit)
                    },
                    onLoadContent = {
                        loadContentCalled++
                        Result.success(Unit)
                    },
                )
            }
        }

        for (bookCardData in bookCardDataList) {
            composeTestRule.onNodeWithText(bookCardData.title).assertExists()
            composeTestRule.onNodeWithText(bookCardData.author).assertExists()
            composeTestRule.onNodeWithText("${(bookCardData.progress * 100).toInt()}%").assertExists()
        }
        composeTestRule.waitUntil {
            loadImageCalled == bookCardDataList.size
        }
        // click on the first book card
        composeTestRule.onNodeWithText(bookCardDataList[0].title).performClick()
        // check if the content is loaded
        composeTestRule.waitUntil {
            loadContentCalled == 1
        }
    }

//    @Test
//    fun addBookView_selectImageClicked() {
//        var onSelectImageCalled = false
//        composeTestRule.setContent {
//            ReadabilityTheme {
//                AddBookView(
//                    onBack = {},
//                    onBookUploaded = {},
//                    onAddBookClicked = { kotlin.Result.success(Unit) },
//                )
//            }
//        }
//
//        composeTestRule.onNodeWithText("Select image").performClick()
//
//    }
//
//    @Test
//    fun addBookView_addBookClicked() {
//        var onAddBookClicked = false
//        composeTestRule.setContent {
//            ReadabilityTheme {
//                AddBookView(
//                    onBack = {},
//                    onBookUploaded = {},
//                    onAddBookClicked = { onAddBookClicked = true
//                        kotlin.Result.success(Unit)}
//                )
//            }
//        }
//
//        composeTestRule.onNodeWithText("Add book").performClick()
//        assert(onAddBookClicked)
//    }

    @Test
    fun bookScreen_isDisplayed() = runTest {
        val bookListViewModel = mockk<BookListViewModel>()
        val addBookViewModel = mockk<AddBookViewModel>()
        every { bookListViewModel.bookCardDataList } returns MutableStateFlow(
            listOf(
                BookCardData(
                    id = 13,
                    title = "Book 1",
                    author = "Author 1",
                    content = "asdf",
                    progress = 0.1,
                    coverImage = "asd",
                ),
            ),
        )
        coEvery { bookListViewModel.getCoverImageData(any()) } returns Result.success(Unit)
        coEvery { bookListViewModel.getContentData(any()) } returns Result.success(Unit)

        composeTestRule.setContent {
            ReadabilityTheme {
                BookScreen(
                    bookListViewModel = bookListViewModel,
                    addBookViewModel = addBookViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("Book 1").assertExists()
        coVerify(timeout = 2500L) { bookListViewModel.getCoverImageData(13) }
    }

    @Test
    fun bookScreen_navigateToView() = runTest {
        val bookListViewModel = mockk<BookListViewModel>()
        val addBookViewModel = mockk<AddBookViewModel>()
        every { bookListViewModel.bookCardDataList } returns MutableStateFlow(
            listOf(
                BookCardData(
                    id = 13,
                    title = "Book 1",
                    author = "Author 1",
                    content = "asdf",
                    progress = 0.1,
                    coverImage = "asd",
                ),
            ),
        )
        coEvery { bookListViewModel.getCoverImageData(any()) } returns Result.success(Unit)
        coEvery { bookListViewModel.getContentData(any()) } returns Result.success(Unit)
        var onNavigateViewerCalled = false

        composeTestRule.setContent {
            ReadabilityTheme {
                BookScreen(
                    bookListViewModel = bookListViewModel,
                    addBookViewModel = addBookViewModel,
                    onNavigateViewer = {
                        onNavigateViewerCalled = true
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Book 1").assertExists()
        coVerify(timeout = 2500L) { bookListViewModel.getCoverImageData(13) }
        composeTestRule.onNodeWithText("Book 1").performClick()
        coVerify(timeout = 2500L) { bookListViewModel.getContentData(13) }
        composeTestRule.waitUntil {
            onNavigateViewerCalled
        }
    }
}
