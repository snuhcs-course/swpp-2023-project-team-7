package com.example.readability.data.book

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.readability.data.parseErrorBody
import com.google.gson.annotations.SerializedName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import javax.inject.Inject
import javax.inject.Singleton

data class BookCardData(
    val id: Int,
    val title: String,
    val author: String,
    val progress: Double,
    val coverImage: String? = null,
    val coverImageData: ImageBitmap? = null,
    val content: String,
    val numTotalInference: Int = 0,
    val numCurrentInference: Int = 0,
)

data class BookResponse(
    val book_id: Int,
    val title: String,
    val author: String,
    val content: String,
    val cover_image: String,
    val progress: Double,
    val num_total_inference: Int,
    val num_current_inference: Int,
)

data class AddBookRequest(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: String,
    @SerializedName("cover_image") val coverImage: String,
)

data class BooksResponse(
    val books: List<BookResponse>,
)

interface BookAPI {
    @Headers("Accept: application/json")
    @GET("/books")
    fun getBooks(@Query("access_token") accessToken: String): Call<BooksResponse>

    @Streaming
    @GET("/book/image")
    fun getBookCoverImage(
        @Query("image_url") imageUrl: String,
        @Query("access_token") accessToken: String,
    ): Call<ResponseBody>

    @Streaming
    @GET("/book/content")
    fun getBookContent(
        @Query("content_url") contentUrl: String,
        @Query("access_token") accessToken: String,
    ): Call<ResponseBody>

    @POST("/book/add")
    fun addBook(@Query("access_token") accessToken: String, @Body book: AddBookRequest): Call<Unit>

    @PUT("/book/{book_id}/progress")
    fun updateProgress(
        @Path("book_id") bookId: Int,
        @Query("progress") progress: Double,
        @Query("access_token") accessToken: String,
    ): Call<ResponseBody>

    @DELETE("/book/delete")
    fun deleteBook(@Query("book_id") bookId: Int, @Query("access_token") accessToken: String): Call<ResponseBody>
}

@InstallIn(SingletonComponent::class)
@Module
class BookAPIProviderModule {
    @Provides
    @Singleton
    fun provideBookAPI(): BookAPI {
        return Retrofit.Builder().baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(BookAPI::class.java)
    }
}

@Singleton
class BookRemoteDataSource @Inject constructor(
    private val bookAPI: BookAPI,
) {

    fun getBookList(accessToken: String): Result<List<BookCardData>> {
        try {
            val response = bookAPI.getBooks(accessToken).execute()
            if (response.isSuccessful) {
                val responseBody = response.body() ?: return Result.failure(Throwable("No body"))
                return Result.success(
                    responseBody.books.map {
                        BookCardData(
                            id = it.book_id,
                            title = it.title,
                            author = it.author,
                            progress = it.progress,
                            coverImage = it.cover_image,
                            content = it.content,
                        )
                    },
                )
            } else {
                return Result.failure(Throwable(parseErrorBody(response.errorBody())))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun getCoverImageData(accessToken: String, coverImage: String): Result<ImageBitmap> {
        try {
            val response = bookAPI.getBookCoverImage(coverImage, accessToken).execute()
            if (response.isSuccessful) {
                val responseBody = response.body() ?: return Result.failure(Throwable("No body"))
                return Result.success(
                    responseBody.byteStream().use {
                        val byteArray = it.readBytes()
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
                    },
                )
            } else {
                return Result.failure(Throwable(parseErrorBody(response.errorBody())))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun getContentData(accessToken: String, content: String): Result<String> {
        try {
            val response = bookAPI.getBookContent(content, accessToken).execute()
            if (response.isSuccessful) {
                val responseBody = response.body() ?: return Result.failure(Throwable("No body"))
                return Result.success(responseBody.string())
            } else {
                return Result.failure(Throwable(parseErrorBody(response.errorBody())))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun addBook(accessToken: String, req: AddBookRequest): Result<Unit> {
        try {
            val response = bookAPI.addBook(accessToken, req).execute()
            if (response.isSuccessful) {
                return Result.success(Unit)
            } else {
                return Result.failure(Throwable(parseErrorBody(response.errorBody())))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun deleteBook(bookId: Int, accessToken: String): Result<String> {
        try {
            val response = bookAPI.deleteBook(bookId, accessToken).execute()
            if (response.isSuccessful) {
                val responseBody = response.body() ?: return Result.failure(Throwable("No body"))
                return Result.success(responseBody.string())
            } else {
                return Result.failure(Throwable(parseErrorBody(response.errorBody())))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun updateProgress(bookId: Int, progress: Double, accessToken: String): Result<String> {
        try {
            val response = bookAPI.updateProgress(bookId, progress, accessToken).execute()
            if (response.isSuccessful) {
                val responseBody = response.body() ?: return Result.failure(Throwable("No body"))
                return Result.success(responseBody.string())
            } else {
                return Result.failure(Throwable(parseErrorBody(response.errorBody())))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
