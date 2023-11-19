package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.data.book.AddBookRequest
import com.example.readability.data.book.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {
    suspend fun addBook(data: AddBookRequest) = bookRepository.addBook(data)
}