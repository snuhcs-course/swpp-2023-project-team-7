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

interface SummaryAPI {
    @GET("/summary")
    fun getSummary(
        @Query("book_id") bookId: Int,
        @Query("progress") progress: Double,
        @Query("access_token") accessToken: String,
    ): Call<ResponseBody>
}

@InstallIn(SingletonComponent::class)
@Module
class SummaryAPIProviderModule {
    @Provides
    fun provideSummaryAPI(): SummaryAPI {
        return Retrofit.Builder()
            .baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SummaryAPI::class.java)
    }
}

class SummaryRemoteDataSource @Inject constructor(
    private val summaryAPI: SummaryAPI,
) {
    fun getSummary(bookId: Int, progress: Double, accessToken: String) = flow {
        val response = summaryAPI.getSummary(bookId, progress, accessToken).execute()
        if (response.isSuccessful) {
            val responseBody = response.body() ?: throw Throwable("No body")
            responseBody.byteStream().bufferedReader().use {
                try {
                    while (currentCoroutineContext().isActive) {
                        val line = it.readLine() ?: continue
                        if (line.startsWith("data:")) {
                            emit(line.substring(6))
                        }
                    }
                } catch (e: Exception) {
                    throw Throwable("Failed to parse summary")
                }
            }
        } else {
            throw Throwable(parseErrorBody(response.errorBody()))
        }
    }
}