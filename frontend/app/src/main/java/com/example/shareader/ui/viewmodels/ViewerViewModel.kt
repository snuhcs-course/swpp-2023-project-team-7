package com.example.shareader.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shareader.ui.models.BookModel
import kotlinx.coroutines.flow.map

class ViewerViewModel(
    private val bookId: String
) : ViewModel() {
    val bookData = BookModel.getInstance().bookList.map { it[bookId] }
    val pageSize = BookModel.getInstance().bookList.map { it[bookId]?.pageSplitData?.pageSplits?.size ?: 0 }

    fun setPageSize(width: Int, height: Int) {
        BookModel.getInstance().setPageSize(width, height, bookId)
    }
}

class ViewerViewModelFactory(private val bookId: String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ViewerViewModel(bookId) as T
}