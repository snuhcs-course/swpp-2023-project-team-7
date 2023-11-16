package com.example.readability.ui.models

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class AddBookRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: String,
    @SerializedName("cover_image") val coverImage: String,
)

interface AddBookAPI {
    @POST("/book/add")
    fun addBook(@Body book: AddBookRequest): Call<Unit>
}

// TODO: Inject dependencies using Hilt

class AddBookModel {
    private val addBookAPI: AddBookAPI

    companion object {
        @Volatile
        private var instance: AddBookModel? = null

        fun getInstance(): AddBookModel {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AddBookModel()
                    }
                }
            }
            return instance!!
        }
    }

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()

        addBookAPI = retrofit.create(AddBookAPI::class.java)
    }

    suspend fun addBook(
        req: AddBookRequest
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = addBookAPI.addBook(req).execute()

            if (result.isSuccessful) {
                return@withContext Result.success(Unit)
            } else {

                println(result.errorBody()!!.charStream().readText())
                println(result.code())
                return@withContext Result.failure(Throwable("Add book failed"))
            }
        }
    }
}