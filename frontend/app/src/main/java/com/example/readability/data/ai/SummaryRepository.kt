package com.example.readability.data.ai

import com.example.readability.data.user.UserNotSignedInException
import com.example.readability.data.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val summaryRemoteDataSource: SummaryRemoteDataSource,
    private val userRepository: UserRepository,
) {
    val summary = MutableStateFlow("")

    suspend fun getSummary(bookId: Int, progress: Double): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val accessToken = userRepository.getAccessToken() ?: return@withContext Result.failure(
                UserNotSignedInException(),
            )
            try {
                summaryRemoteDataSource.getSummary(bookId, progress, accessToken).collect { response ->
                    if (!isActive) return@collect
                    summary.value += response
                }
            } catch (e: Throwable) {
                return@withContext Result.failure(e)
            }
            Result.success(Unit)
        }
    }
}
