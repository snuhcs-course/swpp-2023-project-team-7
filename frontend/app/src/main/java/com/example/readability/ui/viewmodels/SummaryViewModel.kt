package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readability.data.ai.SummaryRepository
import com.example.readability.data.viewer.FontDataSource
import com.example.readability.data.viewer.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val summaryRepository: SummaryRepository,
    private val settingRepository: SettingRepository,
    private val fontDataSource: FontDataSource,
) : ViewModel() {
    val summary = summaryRepository.summary
    val viewerStyle = settingRepository.viewerStyle
    val typeface = fontDataSource.customTypeface
    val referenceLineHeight = fontDataSource.referenceLineHeight

    fun loadSummary(bookId: Int, progress: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            summaryRepository.getSummary(bookId, progress).onFailure {
                it.printStackTrace()
            }
        }
    }
}
