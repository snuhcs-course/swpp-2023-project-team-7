package com.example.readability.data.ai

import com.example.readability.data.parseErrorBody
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import javax.inject.Inject
import javax.inject.Singleton

interface QuizAPI {
    @Streaming
    @GET("/quiz")
    fun getQuiz(
        @Query("book_id") bookId: Int,
        @Query("progress") progress: Double,
        @Query("access_token") accessToken: String,
    ): Call<ResponseBody>
}

@InstallIn(SingletonComponent::class)
@Module
class QuizAPIProviderModule {
    @Provides
    @Singleton
    fun provideQuizAPI(): QuizAPI {
        return Retrofit.Builder().baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(QuizAPI::class.java)
    }
}

enum class QuizResponseType {
    COUNT,
    QUESTION_END,
    ANSWER_END,
    STRING,
}

data class QuizResponse(
    val type: QuizResponseType,
    val data: String,
    val intData: Int,
)

@Singleton
class QuizRemoteDataSource @Inject constructor(
    private val quizAPI: QuizAPI,
) {
    fun getQuiz(bookId: Int, progress: Double, accessToken: String) = flow {
        val response = quizAPI.getQuiz(bookId, progress, accessToken).execute()
        if (response.isSuccessful) {
            val responseBody = response.body() ?: throw Throwable("No body")
            println("QuizRemoteDataSource: getQuiz: response.isSuccessful")
            responseBody.byteStream().bufferedReader().use {
                try {
                    var content = ""
                    var quizCount = 0
                    var receivingQuizCount = false
                    var receivingQuiz = false
                    var receivingAnswer = false
                    var quizCountContent = ""
                    val updateContent = { token: String ->
                        if (receivingQuizCount) {
                            quizCountContent += token
                        } else {
                            content += token
                        }
                    }
                    while (currentCoroutineContext().isActive) {
                        val line = it.readLine() ?: continue
//                        println(line)
                        if (line.startsWith("data:")) {
                            var token = line.substring(6)
                            if (token.isEmpty()) token = "\n"
                            if (token.contains(":")) {
                                updateContent(token.substring(0, token.indexOf(":")))
                                println(
                                    "content: $content, quizCount: $quizCount, receivingQuizCount: $receivingQuizCount, receivingQuiz: $receivingQuiz",
                                )
                                if (quizCount == 0) {
                                    receivingQuizCount = true
                                } else {
                                    receivingQuiz = content.contains("Q")
                                    receivingAnswer = !receivingQuiz
                                    content = ""
                                }
                                updateContent(token.substring(token.indexOf(":") + 1))
                            } else if (token.contains("\n")) {
                                if (receivingQuizCount) {
                                    println("quizCountContent: $quizCountContent")
                                    quizCount = (quizCountContent.trim()).toInt()
                                    emit(QuizResponse(QuizResponseType.COUNT, "", quizCount))
                                } else if (receivingQuiz) {
                                    emit(QuizResponse(QuizResponseType.QUESTION_END, "", 0))
                                } else if (receivingAnswer) {
                                    emit(QuizResponse(QuizResponseType.ANSWER_END, "", 0))
                                }
                                receivingQuizCount = false
                                receivingAnswer = false
                                receivingQuiz = false
                                content = ""
                            } else {
                                updateContent(token)
                                if (receivingQuiz) {
                                    emit(QuizResponse(QuizResponseType.STRING, content, 0))
                                    content = ""
                                } else if (receivingAnswer) {
                                    emit(QuizResponse(QuizResponseType.STRING, content, 0))
                                    content = ""
                                }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw Throwable("Failed to parse quiz")
                }
            }
        } else {
            throw Throwable(parseErrorBody(response.errorBody()))
        }
    }
}
