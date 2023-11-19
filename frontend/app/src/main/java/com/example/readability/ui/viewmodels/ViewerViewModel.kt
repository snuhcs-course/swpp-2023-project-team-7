package com.example.readability.ui.viewmodels

import androidx.compose.ui.graphics.NativeCanvas
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readability.data.book.BookRepository
import com.example.readability.data.viewer.PageSplitData
import com.example.readability.data.viewer.PageSplitRepository
import com.example.readability.data.viewer.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val pageSplitRepository: PageSplitRepository,
    settingRepository: SettingRepository
) : ViewModel() {

    val pageSplitData = MutableStateFlow<PageSplitData?>(null)
    val viewerStyle = settingRepository.viewerStyle

    fun setPageSize(bookId: Int, width: Int, height: Int) {
        if (pageSplitData.value?.width == width && pageSplitData.value?.height == height && pageSplitData.value?.viewerStyle == viewerStyle.value) {
            return
        }
        viewModelScope.launch {
            pageSplitData.value = pageSplitRepository.getSplitData(bookId, width, height)
        }
    }

    fun setProgress(bookId: Int, progress: Double) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                bookRepository.updateProgress(bookId, progress)
            }
        }
    }

    fun getBookData(id: Int) = bookRepository.getBook(id)

    fun drawPage(bookId: Int, canvas: NativeCanvas, page: Int, isDarkMode: Boolean) {
        pageSplitRepository.drawPage(
            canvas = canvas, bookId = bookId, page = page, isDarkMode = isDarkMode
        )
    }
}
