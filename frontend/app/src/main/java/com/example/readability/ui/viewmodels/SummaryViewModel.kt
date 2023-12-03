package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.data.ai.SummaryRepository
import com.example.readability.data.book.BookRepository
import com.example.readability.data.viewer.FontDataSource
import com.example.readability.data.viewer.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val settingRepository: SettingRepository,
    private val fontDataSource: FontDataSource,
    private val bookRepository: BookRepository,
) : ViewModel() {
    val summary = summaryRepository.summary
    val viewerStyle = settingRepository.viewerStyle
    val typeface = fontDataSource.customTypeface
    val referenceLineHeight = fontDataSource.referenceLineHeight

    suspend fun loadSummary(bookId: Int): Result<Unit> {
        return summaryRepository.getSummary(bookId, bookRepository.getBook(bookId).first()!!.progress)
    }
}
