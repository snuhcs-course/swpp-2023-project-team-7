package com.example.readability.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.readability.data.book.AddBookRequest
import com.example.readability.data.book.BookRepository
import com.example.readability.data.user.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import javax.inject.Inject
import kotlin.math.abs
import kotlin.time.Duration

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class BookDBTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var bookRepository: BookRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    fun getFileFromResources(fileName: String): File {
        return File(
            javaClass.classLoader?.getResource(fileName)?.toURI()
                ?: throw Exception("File not found"),
        )
    }

    fun compareBitmaps(bitmap1: Bitmap, bitmap2: Bitmap): Boolean {
        if (bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height) {
            return false
        }
        var diff = 0
        for (x in 0 until bitmap1.width) {
            for (y in 0 until bitmap1.height) {
                diff += abs(bitmap1.getPixel(x, y) - bitmap2.getPixel(x, y))
            }
        }
        return (diff / (bitmap1.width * bitmap1.height)) < 5
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun addBook_success() = runTest(timeout = Duration.parse("60s")) {
        // Arrange
        val email = "dbtesting@test.com"
        val username = "testuser"
        val password = "testpassword"
        val content = getFileFromResources("the_open_boat.txt").readText(Charsets.UTF_8)
        val coverImageFile = getFileFromResources("the_open_boat.jpg")
        val coverImage = coverImageFile.readBytes().toHexString(HexFormat.Default)
        val coverImageBitmap =
            BitmapFactory.decodeByteArray(coverImageFile.readBytes(), 0, coverImageFile.readBytes().size)
        val bookTitle = "testbook_${System.currentTimeMillis()}"
        val bookListScope = CoroutineScope(Dispatchers.Default)
        val bookList = bookRepository.bookList.stateIn(bookListScope)

        // Act
        withContext(Dispatchers.IO) { userRepository.signUp(email, username, password) }
        val signInResult = withContext(Dispatchers.IO) { userRepository.signIn(email, password) }
        val bookAddResult = withContext(Dispatchers.IO) {
            bookRepository.addBook(
                AddBookRequest(
                    title = bookTitle,
                    author = "testauthor",
                    content = content,
                    coverImage = coverImage,
                ),
            )
        }
        Assertions.assertTrue(signInResult.isSuccess, "SignIn should succeed")
        Assertions.assertTrue(bookAddResult.isSuccess, "Book add should succeed")

        val bookRefreshResult = withContext(Dispatchers.IO) { bookRepository.refreshBookList() }
        Assertions.assertTrue(bookRefreshResult.isSuccess, "Book refresh should succeed")
        await().timeout(java.time.Duration.ofMillis(5000L)).until {
            bookList.value.isNotEmpty() && bookList.value.any { it.title == bookTitle }
        }

        // get content and image
        var book = bookList.value.first { it.title == bookTitle }
        val contentResult = withContext(Dispatchers.IO) { bookRepository.getContentData(book.bookId) }
        val imageResult = withContext(Dispatchers.IO) { bookRepository.getCoverImageData(book.bookId) }

        Assertions.assertTrue(contentResult.isSuccess, "Get content should succeed")
        Assertions.assertTrue(imageResult.isSuccess, "Get image should succeed")
        await().timeout(java.time.Duration.ofMillis(5000L)).until {
            book = bookList.value.first { it.title == bookTitle }
            book.contentData != null && book.coverImageData != null
        }
        Assertions.assertTrue(book.contentData == content, "Book content should be the same as the added content")
        Assertions.assertTrue(
            compareBitmaps(book.coverImageData!!.asAndroidBitmap(), coverImageBitmap),
            "Book cover image should be the same as the added image",
        )

        // clean up stateflow
        bookListScope.cancel()
    }
}
