package com.snu.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snu.readability.data.book.Book
import com.snu.readability.data.book.BookCardData
import com.snu.readability.data.book.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private fun bookToBookCardData(book: Book) = BookCardData(
        id = book.bookId,
        title = book.title,
        author = book.author,
        progress = book.progress,
        coverImage = book.coverImage,
        coverImageData = book.coverImageData,
        content = book.content,
        summaryProgress = book.summaryProgress,
    )

    val bookCardDataList = bookRepository.bookList.map {
        it.map { bookToBookCardData(it) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        runBlocking {
            bookRepository.bookList.first().map { bookToBookCardData(it) }
        },
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.refreshBookList().onFailure {
                println("BookListViewModel: refreshBookList failed: ${it.message}")
            }
        }
    }

    suspend fun getCoverImageData(bookId: Int) = withContext(Dispatchers.IO) {
        bookRepository.getCoverImageData(bookId)
    }

    suspend fun getContentData(bookId: Int) = withContext(Dispatchers.IO) {
        bookRepository.getContentData(bookId)
    }

    fun updateProgress(bookId: Int, progress: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateProgress(bookId, progress)
        }
    }

    suspend fun deleteBook(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.deleteBook(bookId)
        }
    }

    suspend fun updateBookList(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            bookRepository.refreshBookList()
        }
    }

    suspend fun clearBookList() {
        bookRepository.clearBooks()
    }
}
