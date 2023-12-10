package com.snu.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.snu.readability.data.ai.QuizRepository
import com.snu.readability.data.book.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val bookRepository: BookRepository,

) : ViewModel() {
    val quizList = quizRepository.quizList
    val quizSize = quizRepository.quizCount
    val quizLoadState = quizRepository.quizLoadState

    suspend fun loadQuiz(bookId: Int): Result<Unit> {
        return quizRepository.getQuiz(bookId, bookRepository.getBook(bookId).first()!!.progress)
    }
}
