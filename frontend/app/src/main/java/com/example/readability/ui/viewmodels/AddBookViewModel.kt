package com.example.readability.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.example.readability.ui.models.AddBookModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AddBookViewModel : ViewModel() {
    private var coverUrl = ""
    private var scope = CoroutineScope(Dispatchers.IO)

    var snackbarMessage = MutableStateFlow("")
    var bookUploaded = MutableStateFlow(false)

    fun uploadImage(image: Bitmap) {
        scope.launch {
            AddBookModel.getInstance().uploadImage(image).onSuccess {
                coverUrl = it
                snackbarMessage.value = "Image uploaded successfully"
            }.onFailure {
                println(it)
                snackbarMessage.value = "Image upload failed"
            }
        }
    }

    fun addBook(title: String, author: String, content: String) {
        scope.launch {
            AddBookModel.getInstance().addBook(title, author, content, coverUrl).onSuccess {
                println("success")
                snackbarMessage.value = "Book added successfully"
                bookUploaded.value = true
            }.onFailure {
                println(it)
                snackbarMessage.value = "Book add failed"
            }
        }
    }

    fun clearSnackbar() {
        snackbarMessage.value = ""
    }
}