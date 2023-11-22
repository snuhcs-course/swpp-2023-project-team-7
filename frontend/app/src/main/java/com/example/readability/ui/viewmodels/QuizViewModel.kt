package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readability.data.ai.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,

) : ViewModel() {
    val quizList = quizRepository.quizList
    val quizSize = quizRepository.quizCount
    val quizLoadState = quizRepository.quizLoadState

    fun loadQuiz(bookId: Int, progress: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            quizRepository.getQuiz(bookId, progress)
        }
    }
}
