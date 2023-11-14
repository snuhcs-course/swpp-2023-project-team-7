package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.ui.models.SummaryModel

class SummaryViewModel : ViewModel() {
    val summaryState = SummaryModel.getInstance().summaryState
    val summaryLoadState = SummaryModel.getInstance().summaryLoadState

    fun loadSummary() {
        SummaryModel.getInstance().loadSummary("1", 0.98)
    }
}