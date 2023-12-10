package com.example.readability.ui.models.viewer

import androidx.compose.ui.graphics.NativeCanvas
import com.example.readability.data.book.Book
import com.example.readability.data.book.BookRepository
import com.example.readability.data.viewer.FontDataSource
import com.example.readability.data.viewer.PageSplitDataSource
import com.example.readability.data.viewer.PageSplitRepository
import com.example.readability.data.viewer.SettingRepository
import com.example.readability.data.viewer.ViewerStyle
import com.example.readability.data.viewer.ViewerStyleBuilder
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class PageSplitRepositoryTest {

    private lateinit var pageSplitRepository: PageSplitRepository
    private lateinit var pageSplitDataSource: PageSplitDataSource
    private lateinit var fontDataSource: FontDataSource
    private lateinit var settingRepository: SettingRepository
    private lateinit var bookRepository: BookRepository

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        pageSplitDataSource = mockk(relaxed = true)
        fontDataSource = mockk(relaxed = true)
        settingRepository = mockk(relaxed = true)
        bookRepository = mockk(relaxed = true)

        coEvery { settingRepository.viewerStyle } returns MutableStateFlow(ViewerStyle())
        coEvery { bookRepository.getBook(any()) } returns MutableStateFlow(
            Book(
                bookId = 1,
                title = "title",
                author = "author",
                content = "content",
                contentData = "content",
                coverImage = null,
                progress = 0.7,
            )
        )
        pageSplitRepository = PageSplitRepository(
            pageSplitDataSource,
            fontDataSource,
            settingRepository,
            bookRepository
        )
        coEvery { pageSplitDataSource.splitPage(any(), any(), any(), any(), any(), any()) } returns listOf(
            0,
            3,
            5
        ).toIntArray()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSplitData returns PageSplitData`() = runTest {
        // Arrange
        val bookId = 1
        val width = 500
        val height = 800
        val viewerStyle = ViewerStyle()
        val charWidths = FloatArray(65536) { 16f }

        // Act
        val result = pageSplitRepository.getSplitData(bookId, width, height)

        // Assert
        assertNotNull(result)
        assertEquals(width, result!!.width)
        assertEquals(height, result.height)
        assertEquals(viewerStyle, result.viewerStyle)
    }

    @Test
    fun `drawPage calls drawPage on PageSplitDataSource`() = runTest {
        // Arrange
        val canvas = mockk<NativeCanvas>(relaxed = true)
        val bookId = 1
        val page = 0
        val isDarkMode = false
        val width = 500
        val height = 800
        val viewerStyle = ViewerStyle()
        val charWidths = FloatArray(65536) { 16f }

        // Act
        pageSplitRepository.getSplitData(bookId, width, height)
        pageSplitRepository.drawPage(canvas, bookId, page, isDarkMode)

        // Assert
        verify(exactly = 1) { pageSplitDataSource.drawPage(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `drawPageRaw calls drawPage on PageSplitDataSource`() {
        // Arrange
        val canvas = mockk<NativeCanvas>(relaxed = true)
        val pageContent = "Lorem ipsum dolor sit amet."
        val viewerStyle = ViewerStyleBuilder().build()
        val width = 500
        val isDarkMode = false

        // Act
        pageSplitRepository.drawPageRaw(canvas, pageContent, viewerStyle, width, isDarkMode)

        // Assert
        verify(exactly = 1) { pageSplitDataSource.drawPage(any(), any(), any(), any(), any(), any()) }
    }
}
