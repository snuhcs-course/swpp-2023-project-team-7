package com.example.readability.data.user

import com.example.readability.data.parseErrorBody
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
)

data class SignUpRequest(
    val email: String,
    val username: String,
    val password: String,
)

data class SignUpResponse(
    val success: Boolean,
)

data class RefreshTokenResponse(
    val access_token: String,
    val token_type: String,
)

data class UserInfoResponse(
    val username: String,
    val email: String,
    val created_at: String,
    val verified: Int,
)

data class UserData(
    val userName: String? = "",
    val userEmail: String,
    val refreshToken: String?,
    val refreshTokenLife: Long?,
    val accessToken: String?,
    val accessTokenLife: Long?,
    val createdAt: String?,
    val verified: Int?,
)

interface UserAPI {
    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/token")
    fun signIn(
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        // Optional field with default null
        @Field("scope") scope: String? = null,
        // Optional field with default null
        @Field("client_id") clientId: String? = null,
        // Optional field with default null
        @Field("client_secret") clientSecret: String? = null,
    ): Call<TokenResponse>

    @POST("/token/refresh")
    fun refreshAccessToken(@Query("refresh_token") refreshToken: String): Call<RefreshTokenResponse>

    @POST("/user/signup")
    fun signUp(@Body signUpRequest: SignUpRequest): Call<SignUpResponse>

    @GET("/user/info")
    fun getUserInfo(@Query("access_token") accessToken: String): Call<UserInfoResponse>
}

@InstallIn(SingletonComponent::class)
@Module
class UserAPIProviderModule {
    @Provides
    @Singleton
    fun provideUserAPI(): UserAPI {
        return Retrofit.Builder()
            .baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserAPI::class.java)
    }
}

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val userApi: UserAPI,
) {
    suspend fun signIn(email: String, password: String): Result<TokenResponse> {
        val result = userApi.signIn(
            grantType = "",
            scope = "",
            clientId = "",
            clientSecret = "",
            username = email,
            password = password,
        ).execute()
        if (result.isSuccessful) {
            val responseBody = result.body()
            if (responseBody != null) {
                return Result.success(responseBody)
            } else {
                return Result.failure(Throwable("Empty Response Body"))
            }
        } else {
            return Result.failure(Throwable(parseErrorBody(result.errorBody())))
        }
    }

    suspend fun signUp(email: String, username: String, password: String): Result<Unit> {
        val result = userApi.signUp(
            SignUpRequest(
                email = email,
                username = username,
                password = password,
            ),
        ).execute()
        if (result.isSuccessful) {
            val responseBody = result.body()
            if (responseBody?.success == true) {
                return Result.success(Unit)
            } else {
                return Result.failure(Throwable("Empty Response Body"))
            }
        } else {
            return Result.failure(Throwable(parseErrorBody(result.errorBody())))
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): Result<String> {
        val result = userApi.refreshAccessToken(refreshToken).execute()
        if (result.isSuccessful) {
            val responseBody = result.body()
            if (responseBody != null) {
                return Result.success(responseBody.access_token)
            }
            return Result.failure(Throwable("Empty Response Body"))
        } else {
            return Result.failure(Throwable(parseErrorBody(result.errorBody())))
        }
    }

    suspend fun getUserInfo(accessToken: String): Result<UserInfoResponse> {
        val result = userApi.getUserInfo(accessToken = accessToken).execute()
        if (result.isSuccessful) {
            val responseBody = result.body()
            if (responseBody != null) {
                return Result.success(responseBody)
            } else {
                return Result.failure(Throwable("Empty Response Body"))
            }
        } else {
            return Result.failure(Throwable(parseErrorBody(result.errorBody())))
        }
    }
}
