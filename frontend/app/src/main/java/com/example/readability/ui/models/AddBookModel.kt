package com.example.readability.ui.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream

data class UploadBookImageResponse(
    @SerializedName("url") val url: String,
)

data class AddBookRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: String,
    @SerializedName("cover_image") val coverImage: String,
)

interface AddBookAPI {
    @Multipart
    @POST("/book/image/upload")
    fun uploadImage(@Part file: MultipartBody.Part): Call<UploadBookImageResponse>

    @POST("/book/add")
    fun addBook(@Body book: AddBookRequest): Call<Unit>
}

// TODO: Inject dependencies using Hilt

class AddBookModel {
    private val addBookAPI: AddBookAPI

    companion object {
        private var instance: AddBookModel? = null

        fun getInstance(): AddBookModel {
            if (instance == null) {
                instance = AddBookModel()
            }
            return instance!!
        }
    }

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()

        addBookAPI = retrofit.create(AddBookAPI::class.java)
    }

    suspend fun uploadImage(image: Bitmap): Result<String> {
        return withContext(Dispatchers.IO) {
            // temporary file
            val file = File.createTempFile("temp", null)
            // save image to temporary file
            with(FileOutputStream(file)) {
                image.compress(Bitmap.CompressFormat.PNG, 100, this)
            }

            val requestFile: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val fileBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val result = addBookAPI.uploadImage(fileBody).execute()
            file.delete()

            if (result.isSuccessful) {
                return@withContext Result.success(result.body()!!.url)
            } else {
                println(result.errorBody())
                return@withContext Result.failure<String>(Throwable("Upload failed"))
            }
        }
    }

    suspend fun addBook(
        title: String, author: String, content: String, coverUrl: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val result = addBookAPI.addBook(
                AddBookRequest(
                    title = title, content = content, author = author, coverImage = coverUrl
                )
            ).execute()

            if (result.isSuccessful) {
                return@withContext Result.success("Success")
            } else {
                return@withContext Result.failure<String>(Throwable("Add book failed"))
            }
        }
    }
}