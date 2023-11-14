package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.ui.models.BookCardData
import com.example.readability.ui.models.BookModel
import kotlinx.coroutines.flow.map

class BookListViewModel : ViewModel() {
    val bookCardDataList = BookModel.getInstance().bookList.map {
        it.map { bookData ->
            BookCardData(
                id = bookData.value.id,
                title = bookData.value.title,
                author = bookData.value.author,
                progress = bookData.value.progress,
                coverImage = bookData.value.coverImage,
            )
        }
    }
}