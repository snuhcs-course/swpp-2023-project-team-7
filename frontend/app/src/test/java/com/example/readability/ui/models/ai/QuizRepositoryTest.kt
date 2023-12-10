package com.example.readability.ui.models.ai

import com.example.readability.data.NetworkStatusRepository
import com.example.readability.data.ai.QuizRemoteDataSource
import com.example.readability.data.ai.QuizRepository
import com.example.readability.data.ai.QuizResponse
import com.example.readability.data.ai.QuizResponseType
import com.example.readability.data.user.UserNotSignedInException
import com.example.readability.data.user.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class QuizRepositoryTest {

    private lateinit var quizRepository: QuizRepository
    private lateinit var quizRemoteDataSource: QuizRemoteDataSource
    private lateinit var userRepository: UserRepository
    private lateinit var networkStatusRepository: NetworkStatusRepository

    @Before
    fun setup() {
        quizRemoteDataSource = mockk()
        userRepository = mockk()
        networkStatusRepository = mockk()
        quizRepository = QuizRepository(quizRemoteDataSource, userRepository, networkStatusRepository)
    }

    @Test
    fun `getQuiz when not connected to network should return failure`() = runTest {
        // Arrange
        coEvery { networkStatusRepository.isConnected } returns false

        // Act
        val result = quizRepository.getQuiz(1, 0.5)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() is Exception)
    }

    @Test
    fun `getQuiz when user not signed in should return failure`() = runTest {
        // Arrange
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery { userRepository.getAccessToken() } returns null

        // Act
        val result = quizRepository.getQuiz(1, 0.5)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() is UserNotSignedInException)
    }

    @Test
    fun `getQuiz success should update quizList and quizCount`() = runTest {
        // Arrange
        coEvery { networkStatusRepository.isConnected } returns true
        coEvery { userRepository.getAccessToken() } returns "testAccessToken"
        coEvery { quizRemoteDataSource.getQuiz(any(), any(), any()) } returns flowOf(
            QuizResponse(QuizResponseType.COUNT, "", 3),
            QuizResponse(QuizResponseType.STRING, "Question 1", 0),
            QuizResponse(QuizResponseType.QUESTION_END, "", 0),
            QuizResponse(QuizResponseType.STRING, "Answer 1", 0),
            QuizResponse(QuizResponseType.ANSWER_END, "", 0),
            QuizResponse(QuizResponseType.STRING, "Question 2", 0),
            QuizResponse(QuizResponseType.QUESTION_END, "", 0),
            QuizResponse(QuizResponseType.STRING, "Answer 2", 0),
            QuizResponse(QuizResponseType.ANSWER_END, "", 0),
            QuizResponse(QuizResponseType.STRING, "Question 3", 0),
            QuizResponse(QuizResponseType.QUESTION_END, "", 0),
            QuizResponse(QuizResponseType.STRING, "Answer 3", 0),
            QuizResponse(QuizResponseType.ANSWER_END, "", 0),
        )

        // Act
        val result = quizRepository.getQuiz(1, 0.5)

        // Assert
        assertEquals(Result.success(Unit), result)
        assertEquals(3, quizRepository.quizCount.first())
        assertEquals(3, quizRepository.quizList.first().size)
        assertEquals("Question 1", quizRepository.quizList.first()[0].question)
        assertEquals("Question 2", quizRepository.quizList.first()[1].question)
        assertEquals("Question 3", quizRepository.quizList.first()[2].question)
    }
}
