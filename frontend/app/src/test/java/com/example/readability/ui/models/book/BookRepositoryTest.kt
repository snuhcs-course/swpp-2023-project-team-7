package com.example.readability.ui.models.book

import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.book.AddBookRequest
import com.example.readability.data.book.Book
import com.example.readability.data.book.BookDao
import com.example.readability.data.book.BookEntity
import com.example.readability.data.book.BookFileDataSource
import com.example.readability.data.book.BookRemoteDataSource
import com.example.readability.data.book.BookRepository
import com.example.readability.data.user.UserRepository
import io.mockk.coEvery
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class BookRepositoryTest {
    @JvmField
    @Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    // classes to be mocked
    @Mock
    private lateinit var bookDao: BookDao

    @Mock
    private lateinit var bookRemoteDataSource: BookRemoteDataSource

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var networkStatusRepository: NetworkStatusRepository

    @Mock
    private lateinit var bookFileDataSource: BookFileDataSource

    // class under test
    private lateinit var bookRepository: BookRepository

    @Before
    fun init() = runBlocking {
        `when`(bookDao.getAll()).thenReturn(
            listOf(
                BookEntity(
                    bookId = 1,
                    title = "test",
                    author = "test",
                    progress = 0.0,
                    coverImage = null,
                    content = "test",
                    summaryProgress = 0.5,
                ),
                BookEntity(
                    bookId = 2,
                    title = "test",
                    author = "test",
                    progress = 0.7,
                    coverImage = "test",
                    content = "test",
                    summaryProgress = 1.0,
                ),
                BookEntity(
                    bookId = 3,
                    title = "test",
                    author = "test",
                    progress = 0.7,
                    coverImage = "test",
                    content = "test",
                    summaryProgress = 1.0,
                ),
            ),
        )
        `when`(bookRemoteDataSource.getBookList("test")).thenReturn(
            Result.success(
                listOf(
                    Book(
                        bookId = 1,
                        title = "test",
                        author = "test",
                        progress = 0.0,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 0.5,
                    ),
                    Book(
                        bookId = 2,
                        title = "test",
                        author = "test",
                        progress = 0.7,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                ),
            ),
        )
        `when`(networkStatusRepository.isConnected).thenReturn(true)
        `when`(userRepository.getAccessToken()).thenReturn("test")
        bookRepository = BookRepository(
            bookDao = bookDao,
            bookRemoteDataSource = bookRemoteDataSource,
            userRepository = userRepository,
            networkStatusRepository = networkStatusRepository,
            bookFileDataSource = bookFileDataSource,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `setProgress success`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        doNothing().`when`(bookDao).updateProgress(bookId, progress)

        // Act
        bookRepository.updateProgress(bookId, progress)

        // Assert
        // updateProgress should be called
        verify(bookDao, times(1)).updateProgress(bookId, progress)
        // bookMap should be updated
        assert(bookRepository.getBook(bookId).firstOrNull()?.progress == progress)
    }

    @Test
    fun `updateProgressSummary success`() = runTest {
        // Arrange
        val bookId = 1
        val summaryProgress = 0.5
        `when`(
            bookRemoteDataSource.getSummaryProgress(
                "test",
                bookId,
            ),
        ).thenReturn(Result.success(summaryProgress.toString()))
        doNothing().`when`(bookDao).updateSummaryProgress(bookId, summaryProgress)

        // Act
        bookRepository.updateSummaryProgress(bookId)

        // Assert
        // bookMap should be updated
        assert(bookRepository.getBook(bookId).firstOrNull()?.summaryProgress == summaryProgress)
    }

    @Test
    fun `updateSummaryProgress when server error should return failure`() = runTest {
        // Arrange
        val bookId = 1
        `when`(bookRemoteDataSource.getSummaryProgress("test", bookId)).thenReturn(Result.failure(Throwable("test")))

        // Act
        val result = bookRepository.updateSummaryProgress(bookId)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `getBook success`() = runTest {
        // Arrange
        val bookId = 1
        `when`(bookDao.getBook(bookId)).thenReturn(
            BookEntity(
                bookId = bookId,
                title = "test",
                author = "test",
                progress = 0.0,
                coverImage = "test",
                content = "test",
                summaryProgress = 0.5,
            ),
        )

        // Act
        val book = bookRepository.getBook(bookId).firstOrNull()

        // Assert
        // book should be returned
        assert(book != null)
    }

    @Test
    fun `getBook fail`() = runTest {
        // Arrange
        val bookId = 10
        `when`(bookDao.getBook(bookId)).thenReturn(null)

        // Act
        val book = bookRepository.getBook(bookId).firstOrNull()

        // Assert
        // book should be null
        assert(book == null)
    }

    @Test
    fun `refreshBookList success should update book list`() = runTest {
        // Arrange
        val deletedBookId = 1
        val updatedProgressBookId = 2
        val insertedBookId = 4
        val updatedProgress = 0.6
        val insertedBook =
            BookEntity(
                bookId = insertedBookId,
                title = "test",
                author = "test",
                progress = 0.7,
                coverImage = "test",
                content = "test",
                summaryProgress = 1.0,
            )

        doNothing().`when`(bookDao).insert(insertedBook)
        doNothing().`when`(bookDao).updateProgress(updatedProgressBookId, updatedProgress)
        doNothing().`when`(bookDao).updateProgress(3, 0.7)

        `when`(bookRemoteDataSource.getBookList("test")).thenReturn(
            Result.success(
                listOf(
                    Book(
                        bookId = 2,
                        title = "test",
                        author = "test",
                        progress = updatedProgress,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                    Book(
                        bookId = 3,
                        title = "test",
                        author = "test",
                        progress = 0.7,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                    Book(
                        bookId = insertedBookId,
                        title = "test",
                        author = "test",
                        progress = 0.7,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                ),
            ),
        )
        `when`(bookDao.getBook(1)).thenReturn(
            BookEntity(
                bookId = insertedBookId,
                title = "test",
                author = "test",
                progress = 0.0,
                coverImage = "test",
                content = "test",
                summaryProgress = 0.5,
            ),
        )
        `when`(bookDao.getBook(2)).thenReturn(
            BookEntity(
                bookId = 2,
                title = "test",
                author = "test",
                progress = 0.7,
                coverImage = "test",
                content = "test",
                summaryProgress = 1.0,
            ),
        )
        `when`(bookDao.getBook(3)).thenReturn(
            BookEntity(
                bookId = 3,
                title = "test",
                author = "test",
                progress = 0.7,
                coverImage = "test",
                content = "test",
                summaryProgress = 1.0,
            ),
        )
        `when`(bookDao.getBook(4)).thenReturn(
            null,
        )

        // Act
        bookRepository.refreshBookList()

        // Assert
        verify(bookDao, times(1)).insert(insertedBook)
        verify(bookDao, times(1)).updateProgress(updatedProgressBookId, updatedProgress)
        assert(bookRepository.getBook(deletedBookId).firstOrNull() == null)
        assert(bookRepository.getBook(updatedProgressBookId).firstOrNull()?.progress == updatedProgress)
        assert(bookRepository.getBook(insertedBookId).firstOrNull() != null)
        assert(bookRepository.bookList.firstOrNull()?.size == 3)
    }

    @Test
    fun `refreshBookList when no network should return failure`() = runTest {
        // Arrange
        `when`(networkStatusRepository.isConnected).thenReturn(false)

        // Act
        val result = bookRepository.refreshBookList()

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `refreshBookList when server error should return failure`() = runTest {
        // Arrange
        `when`(bookRemoteDataSource.getBookList("test")).thenReturn(Result.failure(Throwable("test")))

        // Act
        val result = bookRepository.refreshBookList()

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `getCoverImageData success`() = runTest {
        // Arrange
        val bookId = 2
        val imageBitmap = null

        `when`(bookFileDataSource.coverImageExists(bookId)).thenReturn(true)
        `when`(bookFileDataSource.readCoverImageFile(bookId)).thenReturn(imageBitmap)

        // Act
        val result = bookRepository.getCoverImageData(bookId)

        // Assert
        assert(result.isSuccess)
    }

    @Test
    fun `getCoverImageData fails when server error`() = runTest {
        // Arrange
        val bookId = 2
        val coverImage = "test"

        `when`(bookFileDataSource.coverImageExists(bookId)).thenReturn(false)
        `when`(bookRemoteDataSource.getCoverImageData("test", coverImage)).thenReturn(Result.failure(Throwable("test")))

        // Act
        val result = bookRepository.getCoverImageData(bookId)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `getCoverImageData fails`() = runTest {
        // Arrange
        val bookId = 1
        `when`(bookFileDataSource.coverImageExists(bookId)).thenReturn(false)

        // Act
        val result = bookRepository.getCoverImageData(bookId)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `getContentData success when local data exists`() = runTest {
        // Arrange
        val bookId = 1
        val contentString = "test_string"

        `when`(bookFileDataSource.contentExists(bookId)).thenReturn(true)
        `when`(bookFileDataSource.readContentFile(bookId)).thenReturn(contentString)

        // Act
        val result = bookRepository.getContentData(bookId)

        // Assert
        assert(result.isSuccess)
    }

    @Test
    fun `getContentData fail`() = runTest {
        // Arrange
        val bookId = 1
        val content = "test"

        `when`(bookFileDataSource.contentExists(bookId)).thenReturn(false)
        `when`(bookRemoteDataSource.getContentData("test", content)).thenReturn(Result.failure(Throwable("test")))

        // Act
        val result = bookRepository.getContentData(bookId)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `getContenData success`() = runTest {
        // Arrange
        val bookId = 1
        val content = "test"
        val contentString = "test_string"

        `when`(bookFileDataSource.contentExists(bookId)).thenReturn(false)
        `when`(bookRemoteDataSource.getContentData("test", content)).thenReturn(Result.success(contentString))
        doNothing().`when`(bookFileDataSource).writeContentFile(bookId, contentString)

        // Act
        val result = bookRepository.getContentData(bookId)

        // Assert
        assert(result.isSuccess)
    }

    @Test
    fun `addBook success should add book data`() = runTest {
        // Arrange
        val addBookRequest = AddBookRequest(
            title = "test",
            content = "test",
            author = "test",
            coverImage = "",
        )
        val insertedBookEntity =
            BookEntity(
                bookId = 4,
                title = "test",
                author = "test",
                progress = 0.0,
                coverImage = "test",
                content = "test",
                summaryProgress = 1.0,
            )

        `when`(bookDao.getAll()).thenReturn(
            listOf(
                BookEntity(
                    bookId = 2,
                    title = "test",
                    author = "test",
                    progress = 0.7,
                    coverImage = "test",
                    content = "test",
                    summaryProgress = 1.0,
                ),
                BookEntity(
                    bookId = 3,
                    title = "test",
                    author = "test",
                    progress = 0.7,
                    coverImage = "test",
                    content = "test",
                    summaryProgress = 1.0,
                ),
            ),
        )
        `when`(bookRemoteDataSource.getBookList("test")).thenReturn(
            Result.success(
                listOf(
                    Book(
                        bookId = 2,
                        title = "test",
                        author = "test",
                        progress = 0.7,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                    Book(
                        bookId = 3,
                        title = "test",
                        author = "test",
                        progress = 0.7,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                    Book(
                        bookId = 4,
                        title = "test",
                        author = "test",
                        progress = 0.0,
                        coverImage = "test",
                        content = "test",
                        summaryProgress = 1.0,
                    ),
                ),
            ),
        )

        `when`(bookRemoteDataSource.addBook("test", addBookRequest)).thenReturn(Result.success(Unit))
        doNothing().`when`(bookDao).insert(insertedBookEntity)
        doNothing().`when`(bookDao).updateProgress(2, 0.7)
        doNothing().`when`(bookDao).updateProgress(3, 0.7)

        // Act
        val result = bookRepository.addBook(addBookRequest)

        // Assert
        verify(bookDao, times(1)).insert(insertedBookEntity)
        assert(result.isSuccess)
    }

    @Test
    fun `addBook when server error should return failure`() = runTest {
        // Arrange
        val addBookRequest = AddBookRequest(
            title = "test",
            content = "test",
            author = "test",
            coverImage = "",
        )

        `when`(bookRemoteDataSource.addBook("test", addBookRequest)).thenReturn(Result.failure(Throwable("test")))

        // Act
        val result = bookRepository.addBook(addBookRequest)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `deleteBook success should remove book data`() = runTest {
        // Arrange
        val bookId = 3

        `when`(bookRemoteDataSource.deleteBook(bookId, "test")).thenReturn(Result.success("test"))
        doNothing().`when`(bookFileDataSource).deleteContentFile(bookId)
        doNothing().`when`(bookFileDataSource).deleteCoverImageFile(bookId)
        doNothing().`when`(bookDao).delete(bookId)
        `when`(bookDao.getBook(1)).thenReturn(
            BookEntity(
                bookId = 1,
                title = "test",
                author = "test",
                progress = 0.0,
                coverImage = "test",
                content = "test",
                summaryProgress = 0.5,
            ),
        )
        `when`(bookDao.getBook(2)).thenReturn(
            BookEntity(
                bookId = 2,
                title = "test",
                author = "test",
                progress = 0.7,
                coverImage = "test",
                content = "test",
                summaryProgress = 1.0,
            ),
        )
        doNothing().`when`(bookDao).updateProgress(1, 0.0)
        doNothing().`when`(bookDao).updateProgress(2, 0.7)

        // Act
        val result = bookRepository.deleteBook(bookId)

        // Assert
        verify(bookDao, times(2)).delete(bookId)
        verify(bookFileDataSource, times(2)).deleteContentFile(bookId)
        verify(bookFileDataSource, times(2)).deleteCoverImageFile(bookId)
        assert(result.isSuccess)
        assert(bookRepository.getBook(bookId).firstOrNull() == null)
        assert(bookRepository.bookList.firstOrNull()?.size == 2)
    }

    @Test
    fun `deleteBook when server error should return failure`() = runTest {
        // Arrange
        val bookId = 3

        `when`(bookRemoteDataSource.deleteBook(bookId, "test")).thenReturn(Result.failure(Throwable("test")))

        // Act
        val result = bookRepository.deleteBook(bookId)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun `clearBooks success should clear books`() = runTest {
        // Arrange
        doNothing().`when`(bookFileDataSource).deleteAll()
        doNothing().`when`(bookDao).deleteAll()

        // Act
        bookRepository.clearBooks()

        // Assert
        verify(bookFileDataSource, times(1)).deleteAll()
        verify(bookDao, times(1)).deleteAll()
    }
}
