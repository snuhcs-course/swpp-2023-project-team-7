package com.example.readability.ui.models

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Room
import com.example.readability.ReadabilityApplication
import com.example.readability.database.User
import com.example.readability.database.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


data class UserResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
)

data class SignUpResponse(
    val success : Boolean
)

data class RefreshTokenResponse(
    val access_token: String,
    val token_type: String
)

data class UserInfoResponse(
    val username: String,
    val email: String,
    val created_at: String,
    val verified: Int
)

data class SignUpRequest(
    val email: String,
    val username: String,
    val password: String
)

interface LoginAPI {
    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("/token")
    fun signIn(
        @Field("grant_type") grantType: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("scope") scope: String? = null, // Optional field with default null
        @Field("client_id") clientId: String? = null, // Optional field with default null
        @Field("client_secret") clientSecret: String? = null // Optional field with default null
    ): Call<UserResponse>

    @POST("/token/refresh")
    fun refreshAccessToken(
        @Query("refresh_token") refresh_token: String
    ): Call<RefreshTokenResponse>

    @POST("/user/signup")
    fun signUp(@Body signUpRequest: SignUpRequest) : Call<SignUpResponse>

    @POST("/user/info")
    fun getUserInfo(
        @Query("access_token") access_token: String
    ): Call<UserInfoResponse>
}


class UserModel{
    var loginAPI: LoginAPI
    private var username = ""
    private var useremail = ""
    private var access_token = ""
    private var refresh_token = ""
    private var userDB: UserDatabase? = null


    companion object {
        private var instance: UserModel? = null

        fun getInstance(): UserModel {
            if (instance == null) {
                instance = UserModel()
            }
            return instance!!
        }
    }

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://swpp.scripter36.com/")
            .addConverterFactory(GsonConverterFactory.create()).build()

        loginAPI = retrofit.create(LoginAPI::class.java)
        ReadabilityApplication.instance?.applicationContext?.let {
            userDB = Room.databaseBuilder(
                it,
                UserDatabase::class.java, "user"
            ).build()
        }
    }


    suspend fun signIn (email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = loginAPI.signIn(
                    grantType = "", scope = "", clientId = "", clientSecret = "",
                    username = email, password = password

            ).execute()
            if (result.isSuccessful) {
                val responseBody = result.body()
                if (responseBody != null) {
                    access_token = responseBody.access_token
                    refresh_token = responseBody.refresh_token
                    useremail = email
                    userDB?.userDao()?.insert(User(
                        userName = "",
                        userEmail = email,
                        refreshToken = refresh_token,
                        refreshTokenLife = System.currentTimeMillis() + 2 * 7 * 24 * 60 * 60 * 1000,
                        accessToken = access_token,
                        accessTokenLife  = System.currentTimeMillis() + 30 * 60 * 1000
                    ))
                }
                return@withContext Result.success(Unit)
            } else {
                return@withContext Result.failure(Throwable("Login failed"))
            }
        }
    }

    suspend fun signUp (email: String, username: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = loginAPI.signUp(
                SignUpRequest(
                    email = email,
                    username = username,
                    password = password
                )
            ).execute()
            if (result.isSuccessful) {
                println("signup successful")
                val responseBody = result.body()
                if (responseBody != null && responseBody.success) {
                    println("call signIn")
                    return@withContext signIn(email, password).onSuccess {
                        Result.success("Success")
                    }.onFailure {
                        Result.failure<String>(Throwable(it.message))
                    }
                }else{
                    println("but empty")
                    return@withContext Result.failure(Throwable("Empty Response Body"))
                }
            } else {
                println("signup not successful")
                return@withContext Result.failure(Throwable(result.errorBody()?.string()))
            }
        }
    }

    suspend fun refreshAccessToken() : Result<Unit> {
        return withContext(Dispatchers.IO) {
            val result = loginAPI.refreshAccessToken("").execute()
            if (result.isSuccessful) {
                val responseBody = result.body()
                if (responseBody != null) {
                    userDB?.userDao()?.updateAccessToken(responseBody.access_token)
                    return@withContext Result.success(Unit)
                }
                return@withContext Result.failure(Throwable("Empty Response Body"))
            } else {
                return@withContext Result.failure(Throwable(result.errorBody()?.string()))
            }
        }
    }

    suspend fun saveUserInfo (access_token: String, refresh_token: String) {
        return withContext(Dispatchers.IO) {
            val result = loginAPI.getUserInfo( access_token = access_token).execute()
            if (result.isSuccessful) {
                val responseBody = result.body()
                if (responseBody != null) {
                    userDB?.userDao()?.update(User(
                        responseBody.username,
                        responseBody.email,
                        access_token,
                        System.currentTimeMillis() + 30 * 60 * 1000,
                        refresh_token,
                        System.currentTimeMillis() + 2 * 7 * 24 * 60 * 60 * 1000
                    ))
                }
            }
        }
    }

    fun logout(): Result<Unit> {
        userDB?.userDao()?.deleteAll()
        return Result.success(Unit)
    }
}



