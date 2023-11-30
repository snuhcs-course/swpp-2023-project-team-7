package com.example.readability.ui.models

import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.book.BookDao
import com.example.readability.data.book.BookEntity
import com.example.readability.data.book.BookFileDataSource
import com.example.readability.data.book.BookRemoteDataSource
import com.example.readability.data.book.BookRepository
import com.example.readability.data.user.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
                    coverImage = "test",
                    content = "test",
                    summaryProgress = 0.5,
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

    @Test
    fun setProgress_succeed() = runTest {
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
}
