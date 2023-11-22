package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readability.data.book.BookCardData
import com.example.readability.data.book.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookRepository: BookRepository,
) : ViewModel() {
    val bookCardDataList = bookRepository.bookList.map {
        it.map { book ->
            BookCardData(
                id = book.bookId,
                title = book.title,
                author = book.author,
                progress = book.progress,
                coverImage = book.coverImage,
                coverImageData = book.coverImageData,
                content = book.content,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.refreshBookList().onFailure {
                println("BookListViewModel: refreshBookList failed: $it")
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
}
