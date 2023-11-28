package com.example.readability.data.ai

import com.example.readability.data.user.UserNotSignedInException
import com.example.readability.data.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    private val quizLoadScope = CoroutineScope(Dispatchers.IO)
    private var lastQuizLoadJob: Job? = null

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
                lastQuizLoadJob?.cancel()
                lastQuizLoadJob = quizLoadScope.launch {
                    var receivingQuiz = true
                    quizRemoteDataSource.getQuiz(bookId, progress, accessToken).collect { response ->
                        if (!isActive) return@collect
                        if (response.type == QuizResponseType.COUNT) {
                            _quizCount.value = response.intData
                            _quizList.update { listOf(Quiz("", "")) }
                        } else if (response.type == QuizResponseType.QUESTION_END) {
                            receivingQuiz = false
                        } else if (response.type == QuizResponseType.ANSWER_END) {
                            receivingQuiz = true
                            if (_quizList.value.size < _quizCount.value) {
                                _quizList.update {
                                    it.toMutableList().apply {
                                        add(Quiz("", ""))
                                    }
                                }
                            }
                        } else {
                            val lastIndex = _quizList.value.lastIndex
                            _quizList.update {
                                it.toMutableList().apply {
                                    if (receivingQuiz) {
                                        set(
                                            lastIndex,
                                            Quiz(it[lastIndex].question + response.data, it[lastIndex].answer),
                                        )
                                    } else {
                                        set(
                                            lastIndex,
                                            Quiz(it[lastIndex].question, it[lastIndex].answer + response.data),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                lastQuizLoadJob?.join()
            } catch (e: Throwable) {
                e.printStackTrace()
                return@withContext Result.failure(e)
            }
            if (isActive) {
                _quizLoadState.update { QuizLoadState.LOADED }
            }
            Result.success(Unit)
        }
    }
}
