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
import javax.inject.Inject
import javax.inject.Singleton

interface QuizAPI {
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
    QUESTION,
    ANSWER,
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
            responseBody.byteStream().bufferedReader().use {
                try {
                    var content = ""
                    var quizCount = 0
                    var receivingQuizCount = false
                    var receivingQuiz = true
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
                        if (line.startsWith("data:")) {
                            val token = line.substring(6)
                            if (token.contains(":")) {
                                updateContent(token.substring(0, token.indexOf(":")))
                                if (quizCount == 0) {
                                    receivingQuizCount = true
                                } else {
                                    receivingQuiz = content.contains("Q")
                                    content = ""
                                }
                                updateContent(token.substring(token.indexOf(":") + 1))
                            } else if (token.contains("\n")) {
                                updateContent(token.substring(0, token.indexOf("\n")))
                                if (receivingQuizCount) {
                                    quizCount = quizCountContent.toInt()
                                    receivingQuizCount = false
                                    emit(QuizResponse(QuizResponseType.COUNT, "", quizCount))
                                } else if (receivingQuiz) {
                                    emit(QuizResponse(QuizResponseType.QUESTION, content, 0))
                                    content = ""
                                } else {
                                    emit(QuizResponse(QuizResponseType.ANSWER, content, token.toInt()))
                                    content = ""
                                }
                                updateContent(token.substring(token.indexOf("\n") + 1))
                            } else {
                                updateContent(token)
                            }
                        }
                    }
                } catch (e: Exception) {
                    throw Throwable("Failed to parse quiz")
                }
            }
        } else {
            throw Throwable(parseErrorBody(response.errorBody()))
        }
    }
}
