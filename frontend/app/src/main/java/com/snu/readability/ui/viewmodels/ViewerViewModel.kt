package com.snu.readability.ui.viewmodels

import androidx.compose.ui.graphics.NativeCanvas
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snu.readability.data.book.BookRepository
import com.snu.readability.data.viewer.PageSplitData
import com.snu.readability.data.viewer.PageSplitRepository
import com.snu.readability.data.viewer.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val pageSplitRepository: PageSplitRepository,
    settingRepository: SettingRepository,
) : ViewModel() {

    val pageSplitData = MutableStateFlow<PageSplitData?>(null)
    val viewerStyle = settingRepository.viewerStyle
    private var job: Job? = null

    fun setPageSize(bookId: Int, width: Int, height: Int) {
        if (pageSplitData.value?.width == width &&
            pageSplitData.value?.height == height &&
            pageSplitData.value?.viewerStyle == viewerStyle.value
        ) {
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

    suspend fun updateSummaryProgress(bookId: Int): Result<Unit> {
        return bookRepository.updateSummaryProgress(bookId)
    }

    fun getBookData(id: Int) = bookRepository.getBook(id)

    fun drawPage(bookId: Int, canvas: NativeCanvas, page: Int, isDarkMode: Boolean) {
        pageSplitRepository.drawPage(
            canvas = canvas,
            bookId = bookId,
            page = page,
            isDarkMode = isDarkMode,
        )
    }
}
