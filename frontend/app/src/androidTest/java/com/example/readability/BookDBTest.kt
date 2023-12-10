package com.example.readability

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readability.data.book.AddBookRequest
import com.example.readability.data.book.BookRepository
import com.example.readability.data.user.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.abs
import kotlin.time.Duration

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
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

    fun getFileFromResources(fileName: String): InputStream {
        return javaClass.classLoader?.getResource(fileName)?.openStream() ?: throw Exception("File not found")
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

    suspend fun assertUntil(time: Long, block: () -> Boolean) {
        val startTime = System.currentTimeMillis()
        while (startTime + time > System.currentTimeMillis() && !block()) {
            delay(100L)
        }
        assert(block())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun addBook_success() = runTest(timeout = Duration.parse("60s")) {
        // Arrange
        val email = "dbtesting@test.com"
        val username = "testuser"
        val password = "testpassword"
        val content = getFileFromResources("the_open_boat.txt").use {
            it.readBytes().decodeToString()
        }
        val coverImageBytes = getFileFromResources("the_open_boat.jpg").use {
            it.readBytes()
        }
        val coverImage = coverImageBytes.toHexString(HexFormat.Default)
        val coverImageBitmap =
            BitmapFactory.decodeByteArray(coverImageBytes, 0, coverImageBytes.size)
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
        assertTrue("SignIn should succeed", signInResult.isSuccess)
        assertTrue("Book add should succeed", bookAddResult.isSuccess)

        val bookRefreshResult = withContext(Dispatchers.IO) { bookRepository.refreshBookList() }
        assertTrue("Book refresh should succeed", bookRefreshResult.isSuccess)
        assertUntil(5000L) { bookList.value.isNotEmpty() && bookList.value.any { it.title == bookTitle } }

        // get content and image
        var book = bookList.value.first { it.title == bookTitle }
        val contentResult = withContext(Dispatchers.IO) { bookRepository.getContentData(book.bookId) }
        val imageResult = withContext(Dispatchers.IO) { bookRepository.getCoverImageData(book.bookId) }

        assertTrue("Get content should succeed", contentResult.isSuccess)
        assertTrue("Get image should succeed", imageResult.isSuccess)
        assertUntil(5000L) {
            book = bookList.value.first { it.title == bookTitle }
            book.contentData != null && book.coverImageData != null
        }
        assertTrue("Book content should be the same as the added content", book.contentData == content)
        assertTrue(
            "Book cover image should be the same as the added image",
            compareBitmaps(book.coverImageData!!.asAndroidBitmap(), coverImageBitmap),
        )

        // clean up stateflow
        bookListScope.cancel()
    }
}
