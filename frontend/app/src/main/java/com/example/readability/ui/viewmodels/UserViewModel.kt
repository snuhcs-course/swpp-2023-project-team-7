package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readability.data.user.User
import com.example.readability.data.user.UserData
import com.example.readability.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    val user = userRepository.user.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        runBlocking {
            userRepository.user.first()
        },
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserInfo().onFailure {
                println("UserViewModel: getUserInfo failed: ${it.message}")
            }
        }
    }

    private fun userToUserData(user: User) = UserData(
        userName = user.userName,
        userEmail = user.userEmail,
        refreshToken = user.refreshToken,
        refreshTokenLife = user.refreshTokenLife,
        accessToken = user.accessToken,
        accessTokenLife = user.accessTokenLife,
        createdAt = user.createdAt,
        verified = user.verified,
    )
    suspend fun isSignedIn(): Boolean {
        return userRepository.getAccessToken() != null
    }
    suspend fun signIn(username: String, password: String) = userRepository.signIn(username, password)
    suspend fun signUp(email: String, username: String, password: String) =
        userRepository.signUp(email, username, password)
    suspend fun signOut() = userRepository.signOut()
    suspend fun getUserInfo() = userRepository.getUserInfo()

    suspend fun changePassword(newPassword: String) = userRepository.changePassword(newPassword)
}
