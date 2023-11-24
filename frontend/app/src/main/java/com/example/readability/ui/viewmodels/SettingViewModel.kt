package com.example.readability.ui.viewmodels

import androidx.compose.ui.graphics.NativeCanvas
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readability.data.viewer.PageSplitRepository
import com.example.readability.data.viewer.SettingRepository
import com.example.readability.data.viewer.ViewerStyle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingRepository: SettingRepository,
    private val pageSplitRepository: PageSplitRepository,
) : ViewModel() {
    val sampleText = settingRepository.sampleText.asStateFlow()
    val viewerStyle = settingRepository.viewerStyle

    fun updateViewerStyle(viewerStyle: ViewerStyle) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingRepository.updateViewerStyle(viewerStyle)
            }
        }
    }

    fun drawPage(canvas: NativeCanvas, width: Int, isDarkMode: Boolean) {
        pageSplitRepository.drawPageRaw(
            canvas = canvas,
            viewerStyle = viewerStyle.value,
            pageContent = sampleText.value,
            width = width,
            isDarkMode = isDarkMode,
        )
    }
}
