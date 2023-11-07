package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.ui.models.AddBookModel
import com.example.readability.ui.models.AddBookRequest

class AddBookViewModel : ViewModel() {
    suspend fun addBook(data: AddBookRequest): Result<Unit> {
        return AddBookModel.getInstance().addBook(data)
    }
}