package com.snu.readability.ui.models.ai

import com.snu.readability.data.ai.QuizAPI
import com.snu.readability.data.ai.QuizRemoteDataSource
import com.snu.readability.data.ai.QuizResponseType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.IOException

val quizResponseExample = """
event: quiz
data: Number of Questions

event: quiz
data: :

event: quiz
data: 3

event: quiz
data: 

event: quiz
data: Q1

event: quiz
data: :

event: quiz
data: Why this unit test is meaningful?

event: quiz
data: 

event: quiz
data: A1:

event: quiz
data: Because it checks the correctness of the code.

event: quiz
data: 

event: quiz
data: Q

event: quiz
data: 2:

event: quiz
data: What is the best programming language?

event: quiz
data: 

event: quiz
data: A2:

event: quiz
data: Kotlin!

event: quiz
data: 

event: quiz
data: Q3:

event: quiz
data: Is the parsing works

event: quiz
data: 

event: quiz
data: A3:

event: quiz
data: Yes!

event: quiz
data: 
""".trimIndent()

class QuizRemoteDataSourceTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var quizAPI: QuizAPI

    lateinit var quizRemoteDataSource: QuizRemoteDataSource

    @Before
    fun setUp() {
        quizRemoteDataSource = QuizRemoteDataSource(quizAPI)
    }

    @Test
    fun `getQuiz success`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"
        val responseBody = mock(ResponseBody::class.java)
        doReturn(ByteArrayInputStream(quizResponseExample.toByteArray())).`when`(responseBody).byteStream()
        val response = Response.success(responseBody)

        val call = mock(retrofit2.Call::class.java)
        doReturn(response).`when`(call).execute()
        doReturn(call).`when`(quizAPI).getQuiz(bookId, progress, accessToken)

        // Act
        val result = quizRemoteDataSource.getQuiz(bookId, progress, accessToken)

        // Assert
        val numberOfQuiz = 3
        val data = mutableListOf(
            "Why this unit test is meaningful?" to "Because it checks the correctness of the code.",
            "What is the best programming language?" to "Kotlin!",
            "Is the parsing works" to "Yes!",
        )
        var isQuestion = true
        var index = 0
        result.toList().forEach {
            println(it)
            if (it.type == QuizResponseType.COUNT) {
                assert(it.intData == numberOfQuiz)
            } else if (it.type == QuizResponseType.QUESTION_END) {
                isQuestion = false
            } else if (it.type == QuizResponseType.ANSWER_END) {
                isQuestion = true
                index++
            } else if (it.type == QuizResponseType.STRING) {
                if (isQuestion) {
                    assert(data[index].first.startsWith(it.data))
                    data[index] = data[index].copy(
                        first = data[index].first.substring(it.data.length),
                    )
                } else {
                    assert(data[index].second.startsWith(it.data))
                    data[index] = data[index].copy(
                        second = data[index].second.substring(it.data.length),
                    )
                }
            }
        }

        println(data)

        for (d in data) {
            assert(d.first.isEmpty())
            assert(d.second.isEmpty())
        }

        // Verify
        verify(quizAPI).getQuiz(bookId, progress, accessToken)
    }

    @Test(expected = Throwable::class)
    fun `getQuiz network error`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"

        doThrow(IOException("Network error")).`when`(quizAPI).getQuiz(bookId, progress, accessToken).execute()

        // Act
        val result = quizRemoteDataSource.getQuiz(bookId, progress, accessToken)

        // Assert
        result.collect {
            // This block should not be reached
        }

        // Verify
        verify(quizAPI).getQuiz(bookId, progress, accessToken)
    }

    @Test(expected = Throwable::class)
    fun `getQuiz server error`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"
        val errorBody = mock(ResponseBody::class.java)
        doReturn("{\"detail\":\"Server error\"}").`when`(errorBody).string()
        val response = Response.error<ResponseBody>(400, errorBody)

//        `when`(quizAPI.getQuiz(bookId, progress, accessToken)).thenReturn(mock {
//            on { execute() } doReturn response
//        })
        doReturn(response).`when`(quizAPI).getQuiz(bookId, progress, accessToken)

        // Act
        val result = quizRemoteDataSource.getQuiz(bookId, progress, accessToken)

        // Assert
        result.collect {
            // This block should not be reached
        }

        // Verify
        verify(quizAPI).getQuiz(bookId, progress, accessToken)
    }

    @Test(expected = Throwable::class)
    fun `getQuiz parsing error`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"
        val responseBody = mock(ResponseBody::class.java)
        doReturn(
            ByteArrayInputStream("data: Sorry, but we cannot generate quiz.".toByteArray()),
        ).`when`(responseBody).byteStream()
        val response = Response.success(responseBody)

        val call = mock(retrofit2.Call::class.java)
        doReturn(response).`when`(call).execute()
        doReturn(call).`when`(quizAPI).getQuiz(bookId, progress, accessToken)

        // Act
        val result = quizRemoteDataSource.getQuiz(bookId, progress, accessToken)

        // Assert
        result.collect {
            // This block should not be reached
        }

        // Verify
        verify(quizAPI).getQuiz(bookId, progress, accessToken)
    }
}
