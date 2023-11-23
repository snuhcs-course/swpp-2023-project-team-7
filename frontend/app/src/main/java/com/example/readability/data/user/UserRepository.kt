package com.example.readability.data.user

import com.example.readability.data.book.BookDao
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userDao: UserDao,
    private val bookDao: BookDao,
) {
    val user = userDao.get()
    suspend fun signIn(email: String, password: String): Result<Unit> {
        userRemoteDataSource.signIn(email, password).fold(onSuccess = {
            userDao.insert(
                User(
                    userName = null,
                    userEmail = email,
                    refreshToken = it.refresh_token,
                    refreshTokenLife = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(12),
                    accessToken = it.access_token,
                    accessTokenLife = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30),
                    createdAt = null,
                    verified = null,
                ),
            )
            return Result.success(Unit)
        }, onFailure = {
            return Result.failure(it)
        })
    }

    suspend fun signUp(email: String, username: String, password: String): Result<Unit> {
        userRemoteDataSource.signUp(email, username, password).fold(onSuccess = {
            return signIn(email, password)
        }, onFailure = {
            return Result.failure(it)
        })
    }

    suspend fun getRefreshToken(): String? {
        val user = userDao.get().first() ?: return null
        if (user.refreshTokenLife!! < System.currentTimeMillis()) {
            signOut()
            return null
        }
        return user.refreshToken
    }

    suspend fun getAccessToken(): String? {
        val user = userDao.get().first() ?: return null
        if (user.accessTokenLife!! < System.currentTimeMillis()) {
            refreshAccessToken()
        }
        return user.accessToken
    }

    private suspend fun refreshAccessToken(): Result<Unit> {
        val refreshToken = getRefreshToken() ?: return Result.failure(UserNotSignedInException())
        userRemoteDataSource.refreshAccessToken(refreshToken).fold(onSuccess = {
            userDao.updateAccessToken(
                it,
                System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30),
            )
            return Result.success(Unit)
        }, onFailure = {
            return Result.failure(it)
        })
    }

    suspend fun getUserInfo(): Result<UserInfoResponse> {
        val accessToken = getAccessToken() ?: return Result.failure(UserNotSignedInException())
        userRemoteDataSource.getUserInfo(accessToken).fold(onSuccess = {
            userDao.updateUserInfo(
                username = it.username,
                email = it.email,
                createdAt = it.created_at,
                verified = it.verified,
            )
            return Result.success(it)
        }, onFailure = {
            return Result.failure(it)
        })
    }

    fun signOut() {
        bookDao.deleteAll()
        userDao.deleteAll()
        // TODO: clear bookMap too - currently not possible because injecting BookRepository into UserRepository causes a circular dependency
    }
}