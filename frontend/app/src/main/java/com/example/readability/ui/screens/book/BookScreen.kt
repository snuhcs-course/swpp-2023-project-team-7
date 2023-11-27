package com.example.readability.ui.screens.book

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.readability.ui.animation.SharedAxis
import com.example.readability.ui.animation.composableSharedAxis
import com.example.readability.ui.viewmodels.AddBookViewModel
import com.example.readability.ui.viewmodels.BookListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class BookScreens(val route: String) {
    object BookList : BookScreens("book_list")
    object AddBook : BookScreens("add_book")
}

@Composable
fun BookScreen(onNavigateSettings: () -> Unit = {}, onNavigateViewer: (id: Int) -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    NavHost(navController = navController, startDestination = BookScreens.BookList.route) {
        composableSharedAxis(BookScreens.BookList.route, axis = SharedAxis.X) {
            val bookListViewModel: BookListViewModel = hiltViewModel()
            val bookCardDataList by bookListViewModel.bookCardDataList.collectAsState()
            BookListView(
                bookCardDataList = bookCardDataList,
                onNavigateAddBook = {
                    navController.navigate(BookScreens.AddBook.route)
                },
                onNavigateSettings = { onNavigateSettings() },
                onLoadContent = { id ->
                    bookListViewModel.getContentData(id)
                },
                onLoadImage = { id ->
                    bookListViewModel.getCoverImageData(id)
                },
                onNavigateViewer = { id ->
                    onNavigateViewer(id)
                },
                onNavigateBookList = {
                    navController.popBackStack()
                },
                onProgressChanged = { id, progress ->
                    bookListViewModel.updateProgress(id, progress)
                },
                onBookDeleted = {id ->
                    bookListViewModel.deleteBook(id)
                    Result.success(Unit)
                },
                onRefresh = {
                    bookListViewModel.updateBookList()
                },
            )
        }
        composableSharedAxis(BookScreens.AddBook.route, axis = SharedAxis.X) {
            val addBookViewModel: AddBookViewModel = hiltViewModel()
            AddBookView(
                onBack = { navController.popBackStack() },
                onBookUploaded = { navController.popBackStack() },
                onAddBookClicked = {
                    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                        withContext(Dispatchers.IO) {
                            addBookViewModel.addBook(it)
                        }
                    } else {
                        Result.failure(Exception("No internet connection"))
                    }
                },
            )
        }
    }
}
