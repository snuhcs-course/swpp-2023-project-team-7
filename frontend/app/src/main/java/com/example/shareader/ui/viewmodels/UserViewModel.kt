package com.example.shareader.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shareader.ui.models.UserModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel() : ViewModel() {
    private var scope = CoroutineScope(Dispatchers.IO)

//    private val _userModel = MutableLiveData<UserModel>()
//    val userModel: LiveData<UserModel> = _userModel
//
//    fun loadUser(username: String) {
//        val email = secureStorage.getUserEmail(username)
//        email?.let {
//            _userModel.value = UserModel(username, email)
//        }
//    }
//
    fun signIn(email: String, password: String){
        scope.launch {
            UserModel.getInstance().signIn(email, password).onSuccess {
                println("success")

            }.onFailure {
                println("failed")
            }
        }


    }

    fun signUp(email: String, username:String, password: String){
        scope.launch {
            UserModel.getInstance().signUp(email, username, password).onSuccess {
                println("success")

            }.onFailure {
                println("failed")
            }
        }


    }
}