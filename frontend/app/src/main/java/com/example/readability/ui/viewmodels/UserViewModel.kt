package com.example.readability.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readability.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    suspend fun isSignedIn(): Boolean {
        return userRepository.getAccessToken() != null
    }
    suspend fun signIn(username: String, password: String) = userRepository.signIn(username, password)
    suspend fun signUp(email: String, username: String, password: String) =
        userRepository.signUp(email, username, password)
    suspend fun signOut() = userRepository.signOut()
    suspend fun getUserInfo() = userRepository.getUserInfo()
}
