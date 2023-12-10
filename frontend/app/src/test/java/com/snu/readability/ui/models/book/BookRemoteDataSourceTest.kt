package com.snu.readability.ui.models.book

import android.graphics.Bitmap
import com.snu.readability.data.book.AddBookRequest
import com.snu.readability.data.book.BookAPI
import com.snu.readability.data.book.BookRemoteDataSource
import com.snu.readability.data.book.BookResponse
import com.snu.readability.data.book.BooksResponse
import com.snu.readability.data.book.SummaryProgressResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import java.io.ByteArrayOutputStream

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class BookRemoteDataSourceTest {

    private lateinit var bookAPI: BookAPI
    private lateinit var bookRemoteDataSource: BookRemoteDataSource

    @Before
    fun setup() {
        bookAPI = mockk(relaxed = true)
        bookRemoteDataSource = BookRemoteDataSource(bookAPI)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getBookList success should return a list of books`() = runTest {
        // Arrange
        coEvery { bookAPI.getBooks(any()).execute() } returns Response.success(
            BooksResponse(
                listOf(
                    BookResponse(
                        1,
                        "Title1",
                        "Author1",
                        "Content1",
                        "Cover1",
                        0.5,
                    ),
                    BookResponse(
                        2,
                        "Title2",
                        "Author2",
                        "Content2",
                        "Cover2",
                        0.8,
                    ),
                ),
            ),
        )

        // Act
        val result = bookRemoteDataSource.getBookList("accessToken")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() != null)
        assert(result.getOrNull()?.size == 2)
        assert(result.getOrNull()?.get(0)?.bookId == 1)
        assert(result.getOrNull()?.get(1)?.bookId == 2)
        coVerify { bookAPI.getBooks("accessToken") }
    }

    @Test
    fun `getBookList failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.getBooks(any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.getBookList("accessToken")

        // Assert
        assert(result.isFailure)
        coVerify { bookAPI.getBooks("accessToken") }
    }

    @Test
    fun `getCoverImageData success should return an ImageBitmap`() = runTest {
        // Arrange
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        coEvery { bookAPI.getBookCoverImage(any(), any()).execute() } returns Response.success(
            byteArrayOutputStream.toByteArray().toResponseBody("image/jpeg".toMediaTypeOrNull()),
        )

        // Act
        val result = bookRemoteDataSource.getCoverImageData("accessToken", "coverImage")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() != null)
        coVerify { bookAPI.getBookCoverImage("coverImage", "accessToken") }

        bitmap.recycle()
        byteArrayOutputStream.close()
    }

    @Test
    fun `getCoverImageData failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.getBookCoverImage(any(), any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.getCoverImageData("accessToken", "coverImage")

        // Assert
        assert(result.isFailure)
        coVerify { bookAPI.getBookCoverImage("coverImage", "accessToken") }
    }

    @Test
    fun `getContentData success should return content string`() = runTest {
        // Arrange
        coEvery {
            bookAPI.getBookContent(any(), any()).execute()
        } returns Response.success("Content".toResponseBody("text/plain".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.getContentData("accessToken", "content")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == "Content")
        coVerify { bookAPI.getBookContent("content", "accessToken") }
    }

    @Test
    fun `getContentData failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.getBookContent(any(), any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.getContentData("accessToken", "content")

        // Assert
        assert(result.isFailure)
        coVerify { bookAPI.getBookContent("content", "accessToken") }
    }

    @Test
    fun `getSummaryProgress success should return summary progress string`() = runTest {
        // Arrange
        coEvery { bookAPI.getSummaryProgress(any(), any()).execute() } returns Response.success(
            SummaryProgressResponse(
                "0.7",
            ),
        )

        // Act
        val result = bookRemoteDataSource.getSummaryProgress("accessToken", 1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == "0.7")
        coVerify { bookAPI.getSummaryProgress(1, "accessToken") }
    }

    @Test
    fun `getSummaryProgress failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.getSummaryProgress(any(), any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.getSummaryProgress("accessToken", 1)

        // Assert
        assert(result.isFailure)
        coVerify { bookAPI.getSummaryProgress(1, "accessToken") }
    }

    @Test
    fun `addBook success should return Unit`() = runTest {
        // Arrange
        coEvery { bookAPI.addBook(any(), any()).execute() } returns Response.success(Unit)

        // Act
        val result = bookRemoteDataSource.addBook(
            "accessToken",
            AddBookRequest("Title", "Content", "Author", "Cover"),
        )

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == Unit)
        coVerify {
            bookAPI.addBook(
                "accessToken",
                AddBookRequest("Title", "Content", "Author", "Cover"),
            )
        }
    }

    @Test
    fun `addBook failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.addBook(any(), any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.addBook(
            "accessToken",
            AddBookRequest("Title", "Content", "Author", "Cover"),
        )

        // Assert
        assert(result.isFailure)
        coVerify {
            bookAPI.addBook(
                "accessToken",
                AddBookRequest("Title", "Content", "Author", "Cover"),
            )
        }
    }

    @Test
    fun `deleteBook success should return success message`() = runTest {
        // Arrange
        coEvery {
            bookAPI.deleteBook(any(), any()).execute()
        } returns Response.success("Success".toResponseBody("text/plain".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.deleteBook(1, "accessToken")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == "Success")
        coVerify { bookAPI.deleteBook(1, "accessToken") }
    }

    @Test
    fun `deleteBook failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.deleteBook(any(), any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.deleteBook(1, "accessToken")

        // Assert
        assert(result.isFailure)
        coVerify { bookAPI.deleteBook(1, "accessToken") }
    }

    @Test
    fun `updateProgress success should return success message`() = runTest {
        // Arrange
        coEvery {
            bookAPI.updateProgress(any(), any(), any()).execute()
        } returns Response.success("Success".toResponseBody("text/plain".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.updateProgress(1, 0.5, "accessToken")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == "Success")
        coVerify { bookAPI.updateProgress(1, 0.5, "accessToken") }
    }

    @Test
    fun `updateProgress failure should return a failure result`() = runTest {
        // Arrange
        coEvery {
            bookAPI.updateProgress(any(), any(), any()).execute()
        } returns Response.error(400, "".toResponseBody("application/json".toMediaTypeOrNull()))

        // Act
        val result = bookRemoteDataSource.updateProgress(1, 0.5, "accessToken")

        // Assert
        assert(result.isFailure)
        coVerify { bookAPI.updateProgress(1, 0.5, "accessToken") }
    }
}
