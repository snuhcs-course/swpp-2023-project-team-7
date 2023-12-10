package com.snu.readability.data.book

import androidx.compose.ui.graphics.ImageBitmap
import com.snu.readability.data.NetworkStatusRepository
import com.snu.readability.data.user.UserNotSignedInException
import com.snu.readability.data.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class Book(
    val bookId: Int,
    val title: String,
    val author: String,
    val progress: Double,
    val coverImage: String?,
    val coverImageData: ImageBitmap? = null,
    val content: String,
    val contentData: String? = null,
    val lastRead: Date = Date(0),
    val summaryProgress: Double = 0.0,
) {
    companion object {
        fun fromBookEntity(bookEntity: BookEntity): Book {
            return Book(
                bookId = bookEntity.bookId,
                title = bookEntity.title,
                author = bookEntity.author,
                progress = bookEntity.progress,
                coverImage = bookEntity.coverImage,
                content = bookEntity.content,
                summaryProgress = bookEntity.summaryProgress,
                lastRead = bookEntity.lastRead,
            )
        }
    }

    fun toBookEntity(): BookEntity {
        return BookEntity(
            bookId = this.bookId,
            title = this.title,
            author = this.author,
            progress = this.progress,
            coverImage = this.coverImage,
            content = this.content,
            summaryProgress = this.summaryProgress,
            lastRead = this.lastRead,
        )
    }
}

@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
    private val bookFileDataSource: BookFileDataSource,
    private val bookRemoteDataSource: BookRemoteDataSource,
    private val userRepository: UserRepository,
    private val networkStatusRepository: NetworkStatusRepository,
) {
    private val bookMap = MutableStateFlow(mutableMapOf<Int, Book>())
    private val progressUpdateScope = CoroutineScope(Dispatchers.IO)
    private var progressUpdateJob: Job? = null

    val bookList = bookMap.map {
        it.values.toList()
    }

    fun getBook(bookId: Int) = bookMap.map {
        it[bookId]
    }

    init {
        // load book list from database
        runBlocking {
            withContext(Dispatchers.IO) {
                val bookList = bookDao.getAll()
                val map = mutableMapOf<Int, Book>()
                bookList.forEach { book ->
                    map[book.bookId] = Book.fromBookEntity(book)
                }
                bookMap.value = map
            }
        }
    }

    suspend fun refreshBookList(): Result<Unit> {
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return bookRemoteDataSource.getBookList(accessToken).fold(onSuccess = {
                newBookList ->
            val newMap = bookMap.value.toMutableMap()
            newBookList.forEach { book ->
                println("BookRepository: book: $book")
                if (bookDao.getBook(book.bookId) != null) {
                    bookDao.updateProgress(book.bookId, book.progress)
                    newMap[book.bookId] = newMap[book.bookId]!!.copy(progress = book.progress)
                } else {
                    bookDao.insert(book.toBookEntity())
                    newMap[book.bookId] = book
                }
            }
            // delete books that are not in the list
            bookDao.getAll().forEach { book ->
                if (newBookList.find { book.bookId == it.bookId } == null) {
                    bookFileDataSource.deleteContentFile(book.bookId)
                    bookFileDataSource.deleteCoverImageFile(book.bookId)
                    bookDao.delete(book.bookId)
                    newMap.remove(book.bookId)
                }
            }
            bookMap.value = newMap
            Result.success(Unit)
        }, onFailure = {
            Result.failure(it)
        })
    }

    suspend fun getCoverImageData(bookId: Int): Result<Unit> {
        // find book
        val book = bookMap.value[bookId] ?: return Result.failure(Exception("Book not found"))
        // first check if the cover image is already downloaded
        if (bookFileDataSource.coverImageExists(bookId)) {
            println("BookRepository: cover image exists")
            bookMap.update {
                it.toMutableMap().apply {
                    this[bookId] = this[bookId]!!.copy(coverImageData = bookFileDataSource.readCoverImageFile(bookId))
                }
            }
            return Result.success(Unit)
        }
        println("BookRepository: cover image does not exist")

        // else, download the cover image
        // check if the user is signed in
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        if (book.coverImage == null) {
            return Result.failure(Exception("Book cover image path not found"))
        }
        return bookRemoteDataSource.getCoverImageData(accessToken, book.coverImage)
            .fold(onSuccess = { image ->
                bookFileDataSource.writeCoverImageFile(bookId, image)
                bookMap.update {
                    it.toMutableMap().apply {
                        this[bookId] = book.copy(coverImageData = image)
                    }
                }
                Result.success(Unit)
            }, onFailure = {
                Result.failure(it)
            })
    }

    suspend fun getContentData(bookId: Int): Result<Unit> {
        // find book
        val book = bookMap.value[bookId] ?: return Result.failure(Exception("Book not found"))
        // first check if the content data is already downloaded
        if (bookFileDataSource.contentExists(bookId)) {
            println("BookRepository: content exists")
            bookMap.update {
                it.toMutableMap().apply {
                    this[bookId] = this[bookId]!!.copy(contentData = bookFileDataSource.readContentFile(bookId))
                }
            }
            return Result.success(Unit)
        }

        // else, download the content data
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return bookRemoteDataSource.getContentData(accessToken, book.content)
            .fold(onSuccess = { contentData ->
                bookFileDataSource.writeContentFile(bookId, contentData)
                bookMap.update {
                    it.toMutableMap().apply {
                        this[bookId] = book.copy(contentData = contentData)
                    }
                }
                Result.success(Unit)
            }, onFailure = {
                Result.failure(it)
            })
    }

    suspend fun updateSummaryProgress(bookId: Int): Result<Unit> {
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return bookRemoteDataSource.getSummaryProgress(accessToken, bookId)
            .fold(onSuccess = { summaryProgress ->
                delay(1000L)
                bookDao.updateSummaryProgress(bookId, summaryProgress.toDouble())
                bookMap.update {
                    it.toMutableMap().apply {
                        this[bookId] = this[bookId]!!.copy(summaryProgress = summaryProgress.toDouble())
                    }
                }
                Result.success(Unit)
            }, onFailure = {
                Result.failure(it)
            })
    }

    suspend fun addBook(data: AddBookRequest): Result<Unit> {
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return bookRemoteDataSource.addBook(accessToken, data).fold(onSuccess = {
            refreshBookList()
        }, onFailure = {
            Result.failure(it)
        })
    }

    suspend fun updateProgress(bookId: Int, progress: Double): Result<Unit> {
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        bookDao.updateProgress(bookId, progress)
        bookMap.update {
            it.toMutableMap().apply {
                this[bookId] = this[bookId]!!.copy(progress = progress)
            }
        }

        progressUpdateJob?.cancel()
        progressUpdateJob = progressUpdateScope.launch {
            delay(100L)
            if (!isActive || !networkStatusRepository.isConnected) {
                return@launch
            }
            bookRemoteDataSource.updateProgress(bookId, progress, accessToken)
        }
        return Result.success(Unit)
    }

    suspend fun deleteBook(bookId: Int): Result<Unit> {
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return bookRemoteDataSource.deleteBook(bookId, accessToken).fold(onSuccess = {
            val book = bookMap.value[bookId] ?: return Result.failure(UserNotSignedInException())

            bookFileDataSource.deleteContentFile(bookId)
            bookFileDataSource.deleteCoverImageFile(bookId)
            bookDao.delete(book.bookId)
            bookMap.update {
                val newMap = it.toMutableMap()
                newMap.remove(bookId)
                newMap
            }

            refreshBookList()
        }, onFailure = {
            Result.failure(it)
        })
    }

    suspend fun clearBooks() {
        bookFileDataSource.deleteAll()
        bookDao.deleteAll()
        bookMap.value = mutableMapOf()
    }
}
