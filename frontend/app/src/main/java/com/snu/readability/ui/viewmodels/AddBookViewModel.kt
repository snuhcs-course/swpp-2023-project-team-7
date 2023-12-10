package com.snu.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.snu.readability.data.book.AddBookRequest
import com.snu.readability.data.book.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
) : ViewModel() {
    suspend fun addBook(data: AddBookRequest) = bookRepository.addBook(data)
}
