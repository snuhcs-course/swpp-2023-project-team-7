package com.example.readability.ui.models

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


data class UserResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
)

data class SignUpResponse(
    val success : Boolean
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

    @POST("/user/signup")
    fun signUp(@Body signUpRequest: SignUpRequest) : Call<SignUpResponse>
}


class UserModel{
    var loginAPI: LoginAPI
    private var username = ""
    private var useremail = ""
    private var access_token = ""
    private var refresh_token = ""


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
    }


    suspend fun signIn (email: String, password: String): Result<String> {
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
                }
                return@withContext Result.success("Success")
            } else {
                return@withContext Result.failure<String>(Throwable("Login failed"))
            }
        }
    }

    suspend fun signUp (email: String, username: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val result = loginAPI.signUp(
                SignUpRequest(
                    email = email,
                    username = username,
                    password = password
                )
            ).execute()
            if (result.isSuccessful) {
                val responseBody = result.body()
                if (responseBody != null && responseBody.success) {
                    return@withContext Result.success("Success")
                }else{
                    return@withContext Result.success("SignUp failed")
                }
            } else {
                return@withContext Result.failure<String>(Throwable("SignUp error"))
            }
        }
    }
}



