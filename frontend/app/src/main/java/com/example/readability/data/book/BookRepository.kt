package com.example.readability.data.book

import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.user.UserNotSignedInException
import com.example.readability.data.user.UserRepository
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookDao: BookDao,
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
                    map[book.bookId] = book
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
                if (bookDao.getBook(book.id) != null) {
                    bookDao.updateProgress(book.id, book.progress)
                    newMap[book.id] = newMap[book.id]!!.copy(progress = book.progress)
                } else {
                    val bookObject = Book(
                        bookId = book.id,
                        title = book.title,
                        author = book.author,
                        progress = book.progress,
                        coverImage = book.coverImage,
                        content = book.content,
                        numCurrentInference = book.numCurrentInference,
                        numTotalInference = book.numTotalInference,
                    )
                    bookDao.insert(bookObject)
                    newMap[book.id] = bookObject
                }
            }
            // delete books that are not in the list
            bookDao.getAll().forEach { book ->
                if (newBookList.find { book.bookId == it.id } == null) {
                    bookDao.delete(book)
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
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        val book = bookDao.getBook(bookId) ?: return Result.failure(
            Exception("Book not found"),
        )
        if (book.coverImageData != null) {
            return Result.success(Unit)
        }
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        if (book.coverImage == null) {
            return Result.failure(Exception("Book cover image not found"))
        }
        return bookRemoteDataSource.getCoverImageData(accessToken, book.coverImage)
            .fold(onSuccess = { image ->
                bookDao.updateCoverImageData(bookId, image)
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
        val accessToken =
            userRepository.getAccessToken() ?: return Result.failure(UserNotSignedInException())
        val book = bookDao.getBook(bookId) ?: return Result.failure(
            Exception("Book not found"),
        )
        if (book.contentData != null) {
            return Result.success(Unit)
        }
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return bookRemoteDataSource.getContentData(accessToken, book.content)
            .fold(onSuccess = { contentData ->
                bookDao.updateContentData(bookId, contentData)
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

            bookDao.delete(book)
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
        bookDao.deleteAll()
        bookMap.value = mutableMapOf()
    }

    fun updateAIStatus(bookId: Int, aiStatus: Double) {
        bookDao.getNumTotalInference(bookId) ?: return
        bookMap.update {
            it.toMutableMap().apply {
//                this[bookId] = this[bookId]!!.copy(aiStatus = aiStatus)
            }
        }
    }
}
