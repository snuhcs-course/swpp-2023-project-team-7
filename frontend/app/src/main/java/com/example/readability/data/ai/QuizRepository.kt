package com.example.readability.data.ai

import com.example.readability.data.user.UserNotSignedInException
import com.example.readability.data.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class Quiz(
    val question: String,
    val answer: String,
)

enum class QuizLoadState {
    LOADING,
    LOADED,
}

@Singleton
class QuizRepository @Inject constructor(
    private val quizRemoteDataSource: QuizRemoteDataSource,
    private val userRepository: UserRepository,
) {
    private val _quizList = MutableStateFlow(listOf<Quiz>())
    private val _quizCount = MutableStateFlow(0)
    private val _quizLoadState = MutableStateFlow(QuizLoadState.LOADED)

    val quizList = _quizList.asStateFlow()
    val quizCount = _quizCount.asStateFlow()
    val quizLoadState = _quizLoadState.asStateFlow()
    suspend fun getQuiz(bookId: Int, progress: Double): Result<Unit> {
        return withContext(Dispatchers.IO) {
            _quizLoadState.update { QuizLoadState.LOADING }
            val accessToken = userRepository.getAccessToken()
                ?: return@withContext Result.failure(UserNotSignedInException())
            _quizList.update { listOf() }
            _quizCount.update { 0 }
            try {
                quizRemoteDataSource.getQuiz(bookId, progress, accessToken).collect { response ->
                    if (!isActive) return@collect
                    if (response.type == QuizResponseType.COUNT) {
                        _quizCount.value = response.intData
                    } else if (response.type == QuizResponseType.QUESTION) {
                        if (_quizList.value.size < response.intData) {
                            _quizList.update {
                                it.toMutableList().apply {
                                    add(Quiz(response.data, ""))
                                }
                            }
                        } else {
                            _quizList.update {
                                it.toMutableList().apply {
                                    set(response.intData - 1, Quiz(response.data, ""))
                                }
                            }
                        }
                    } else if (response.type == QuizResponseType.ANSWER) {
                        _quizList.update {
                            it.toMutableList().apply {
                                set(response.intData - 1, Quiz(this[response.intData - 1].question, response.data))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
            if (isActive) {
                _quizLoadState.update { QuizLoadState.LOADED }
            }
            Result.success(Unit)
        }
    }
}
