package com.snu.readability.data.ai

import com.snu.readability.data.NetworkStatusRepository
import com.snu.readability.data.user.UserNotSignedInException
import com.snu.readability.data.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryRepository @Inject constructor(
    private val summaryRemoteDataSource: SummaryRemoteDataSource,
    private val userRepository: UserRepository,
    private val networkStatusRepository: NetworkStatusRepository,
) {
    private val summaryLoadScope = CoroutineScope(Dispatchers.IO)
    private var lastSummaryLoadJob: Job? = null

    private val _summary = MutableStateFlow("")

    val summary = _summary.asStateFlow()

    suspend fun getSummary(bookId: Int, progress: Double): Result<Unit> {
        if (!networkStatusRepository.isConnected) {
            return Result.failure(Exception("Network not connected"))
        }
        return withContext(Dispatchers.IO) {
            val accessToken = userRepository.getAccessToken() ?: return@withContext Result.failure(
                UserNotSignedInException(),
            )
            try {
                lastSummaryLoadJob?.cancel()
                var error: Throwable? = null
                lastSummaryLoadJob = summaryLoadScope.launch {
                    try {
                        _summary.value = ""
                        summaryRemoteDataSource.getSummary(bookId, progress, accessToken).collect { response ->
                            if (!isActive) return@collect
                            _summary.value += response
                        }
                    } catch (e: Throwable) {
                        error = e
                    }
                }
                lastSummaryLoadJob?.join()
                if (error != null) {
                    return@withContext Result.failure(error!!)
                }
            } catch (e: Throwable) {
                return@withContext Result.failure(e)
            }
            Result.success(Unit)
        }
    }

    fun clearSummary() {
        _summary.value = ""
    }
}
