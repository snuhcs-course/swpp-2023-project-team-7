package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.ui.models.QuizModel

class QuizViewModel : ViewModel() {
    val quizList = QuizModel.getInstance().quizList
    val quizSize = QuizModel.getInstance().quizSize
    val quizLoadState = QuizModel.getInstance().quizLoadState

    fun loadQuiz() {
        QuizModel.getInstance().loadQuiz("1", 0.98)
    }
}